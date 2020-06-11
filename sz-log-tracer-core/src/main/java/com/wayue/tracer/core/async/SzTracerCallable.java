package com.wayue.tracer.core.async;

import com.wayue.tracer.core.context.trace.SzTraceContext;
import com.wayue.tracer.core.extensions.SpanExtensionFactory;
import com.wayue.tracer.core.holder.SzTraceContextHolder;
import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.concurrent.Callable;

/**
 * Callable that passes Span between threads. The Span name is
 * taken either from the passed value or from the interface.
 *
 * @author jinming.xiao
 */
public class SzTracerCallable<T> implements Callable<T> {

    private long tid = Thread.currentThread().getId();
    private Callable<T> wrappedCallable;
    private SzTraceContext traceContext;
    private SzTracerSpan currentSpan;

    public SzTracerCallable(Callable<T> wrappedCallable) {
        this.initCallable(wrappedCallable, SzTraceContextHolder.getSzTraceContext());
    }

    public SzTracerCallable(Callable<T> wrappedCallable, SzTraceContext traceContext) {
        this.initCallable(wrappedCallable, traceContext);
    }

    private void initCallable(Callable<T> wrappedCallable, SzTraceContext traceContext) {
        this.wrappedCallable = wrappedCallable;
        this.traceContext = traceContext;
        if (!traceContext.isEmpty()) {
            this.currentSpan = traceContext.getCurrentSpan();
        } else {
            this.currentSpan = null;
        }
    }

    @Override
    public T call() throws Exception {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.push(currentSpan);
                SpanExtensionFactory.logStartedSpan(currentSpan);
            }
        }
        try {
            return wrappedCallable.call();
        } finally {
            if (Thread.currentThread().getId() != tid) {
                if (currentSpan != null) {
                    traceContext.pop();
                }
            }
        }
    }

}