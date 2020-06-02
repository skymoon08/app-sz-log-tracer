package com.wayyue.tracer.core.appender.manager;


import com.wayyue.tracer.core.appender.TraceAppender;
import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.appender.file.LoadTestAwareAppender;
import com.wayyue.tracer.core.appender.sefllog.SynchronizingSelfLog;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.TracerUtils;
import disruptor.buffer.RingBuffer;
import disruptor.dsl.Disruptor;
import disruptor.dsl.ProducerType;
import disruptor.exception.InsufficientCapacityException;
import disruptor.handler.EventHandler;
import disruptor.strategy.BlockingWaitStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jinming.xiao
 * @since 2020/06/01
 * @version $Id: AsyncCommonDigestAppenderManager.java, v 0.1 October 23, 2017 9：47 AM liangen Exp $
 */
public class AsyncCommonDigestAppenderManager {
    private final Map<String, TraceAppender> appenders = new ConcurrentHashMap<String, TraceAppender>();
    private final Map<String, SpanEncoder> contextEncoders = new ConcurrentHashMap<String, SpanEncoder>();

    private Disruptor<SzTracerSpanEvent> disruptor;
    private RingBuffer<SzTracerSpanEvent> ringBuffer;
    private final ConsumerThreadFactory threadFactory = new ConsumerThreadFactory();

    private List<Consumer> consumers;
    private AtomicInteger index = new AtomicInteger(0);
    private static final int DEFAULT_CONSUMER_NUMBER = 3;

    private boolean allowDiscard;
    private boolean isOutDiscardNumber;
    private boolean isOutDiscardId;
    private long discardOutThreshold;
    private PaddedAtomicLong discardCount;

    private static final String DEFAULT_ALLOW_DISCARD = "true";
    private static final String DEFAULT_IS_OUT_DISCARD_NUMBER = "true";
    private static final String DEFAULT_IS_OUT_DISCARD_ID = "false";

    private static final String DEFAULT_DISCARD_OUT_THRESHOLD = "500";

    public AsyncCommonDigestAppenderManager(int queueSize, int consumerNumber) {
        int realQueueSize = 1 << (32 - Integer.numberOfLeadingZeros(queueSize - 1));
        disruptor = new Disruptor<SzTracerSpanEvent>(new SzTracerSpanEventFactory(),
                realQueueSize, threadFactory, ProducerType.MULTI, new BlockingWaitStrategy());

        this.consumers = new ArrayList<Consumer>(consumerNumber);

        for (int i = 0; i < consumerNumber; i++) {
            Consumer consumer = new Consumer();
            consumers.add(consumer);
            disruptor.setDefaultExceptionHandler(new ConsumerExceptionHandler());
            disruptor.handleEventsWith(consumer);
        }

        this.allowDiscard = Boolean.parseBoolean(SzTracerConfiguration.getProperty(
                SzTracerConfiguration.TRACER_ASYNC_APPENDER_ALLOW_DISCARD, DEFAULT_ALLOW_DISCARD));
        if (allowDiscard) {
            this.isOutDiscardNumber = Boolean.parseBoolean(SzTracerConfiguration.getProperty(
                    SzTracerConfiguration.TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_NUMBER,
                    DEFAULT_IS_OUT_DISCARD_NUMBER));
            this.isOutDiscardId = Boolean.parseBoolean(SzTracerConfiguration.getProperty(
                    SzTracerConfiguration.TRACER_ASYNC_APPENDER_IS_OUT_DISCARD_ID,
                    DEFAULT_IS_OUT_DISCARD_ID));
            this.discardOutThreshold = Long.parseLong(SzTracerConfiguration.getProperty(
                    SzTracerConfiguration.TRACER_ASYNC_APPENDER_DISCARD_OUT_THRESHOLD,
                    DEFAULT_DISCARD_OUT_THRESHOLD));

            if (isOutDiscardNumber) {
                this.discardCount = new PaddedAtomicLong(0L);
            }
        }
    }

    public AsyncCommonDigestAppenderManager(int queueSize) {
        this(queueSize, DEFAULT_CONSUMER_NUMBER);
    }

