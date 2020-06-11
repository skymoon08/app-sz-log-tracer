package com.wayue.tracer.core.extensions;

import io.opentracing.Span;


public interface SpanExtension {
    /**
     * Called at the beginning of span
     * @param currentSpan
     */
    void logStartedSpan(Span currentSpan);

    /**
     * Called at the end of span
     * @param currentSpan
     */
    void logStoppedSpan(Span currentSpan);

    /**
     * Called at the end of span in Runnable
     * @param currentSpan
     */
    void logStoppedSpanInRunnable(Span currentSpan);

    /**
     * return support name
     * @return
     */
    String supportName();

}