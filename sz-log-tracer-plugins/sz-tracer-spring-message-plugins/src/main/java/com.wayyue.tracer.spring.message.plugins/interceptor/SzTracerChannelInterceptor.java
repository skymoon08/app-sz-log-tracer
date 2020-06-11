
package com.wayyue.tracer.spring.message.plugins.interceptor;


import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.context.span.SzTracerSpanContext;
import com.wayue.tracer.core.context.trace.SzTraceContext;
import com.wayue.tracer.core.holder.SzTraceContextHolder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.spring.message.plugins.tracers.MessagePubTracer;
import com.wayyue.tracer.spring.message.plugins.tracers.MessageSubTracer;
import io.opentracing.tag.Tags;
import org.springframework.aop.support.AopUtils;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.messaging.*;
import org.springframework.messaging.support.*;
import org.springframework.util.ClassUtils;
import org.springframework.integration.channel.AbstractMessageChannel;

public class SzTracerChannelInterceptor implements ChannelInterceptor, ExecutorChannelInterceptor {

    private static final String REMOTE_SERVICE_NAME = "broker";

    public static final String STREAM_DIRECT_CHANNEL = "org.springframework.cloud.stream.messaging.DirectWithAttributesChannel";

    final boolean integrationObjectSupportPresent;
    private final boolean hasDirectChannelClass;
    // special case of a Stream
    private final Class<?> directWithAttributesChannelClass;

    private static final String SPAN_CONTEXT_KEY = "STREAM_SPAM_CONTEXT_SOFA";

    private static final String ORIGINAL_ROCKETMQ_MESSAGE_KEY = "ORIGINAL_ROCKETMQ_MESSAGE";

    private final MessageSubTracer messageSubTracer;
    private final MessagePubTracer messagePubTracer;

    private final String applicationName;

    SzTracerChannelInterceptor(String applicationName) {
        this.integrationObjectSupportPresent = ClassUtils.isPresent(
                "org.springframework.integration.context.IntegrationObjectSupport", null);
        this.hasDirectChannelClass = ClassUtils.isPresent(
                "org.springframework.integration.channel.DirectChannel", null);
        this.directWithAttributesChannelClass = ClassUtils.isPresent(STREAM_DIRECT_CHANNEL, null) ? ClassUtils
                .resolveClassName(STREAM_DIRECT_CHANNEL, null) : null;
        messageSubTracer = MessageSubTracer.getMessageSubTracerSingleton();
        messagePubTracer = MessagePubTracer.getMessagePubTracerSingleton();
        this.applicationName = applicationName;
    }

    public static SzTracerChannelInterceptor create(String applicationName) {
        return new SzTracerChannelInterceptor(applicationName);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (emptyMessage(message)) {
            return message;
        }
        Message<?> retrievedMessage = getMessage(message);
        MessageHeaderAccessor headers = mutableHeaderAccessor(retrievedMessage);
        Object spanContextSerialize = parseSpanContext(headers);
        SzTracerSpan szTracerSpan;
        if (spanContextSerialize instanceof String) {
            SzTracerSpanContext spanContext = SzTracerSpanContext
                    .deserializeFromString(spanContextSerialize.toString());
            szTracerSpan = messageSubTracer.serverReceive(spanContext);
            szTracerSpan.setOperationName("mq-message-receive");
        } else {
            szTracerSpan = messagePubTracer.clientSend("mq-message-send");
        }
        // 塞回到 headers 中去
        headers.setHeader(SPAN_CONTEXT_KEY, szTracerSpan.getSzTracerSpanContext()
                .serializeSpanContext());
        Message<?> outputMessage = outputMessage(message, retrievedMessage, headers);
        if (isDirectChannel(channel)) {
            beforeHandle(outputMessage, channel, null);
        }
        return outputMessage;
    }

    private Object parseSpanContext(MessageHeaderAccessor headers) {
        Object spanContext = null;
        return spanContext;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent,
                                    Exception ex) {
        if (emptyMessage(message)) {
            return;
        }
        if (isDirectChannel(channel)) {
            afterMessageHandled(message, channel, null, ex);
        }
        finishSpan(ex, message, channel);
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel,
                                   MessageHandler handler) {
        if (emptyMessage(message)) {
            return message;
        }
        MessageHeaderAccessor headers = mutableHeaderAccessor(message);
        if (message instanceof ErrorMessage) {
            return new ErrorMessage((Throwable) message.getPayload(), headers.getMessageHeaders());
        }
        headers.setImmutable();
        return new GenericMessage<>(message.getPayload(), headers.getMessageHeaders());
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel,
                                    MessageHandler handler, Exception ex) {
        if (emptyMessage(message)) {
            return;
        }
        finishSpan(ex, message, channel);
    }