    public void start(final String workerName) {
        this.threadFactory.setWorkName(workerName);

        this.ringBuffer = this.disruptor.start();

    }

    public void addAppender(String logType, TraceAppender appender, SpanEncoder encoder) {
        if (isAppenderOrEncoderExist(logType)) {
            SynchronizingSelfLog.error("logType[" + logType
                    + "] already is added AsyncCommonDigestAppenderManager");
            return;
        }

        appenders.put(logType, appender);
        contextEncoders.put(logType, encoder);

        consumers.get(index.incrementAndGet() % consumers.size()).addLogType(logType);
    }

    public boolean isAppenderOrEncoderExist(String logType) {
        return appenders.containsKey(logType) || contextEncoders.containsKey(logType);
    }

    public boolean isAppenderAndEncoderExist(String logType) {
        return appenders.containsKey(logType) && contextEncoders.containsKey(logType);
    }

    public boolean isAppenderExist(Character logType) {
        return appenders.containsKey(logType);
    }

    public boolean append(SzTracerSpan tracerSpan) {
        long sequence = 0L;
        if (allowDiscard) {
            try {
                sequence = ringBuffer.tryNext();
            } catch (InsufficientCapacityException e) {

                if (isOutDiscardId) {
                    SzTracerSpanContext szTracerSpanContext = tracerSpan.getSzTracerSpanContext();
                    if (szTracerSpanContext != null) {
                        SynchronizingSelfLog.warn("discarded tracer: traceId["
                                + szTracerSpanContext.getTraceId()
                                + "];spanId[" + szTracerSpanContext.getSpanId()
                                + "]");
                    }
                }

                if ((isOutDiscardNumber) && discardCount.incrementAndGet() == discardOutThreshold) {
                    discardCount.set(0);
                    if (isOutDiscardNumber) {
                        SynchronizingSelfLog.warn("discarded " + discardOutThreshold + " logs");
                    }
                }
                return false;
            }
        } else {
            sequence = ringBuffer.next();
        }
        try {
            SzTracerSpanEvent event = ringBuffer.get(sequence);
            event.setTracerSpan(tracerSpan);
        } catch (Exception e) {
            SynchronizingSelfLog.error("fail to add event");
            return false;
        }
        ringBuffer.publish(sequence);
        return true;
    }

    private class Consumer implements EventHandler<SzTracerSpanEvent> {
        protected Set<String> logTypes = Collections.synchronizedSet(new HashSet<String>());

        @Override
        public void onEvent(SzTracerSpanEvent event, long sequence, boolean endOfBatch) throws Exception {

            SzTracerSpan szTracerSpan = event.getTracerSpan();

            if (szTracerSpan != null) {
                try {

                    String logType = szTracerSpan.getLogType();
                    if (logTypes.contains(logType)) {
                        SpanEncoder encoder = contextEncoders.get(logType);
                        TraceAppender appender = appenders.get(logType);

                        String encodedStr = encoder.encode(szTracerSpan);
                        if (appender instanceof LoadTestAwareAppender) {
                            ((LoadTestAwareAppender) appender).append(encodedStr,
                                    TracerUtils.isLoadTest(szTracerSpan));
                        } else {
                            appender.append(encodedStr);
                        }
                        appender.flush();

                    }
                } catch (Exception e) {
                    SzTracerSpanContext tracerSpanContext = szTracerSpan.getSzTracerSpanContext();
                    if (tracerSpanContext != null) {
                        SynchronizingSelfLog.error(
                                "fail to async write log,tracerId["
                                        + tracerSpanContext.getTraceId() + "];spanId["
                                        + tracerSpanContext.getSpanId() + "]", e);
                    } else {
                        SynchronizingSelfLog.error(
                                "fail to async write log.And the tracerSpanContext is null", e);
                    }

                }
            }

        }

        public void addLogType(String logType) {
            logTypes.add(logType);
        }
    }

    class PaddedAtomicLong extends AtomicLong {
        public volatile long p1, p2, p3, p4, p5, p6 = 7L;

        public PaddedAtomicLong(long initialValue) {
            super(initialValue);
        }

        public PaddedAtomicLong() {
        }
    }

}