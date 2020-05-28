
package com.wayyue.tracer.core.tracer;


import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.span.SzTracerSpan;

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
