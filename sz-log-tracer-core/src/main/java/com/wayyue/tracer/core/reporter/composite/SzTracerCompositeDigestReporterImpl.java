package com.wayyue.tracer.core.reporter.composite;


import com.wayyue.tracer.core.reporter.facade.AbstractReporter;
import com.wayyue.tracer.core.reporter.facade.Reporter;
import com.wayyue.tracer.core.span.SzTracerSpan;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SzTracerCompositeDigestReporterImpl
 *
 */
public class SzTracerCompositeDigestReporterImpl extends AbstractReporter {

    private Map<String, Reporter> compositedReporters = new ConcurrentHashMap<String, Reporter>();

    public synchronized boolean addReporter(Reporter reporter) {
        if (reporter == null) {
            return false;
        }
        String reporterType = reporter.getReporterType();
        if (compositedReporters.containsKey(reporterType)) {
            return false;
        }
        this.compositedReporters.put(reporterType, reporter);
        return true;
    }

    @Override
    public String getReporterType() {
        return COMPOSITE_REPORTER;
    }

    @Override
    public void doReport(SzTracerSpan span) {
        for (Map.Entry<String, Reporter> entry : this.compositedReporters.entrySet()) {
            Reporter reporter = entry.getValue();
            reporter.report(span);
        }
    }
}
