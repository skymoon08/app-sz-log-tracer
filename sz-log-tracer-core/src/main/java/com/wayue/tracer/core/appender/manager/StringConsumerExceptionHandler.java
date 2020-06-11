package com.wayue.tracer.core.appender.manager;


import com.wayue.tracer.core.appender.sefllog.SynchronizingSelfLog;
import disruptor.exception.ExceptionHandler;

/**
 *
 */
public class StringConsumerExceptionHandler implements ExceptionHandler<StringEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, StringEvent event) {
        //Loop call
        if (event != null) {
            SynchronizingSelfLog.error(
                "AsyncConsumer occurs exception during handle StringEvent, The string is["
                        + event.getString() + "]", ex);
        } else {
            SynchronizingSelfLog.error(
                "AsyncConsumer occurs exception during handle StringEvent, The string is null", ex);
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