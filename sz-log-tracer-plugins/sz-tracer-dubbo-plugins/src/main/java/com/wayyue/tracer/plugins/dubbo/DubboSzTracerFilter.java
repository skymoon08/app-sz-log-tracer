package com.wayyue.tracer.plugins.dubbo;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.samplers.Sampler;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.LogData;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import com.wayyue.tracer.plugins.dubbo.tracer.DubboConsumerSzTracer;
import com.wayyue.tracer.plugins.dubbo.tracer.DubboProviderSzTracer;
import disruptor.exception.TimeoutException;
import io.opentracing.tag.Tags;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, order = 1)
public class DubboSzTracerFilter implements Filter {

    private String appName = StringUtils.EMPTY_STRING;

    private static final String BLANK = StringUtils.EMPTY_STRING;

    private static final String SPAN_INVOKE_KEY = "sofa.current.span.key";

    private DubboConsumerSzTracer dubboConsumerSzTracer;

    private DubboProviderSzTracer dubboProviderSzTracer;

    private static Map<String, SzTracerSpan> TracerSpanMap = new ConcurrentHashMap<String, SzTracerSpan>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // do not record
        if ("$echo".equals(invocation.getMethodName())) {
            return invoker.invoke(invocation);
        }

        RpcContext rpcContext = RpcContext.getContext();
        // get appName
        if (StringUtils.isBlank(this.appName)) {
            this.appName = SzTracerConfiguration.getProperty(SzTracerConfiguration.TRACER_APPNAME_KEY);
        }
        // get span kind by rpc request type
        String spanKind = spanKind(rpcContext);
        Result result;
        if (spanKind.equals(Tags.SPAN_KIND_SERVER)) {
            result = doServerFilter(invoker, invocation);
        } else {
            result = doClientFilter(rpcContext, invoker, invocation);
        }
        return result;
    }

    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        String spanKey = getTracerSpanMapKey(invoker);
        try {
            // only the asynchronous callback to print
            boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
            if (!isAsync) {
                return result;
            }
            if (TracerSpanMap.containsKey(spanKey)) {
                SzTracerSpan sofaTracerSpan = TracerSpanMap.get(spanKey);
                // to build tracer instance
                if (dubboConsumerSzTracer == null) {
                    this.dubboConsumerSzTracer = DubboConsumerSzTracer.getDubboConsumerSzTracerSingleton();
                }
                String resultCode = SzTracerConstant.RESULT_CODE_SUCCESS;
                if (result.hasException()) {
                    if (result.getException() instanceof RpcException) {
                        resultCode = Integer.toString(((RpcException) result.getException())
                                .getCode());
                        sofaTracerSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
                    } else {
                        resultCode = SzTracerConstant.RESULT_CODE_ERROR;
                    }
                }
                // add elapsed time
                appendElapsedTimeTags(invocation, sofaTracerSpan, result, true);
                dubboConsumerSzTracer.clientReceiveTagFinish(sofaTracerSpan, resultCode);
            }
        } finally {
            if (TracerSpanMap.containsKey(spanKey)) {
                TracerSpanMap.remove(spanKey);
            }
        }
        return result;
    }

    /**
     * rpc client handler
     *
     * @param rpcContext
     * @param invoker
     * @param invocation
     * @return
     */
    private Result doClientFilter(RpcContext rpcContext, Invoker<?> invoker, Invocation invocation) {
        // to build tracer instance
        if (dubboConsumerSzTracer == null) {
            this.dubboConsumerSzTracer = DubboConsumerSzTracer.getDubboConsumerSzTracerSingleton();
        }
        // get methodName
        String methodName = rpcContext.getMethodName();
        // get service interface
        String service = invoker.getInterface().getSimpleName();
        // build a dubbo rpc span
        SzTracerSpan szTracerSpan = dubboConsumerSzTracer.clientSend(service + "#" + methodName);
        // set tags to span
        appendRpcClientSpanTags(invoker, szTracerSpan);
        // do serialized and then transparent transmission to the rpc server
        String serializedSpanContext = szTracerSpan.getSzTracerSpanContext().serializeSpanContext();
        //put into attachments
        invocation.getAttachments().put(CommonSpanTags.RPC_TRACE_NAME, serializedSpanContext);
        // check invoke type
        boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        boolean isOneWay = false;
        if (isAsync) {
            szTracerSpan.setTag(CommonSpanTags.INVOKE_TYPE, "future");
        } else {
            isOneWay = RpcUtils.isOneway(invoker.getUrl(), invocation);
            if (isOneWay) {
                szTracerSpan.setTag(CommonSpanTags.INVOKE_TYPE, "oneway");
            } else {
                szTracerSpan.setTag(CommonSpanTags.INVOKE_TYPE, "sync");
            }
        }
        Result result;
        Throwable exception = null;
        String resultCode = SzTracerConstant.RESULT_CODE_SUCCESS;
        try {
            // do invoke
            result = invoker.invoke(invocation);
            // check result
            if (result == null) {
                // isOneWay, we think that the current request is successful
                if (isOneWay) {
                    szTracerSpan.setTag(CommonSpanTags.RESP_SIZE, 0);
                }
            } else {
                // add elapsed time
                appendElapsedTimeTags(invocation, szTracerSpan, result, true);
            }
        } catch (RpcException e) {
            exception = e;
            throw e;
        } catch (Throwable t) {
            exception = t;
            throw new RpcException(t);
        } finally {
            if (exception != null) {
                if (exception instanceof RpcException) {
                    szTracerSpan.setTag(Tags.ERROR.getKey(), exception.getMessage());
                    RpcException rpcException = (RpcException) exception;
                    resultCode = String.valueOf(rpcException.getCode());
                } else {
                    resultCode = SzTracerConstant.RESULT_CODE_ERROR;
                }
            }

            if (!isAsync) {
                dubboConsumerSzTracer.clientReceive(resultCode);
            } else {
                SzTraceContext szTraceContext = SzTraceContextHolder.getSzTraceContext();

                SzTracerSpan clientSpan = szTraceContext.pop();
                if (clientSpan != null) {
                    // Record client send event
                    szTracerSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                }
                // cache the current span
                TracerSpanMap.put(getTracerSpanMapKey(invoker), szTracerSpan);
                if (clientSpan != null && clientSpan.getParentSzTracerSpan() != null) {
                    //restore parent
                    szTraceContext.push(clientSpan.getParentSzTracerSpan());
                }
                CompletableFuture<Object> future = (CompletableFuture<Object>) RpcContext.getContext().getFuture();
                future.whenComplete((object, throwable) -> {
                    if (throwable instanceof TimeoutException) {
                        szTracerSpan.setTag(Tags.ERROR.getKey(), throwable.getMessage());
                        dubboConsumerSzTracer.clientReceiveTagFinish(szTracerSpan, SzTracerConstant.RESULT_CODE_TIME_OUT);
                    }
                });
            }
        }
        return result;
    }

    /**
     * rpc client handler
     *
     * @param invoker
     * @param invocation
     * @return
     */
    private Result doServerFilter(Invoker<?> invoker, Invocation invocation) {
        if (dubboProviderSzTracer == null) {
            this.dubboProviderSzTracer = DubboProviderSzTracer.getDubboProviderSofaTracerSingleton();
        }
        SzTracerSpan szTracerSpan = serverReceived(invocation);
        appendRpcServerSpanTags(invoker, szTracerSpan);
        Result result;
        Throwable exception = null;
        try {
            result = invoker.invoke(invocation);
            if (result == null) {
                return null;
            } else {
                appendElapsedTimeTags(invocation, szTracerSpan, result, false);
            }
            if (result.hasException()) {
                exception = result.getException();
            }
            return result;
        } catch (RpcException e) {
            exception = e;
            throw e;
        } catch (Throwable t) {
            exception = t;
            throw new RpcException(t);
        } finally {
            String resultCode = SzTracerConstant.RESULT_CODE_SUCCESS;
            if (exception != null) {
                if (exception instanceof RpcException) {
                    szTracerSpan.setTag(Tags.ERROR.getKey(), exception.getMessage());
                    RpcException rpcException = (RpcException) exception;
                    if (rpcException.isBiz()) {
                        resultCode = String.valueOf(rpcException.getCode());
                    }
                } else {
                    resultCode = SzTracerConstant.RESULT_CODE_ERROR;
                }
            }
            dubboProviderSzTracer.serverSend(resultCode);
        }
    }

    private SzTracerSpan serverReceived(Invocation invocation) {
        Map<String, String> tags = new HashMap<>();
        tags.put(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        String serializeSpanContext = invocation.getAttachments().get(CommonSpanTags.RPC_TRACE_NAME);
        SzTracerSpanContext sofaTracerSpanContext = SzTracerSpanContext.deserializeFromString(serializeSpanContext);
        boolean isCalculateSampler = false;
        boolean isSampled = true;
        if (sofaTracerSpanContext == null) {
            SelfDefineLog.error("SpanContext created error when server received and root SpanContext created.");
            sofaTracerSpanContext = SzTracerSpanContext.rootStart();
            isCalculateSampler = true;
        }
        String simpleName = invocation.getInvoker().getInterface().getSimpleName();
        SzTracerSpan serverSpan = new SzTracerSpan(dubboProviderSzTracer.getSzTracer(),
                System.currentTimeMillis(), simpleName, sofaTracerSpanContext, tags);
        // calculate sampler
        if (isCalculateSampler) {
            Sampler sampler = dubboProviderSzTracer.getSzTracer().getSampler();
            if (sampler != null) {
                isSampled = sampler.sample(serverSpan).isSampled();
            }
            sofaTracerSpanContext.setSampled(isSampled);
        }
        SzTraceContext sofaTraceContext = SzTraceContextHolder.getSzTraceContext();
        // Record server receive event
        serverSpan.log(LogData.SERVER_RECV_EVENT_VALUE);
        sofaTraceContext.push(serverSpan);
        return serverSpan;
    }

    private void appendElapsedTimeTags(Invocation invocation, SzTracerSpan szTracerSpan,
                                       Result result, boolean isClient) {
        if (szTracerSpan == null) {
            return;
        }
        String reqSize;
        String respSize;
        String elapsed;
        String deElapsed;
        if (isClient) {
            reqSize = invocation.getAttachment(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE);
            elapsed = invocation.getAttachment(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME);
            respSize = result.getAttachment(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE);
            deElapsed = result.getAttachment(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME);
            szTracerSpan.setTag(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME, parseAttachment(elapsed, 0));
            szTracerSpan.setTag(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME, parseAttachment(deElapsed, 0));
            szTracerSpan.setTag(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE, parseAttachment(reqSize, 0));
            szTracerSpan.setTag(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE, parseAttachment(respSize, 0));
        } else {
            reqSize = invocation.getAttachment(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE);
            deElapsed = invocation.getAttachment(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME);
            respSize = result.getAttachment(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE);
            elapsed = result.getAttachment(AttachmentKeyConstants.SERVER_SERIALIZE_TIME);
            szTracerSpan.setTag(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE, parseAttachment(reqSize, 0));
            szTracerSpan.setTag(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME, parseAttachment(deElapsed, 0));
            szTracerSpan.setTag(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE, parseAttachment(respSize, 0));
            szTracerSpan.setTag(AttachmentKeyConstants.SERVER_SERIALIZE_TIME, parseAttachment(elapsed, 0));
        }

    }

    private int parseAttachment(String value, int defaultVal) {
        try {
            if (StringUtils.isNotBlank(value)) {
                defaultVal = Integer.parseInt(value);
            }
        } catch (Exception e) {
            SelfDefineLog.error("Failed to parse Dubbo plugin params.", e);
        }
        return defaultVal;
    }

    /**
     * set rpc server span tags
     *
     * @param invoker
     * @param szTracerSpan
     */
    private void appendRpcServerSpanTags(Invoker<?> invoker, SzTracerSpan szTracerSpan) {
        if (szTracerSpan == null) {
            return;
        }
        RpcContext rpcContext = RpcContext.getContext();
        Map<String, String> tagsStr = szTracerSpan.getTagsWithStr();
        tagsStr.put(Tags.SPAN_KIND.getKey(), spanKind(rpcContext));
        String service = invoker.getInterface().getName();
        tagsStr.put(CommonSpanTags.SERVICE, service == null ? BLANK : service);
        String methodName = rpcContext.getMethodName();
        tagsStr.put(CommonSpanTags.METHOD, methodName == null ? BLANK : methodName);
        String app = rpcContext.getUrl().getParameter(Constants.APPLICATION_KEY);
        tagsStr.put(CommonSpanTags.REMOTE_HOST, rpcContext.getRemoteHost());
        tagsStr.put(CommonSpanTags.LOCAL_APP, app == null ? BLANK : app);
        tagsStr.put(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        String protocol = rpcContext.getUrl().getProtocol();
        tagsStr.put(CommonSpanTags.PROTOCOL, protocol == null ? BLANK : protocol);
        tagsStr.put(CommonSpanTags.LOCAL_HOST, rpcContext.getRemoteHost());
        tagsStr.put(CommonSpanTags.LOCAL_PORT, String.valueOf(rpcContext.getRemotePort()));
    }

    private void appendRpcClientSpanTags(Invoker<?> invoker, SzTracerSpan sofaTracerSpan) {
        if (sofaTracerSpan == null) {
            return;
        }
        RpcContext rpcContext = RpcContext.getContext();
        Map<String, String> tagsStr = sofaTracerSpan.getTagsWithStr();
        tagsStr.put(Tags.SPAN_KIND.getKey(), spanKind(rpcContext));
        String protocol = rpcContext.getUrl().getProtocol();
        tagsStr.put(CommonSpanTags.PROTOCOL, protocol == null ? BLANK : protocol);
        String service = invoker.getInterface().getName();
        tagsStr.put(CommonSpanTags.SERVICE, service == null ? BLANK : service);
        String methodName = rpcContext.getMethodName();
        tagsStr.put(CommonSpanTags.METHOD, methodName == null ? BLANK : methodName);
        tagsStr.put(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
        String app = rpcContext.getUrl().getParameter(Constants.APPLICATION_KEY);
        tagsStr.put(CommonSpanTags.LOCAL_APP, app == null ? BLANK : app);
        tagsStr.put(CommonSpanTags.REMOTE_HOST, rpcContext.getRemoteHost());
        tagsStr.put(CommonSpanTags.REMOTE_PORT, String.valueOf(rpcContext.getRemotePort()));
        tagsStr.put(CommonSpanTags.LOCAL_HOST, rpcContext.getLocalHost());
    }

    private String spanKind(RpcContext rpcContext) {
        return rpcContext.isConsumerSide() ? Tags.SPAN_KIND_CLIENT : Tags.SPAN_KIND_SERVER;
    }

    private String getTracerSpanMapKey(Invoker<?> invoker) {
        return SPAN_INVOKE_KEY + "." + invoker.hashCode();
    }
}
