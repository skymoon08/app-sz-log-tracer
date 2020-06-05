package com.wayyue.tracer.core.async;


import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.extensions.SpanExtensionFactory;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.span.SzTracerSpan;

/**
 * Runnable that passes Span between threads. The Span name is
 * taken either from the passed value or from the interface.
 *
 * @author jinming.xiao
 */
public class SzTracerRunnable implements Runnable {

    private long             tid = Thread.currentThread().getId();
    private Runnable         wrappedRunnable;
    private SzTraceContext traceContext;
    private SzTracerSpan currentSpan;

    public SzTracerRunnable(Runnable wrappedRunnable) {
        this.initRunnable(wrappedRunnable, SzTraceContextHolder.getSzTraceContext());
    }

    public SzTracerRunnable(Runnable wrappedRunnable, SzTraceContext traceContext) {
        this.initRunnable(wrappedRunnable, traceContext);
    }

    private void initRunnable(Runnable wrappedRunnable, SzTraceContext traceContext) {
        this.wrappedRunnable = wrappedRunnable;
        this.traceContext = traceContext;
        if (!traceContext.isEmpty()) {
            this.currentSpan = traceContext.getCurrentSpan();
        } else {
            this.currentSpan = null;
        }
    }

    @Override
    public void run() {
        if (Thread.currentThread().getId() != tid) {
            if (currentSpan != null) {
                traceContext.push(currentSpan);
                SpanExtensionFactory.logStartedSpan(currentSpan);
            }
        }
        try {
            wrappedRunnable.run();
        } finally {
            if (Thread.currentThread().getId() != tid) {
                if (currentSpan != null) {
                    traceContext.pop();
                }
            }
        }
    }
}