    private void appendTags(Message<?> message, MessageChannel channel,
                            SzTracerSpan szTracerSpan) {

        Message<?> retrievedMessage = getMessage(message);
        MessageHeaderAccessor headers = mutableHeaderAccessor(retrievedMessage);
        String messageId = message.getHeaders().getId().toString();
        String channelName = channelName(channel);
        szTracerSpan.setTag(CommonSpanTags.MSG_ID, messageId);
        szTracerSpan.setTag(CommonSpanTags.MSG_CHANNEL, channelName);
        szTracerSpan.setTag(CommonSpanTags.REMOTE_APP, REMOTE_SERVICE_NAME);
        szTracerSpan.setTag(CommonSpanTags.LOCAL_APP, applicationName);
    }

    private String channelName(MessageChannel channel) {
        String name = null;
        if (this.integrationObjectSupportPresent) {
            if (channel instanceof IntegrationObjectSupport) {
                name = ((IntegrationObjectSupport) channel).getComponentName();
            }
            if (name == null && channel instanceof AbstractMessageChannel) {
                name = ((AbstractMessageChannel) channel).getFullChannelName();
            }
        }
        if (name == null) {
            name = channel.toString();
        }
        return name;
    }

    private void finishSpan(Exception error, Message<?> message, MessageChannel channel) {
        SzTraceContext szTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan currentSpan = szTraceContext.pop();
        if (currentSpan == null) {
            return;
        }
        appendTags(message, channel, currentSpan);
        String resultCode = SzTracerConstant.RESULT_CODE_SUCCESS;
        if (error != null) {
            String exMessage = error.getMessage();
            if (exMessage == null) {
                exMessage = error.getClass().getSimpleName();
            }
            resultCode = SzTracerConstant.RESULT_CODE_ERROR;
            currentSpan.setTag(Tags.ERROR.getKey(), exMessage);
        }
        currentSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
        currentSpan.finish();
        // 恢复上下文
        if (currentSpan.getParentSzTracerSpan() != null) {
            szTraceContext.push(currentSpan.getParentSzTracerSpan());
        }
    }

    private boolean emptyMessage(Message<?> message) {
        return message == null;
    }

    private Message<?> getMessage(Message<?> message) {
        Object payload = message.getPayload();
        if (payload instanceof MessagingException) {
            MessagingException e = (MessagingException) payload;
            return e.getFailedMessage();
        }
        return message;
    }

    private MessageHeaderAccessor mutableHeaderAccessor(Message<?> message) {
        MessageHeaderAccessor headers = MessageHeaderAccessor.getMutableAccessor(message);
        headers.setLeaveMutable(true);
        return headers;
    }

    private boolean isWebSockets(MessageHeaderAccessor headerAccessor) {
        return headerAccessor.getMessageHeaders().containsKey("stompCommand")
                || headerAccessor.getMessageHeaders().containsKey("simpMessageType");
    }

    private Message<?> outputMessage(Message<?> originalMessage, Message<?> retrievedMessage,
                                     MessageHeaderAccessor additionalHeaders) {
        MessageHeaderAccessor headers = MessageHeaderAccessor.getMutableAccessor(originalMessage);
        if (originalMessage.getPayload() instanceof MessagingException) {
            return new ErrorMessage((MessagingException) originalMessage.getPayload(),
                    isWebSockets(headers) ? headers.getMessageHeaders() : new MessageHeaders(
                            headers.getMessageHeaders()));
        }
        headers.copyHeaders(additionalHeaders.getMessageHeaders());
        return new GenericMessage<>(retrievedMessage.getPayload(),
                isWebSockets(headers) ? headers.getMessageHeaders() : new MessageHeaders(
                        headers.getMessageHeaders()));
    }

    private boolean isDirectChannel(MessageChannel channel) {
        Class<?> targetClass = AopUtils.getTargetClass(channel);
        boolean directChannel = this.hasDirectChannelClass
                && DirectChannel.class.isAssignableFrom(targetClass);
        if (!directChannel) {
            return false;
        }
        if (this.directWithAttributesChannelClass == null) {
            return true;
        }
        return !isStreamSpecialDirectChannel(targetClass);
    }

    private boolean isStreamSpecialDirectChannel(Class<?> targetClass) {
        return this.directWithAttributesChannelClass.isAssignableFrom(targetClass);
    }
}
