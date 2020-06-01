package com.wayyue.tracer.core.context.trace;


import com.wayyue.tracer.core.span.SzTracerSpan;

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
        SzTracerSpan szTracerSpan = threadLocal.get();
        //remove
        this.clear();
        return szTracerSpan;
    }

    @Override
    public int getThreadLocalSpanSize() {
        SzTracerSpan sofaTracerSpan = threadLocal.get();
        return sofaTracerSpan == null ? 0 : 1;
    }

    @Override
    public boolean isEmpty() {
        SzTracerSpan sofaTracerSpan = threadLocal.get();
        return sofaTracerSpan == null;
    }

    @Override
    public void clear() {
        threadLocal.remove();
    }
}
