package com.wayue.tracer.core.async;



import com.wayue.tracer.core.context.trace.SzTraceContext;
import com.wayue.tracer.core.holder.SzTraceContextHolder;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Extended timing thread pool for SOFATracer
 *
 * @author jinming.xiao
 */
public class TracerScheduleExecutorService extends TracedExecutorService implements
                                                                        ScheduledExecutorService {

    public TracerScheduleExecutorService(ScheduledExecutorService delegate) {
        super(delegate, SzTraceContextHolder.getSzTraceContext());
    }

    public TracerScheduleExecutorService(ScheduledExecutorService delegate,
                                         SzTraceContext traceContext) {
        super(delegate, traceContext);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        SzTracerRunnable r = new SzTracerRunnable(command, this.traceContext);
        return getScheduledExecutorService().schedule(r, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        SzTracerCallable c = new SzTracerCallable(callable, this.traceContext);
        return getScheduledExecutorService().schedule(c, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
                                                  TimeUnit unit) {
        SzTracerRunnable r = new SzTracerRunnable(command, this.traceContext);
        return getScheduledExecutorService().scheduleAtFixedRate(r, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
                                                     long delay, TimeUnit unit) {
        SzTracerRunnable r = new SzTracerRunnable(command, this.traceContext);
        return getScheduledExecutorService().scheduleWithFixedDelay(r, initialDelay, delay, unit);
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        return (ScheduledExecutorService) this.delegate;
    }

}