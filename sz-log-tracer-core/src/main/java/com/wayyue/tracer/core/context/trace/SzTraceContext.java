package com.wayyue.tracer.core.context.trace;


import com.wayyue.tracer.core.span.SzTracerSpan;

/**
 * SzTraceContext allows an application access and manipulation of the current span state.
 *
 * @author yangguanchao
 * @since  2017/06/17
 */
public interface SzTraceContext {

    /**
     * Adds the given span to the TraceContext
     *
     * if the span is null ,then ignore pushed
     *
     * @param span The span to be pushed onto the thread local stacked.
     */
    void push(SzTracerSpan span);

    /**
     * Retrieves the current span without modifying the TraceContext
     *
     * @return returns the current span on the thread local stack without removing it from the stack.
     */
    SzTracerSpan getCurrentSpan();

    /**
     * Removes a span from the TraceContext
     *
     * @return returns and removes the current span from the top of the stack
     */
    SzTracerSpan pop();

    /**
     * Retrieves the current span size stored in current thread local
     *
     * @return the span size of current thread local
     */
    int getThreadLocalSpanSize();

    /**
     * Clear current thread local span
     */
    void clear();

    /**
     * Checks if their is any span set in the current TraceContext
     *
     * @return returns a boolean saying weather or not the thread local is empty
     */
    boolean isEmpty();
}
