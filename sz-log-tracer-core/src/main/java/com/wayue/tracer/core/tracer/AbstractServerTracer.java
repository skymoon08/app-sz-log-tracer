
package com.wayue.tracer.core.tracer;


import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;

/**
 * AbstractServerTracer
 *
 */
public abstract class AbstractServerTracer extends  AbstractTracer {

    public AbstractServerTracer(String tracerType) {
        super(tracerType, false, true);
    }

    protected String getClientDigestReporterLogName() {
        return null;
    }

    protected String getClientDigestReporterRollingKey() {
        return null;
    }

    protected String getClientDigestReporterLogNameKey() {
        return null;
    }

    protected SpanEncoder<SzTracerSpan> getClientDigestEncoder() {
        return null;
    }

    protected AbstractSzTracerStatisticReporter generateClientStatReporter() {
        return null;
    }
}
