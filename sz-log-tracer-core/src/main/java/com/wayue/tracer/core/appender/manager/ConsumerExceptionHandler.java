package com.wayue.tracer.core.appender.manager;


import com.wayue.tracer.core.appender.sefllog.SynchronizingSelfLog;
import com.wayue.tracer.core.span.SzTracerSpan;
import disruptor.exception.ExceptionHandler;


public class ConsumerExceptionHandler implements ExceptionHandler<SzTracerSpanEvent> {

    @Override
    public void handleEventException(Throwable ex, long sequence, SzTracerSpanEvent event) {
        if (event != null) {
            SzTracerSpan tracerSpan = event.getTracerSpan();
            SynchronizingSelfLog.error("AsyncConsumer occurs exception during handle SzTracerSpanEvent, " +
                    "The szTracerSpan is[" + tracerSpan + "]", ex);
        } else {
            SynchronizingSelfLog
                .error("AsyncConsumer occurs exception during handle SzTracerSpanEvent, The szTracerSpan is null", ex);

        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        SynchronizingSelfLog.error("AsyncConsumer occurs exception on start", ex);

    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        SynchronizingSelfLog.error("Disruptor or AsyncConsumer occurs exception on shutdown", ex);

    }
}