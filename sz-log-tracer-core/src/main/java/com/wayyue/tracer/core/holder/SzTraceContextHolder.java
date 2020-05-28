package com.wayyue.tracer.core.holder;


import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.context.trace.SzTracerThreadLocalTraceContext;

/**
 * SzTraceContextHolder
 *
 * @author:   zhanglong
 * @since:     2020/5/27
 */
public class SzTraceContextHolder {

    /**
     * singleton SzTraceContext
     */
    private static final SzTraceContext SZ_TRACE_CONTEXT = new SzTracerThreadLocalTraceContext();

    /**
     * Get threadlocal alipay trace context
     * @return SzTraceContext
     */
    public static SzTraceContext getSzTraceContext() {
        return SZ_TRACE_CONTEXT;
    }
}
