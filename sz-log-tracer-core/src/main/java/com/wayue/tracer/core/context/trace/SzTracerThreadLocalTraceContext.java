package com.wayue.tracer.core.context.trace;


import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.EmptyStackException;

/**
 * SzTracerThreadLocalTraceContext
 *
 */
public class SzTracerThreadLocalTraceContext implements SzTraceContext {

    private final ThreadLocal<SzTracerSpan> threadLocal = new ThreadLocal<SzTracerSpan>();

    @Override
    public void push(SzTracerSpan span) {
        if (span == null) {
            return;
        }
        threadLocal.set(span);
    }

    @Override
    public SzTracerSpan getCurrentSpan() throws EmptyStackException {
        if (this.isEmpty()) {
            return null;
        }
        return threadLocal.get();
    }

    @Override
    public SzTracerSpan pop() throws EmptyStackException {
        if (this.isEmpty()) {
            return null;
        }
        SzTracerSpan tracerSpan = threadLocal.get();
        //remove
        this.clear();
        return tracerSpan;
    }

    @Override
    public int getThreadLocalSpanSize() {
        SzTracerSpan tracerSpan = threadLocal.get();
        return tracerSpan == null ? 0 : 1;
    }

    @Override
    public boolean isEmpty() {
        SzTracerSpan tracerSpan = threadLocal.get();
        return tracerSpan == null;
    }

    @Override
    public void clear() {
        threadLocal.remove();
    }
}
