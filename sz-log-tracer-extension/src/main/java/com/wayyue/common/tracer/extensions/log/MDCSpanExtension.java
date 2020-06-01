package com.wayyue.common.tracer.extensions.log;

import com.wayyue.common.tracer.extensions.log.constants.MDCKeyConstants;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.extensions.SpanExtension;
import com.wayyue.tracer.core.span.SzTracerSpan;
import io.opentracing.Span;
import org.slf4j.MDC;

public class MDCSpanExtension implements SpanExtension {

    @Override
    public void logStartedSpan(Span currentSpan) {
        if (currentSpan != null) {

            SzTracerSpan span = (SzTracerSpan) currentSpan;
            SzTracerSpanContext SzTracerSpanContext = span.getSzTracerSpanContext();
            if (SzTracerSpanContext != null) {
                MDC.put(MDCKeyConstants.MDC_TRACEID, SzTracerSpanContext.getTraceId());
                MDC.put(MDCKeyConstants.MDC_SPANID, SzTracerSpanContext.getSpanId());
            }
        }
    }

    @Override
    public void logStoppedSpan(Span currentSpan) {
        MDC.remove(MDCKeyConstants.MDC_TRACEID);
        MDC.remove(MDCKeyConstants.MDC_SPANID);
        if (currentSpan != null) {
            SzTracerSpan span = (SzTracerSpan) currentSpan;
            SzTracerSpan parentSpan = span.getParentSzTracerSpan();
            if (parentSpan != null) {
                SzTracerSpanContext SzTracerSpanContext = parentSpan.getSzTracerSpanContext();
                if (SzTracerSpanContext != null) {
                    MDC.put(MDCKeyConstants.MDC_TRACEID, SzTracerSpanContext.getTraceId());
                    MDC.put(MDCKeyConstants.MDC_SPANID, SzTracerSpanContext.getSpanId());
                }
            }
        }
    }

    @Override
    public void logStoppedSpanInRunnable(Span currentSpan) {
        MDC.remove(MDCKeyConstants.MDC_TRACEID);
        MDC.remove(MDCKeyConstants.MDC_SPANID);
    }

    @Override
    public String supportName() {
        return "slf4jmdc";
    }

}