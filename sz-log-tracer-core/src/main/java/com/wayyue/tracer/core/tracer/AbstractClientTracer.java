
package com.wayyue.tracer.core.tracer;


import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.span.SzTracerSpan;

/**
 * AbstractClientTracer
 *
 */
public abstract class AbstractClientTracer extends AbstractTracer {

    public AbstractClientTracer(String tracerType) {
        //client tracer
        super(tracerType, true, false);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return null;
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return null;
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return null;
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getServerDigestEncoder() {
        return null;
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateServerStatReporter() {
        return null;
    }
}
