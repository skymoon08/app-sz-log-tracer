package com.wayyue.tracer.core.listener;


import com.wayyue.tracer.core.span.SzTracerSpan;

/**
 * SpanReportListener:Reporter extension interface
 *
 */
public interface SpanReportListener {

    /**
     * Reporter extension callback method
     * It can be printed to the log, or it can be reported to some remote server
     * @param szTracerSpan
     */
    void onSpanReport(SzTracerSpan szTracerSpan);
}
