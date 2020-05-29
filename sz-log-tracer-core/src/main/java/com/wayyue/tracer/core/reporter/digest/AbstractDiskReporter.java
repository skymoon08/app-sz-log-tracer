package com.wayyue.tracer.core.reporter.digest;

import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.reporter.facade.AbstractReporter;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;

import java.util.Map;

/**
 * AbstractDiskReporter
 *
 * Abstract classes for persisting log information, including digest log persistence and stat log persistence
 */
public abstract class AbstractDiskReporter extends AbstractReporter {

    /**
     * Get Reporter instance type
     * @return
     */
    @Override
    public String getReporterType() {
        //By default, the type of the digest log is used as the type of span
        return this.getDigestReporterType();
    }

    /**
     * output span
     * @param span
     */
    @Override
    public void doReport(SzTracerSpan span) {
        //Set the log type for easy printing, otherwise it will not print correctly.
        span.setLogType(this.getDigestReporterType());
        if (!isDisableDigestLog(span)) {
            //print digest log
            this.digestReport(span);
        }
        //print stat log
        this.statisticReport(span);
    }

    /**
     * Get digest reporter instance type
     * @return
     */
    public abstract String getDigestReporterType();

    /**
     * Get stat reporter instance type
     * @return
     */
    public abstract String getStatReporterType();

    /**
     * print digest log
     * @param span span
     */
    public abstract void digestReport(SzTracerSpan span);

    /**
     * print stat log
     * @param span span
     */
    public abstract void statisticReport(SzTracerSpan span);

    protected boolean isDisableDigestLog(SzTracerSpan span) {
        if (span == null || span.context() == null) {
            return true;
        }
        SzTracerSpanContext sofaTracerSpanContext = (SzTracerSpanContext) span.context();
        // sampled is false; this span will not be report
        if (!sofaTracerSpanContext.isSampled()) {
            return true;
        }
        boolean allDisabled = Boolean.TRUE.toString().equalsIgnoreCase(
            SzTracerConfiguration.getProperty(SzTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY));
        if (allDisabled) {
            return true;
        }

        Map<String, String> disableConfiguration = SzTracerConfiguration.getMapEmptyIfNull(
                SzTracerConfiguration.DISABLE_DIGEST_LOG_KEY);
        //digest log type
        String logType = StringUtils.EMPTY_STRING + span.getLogType();
        if (StringUtils.isBlank(logType)) {
            //if the digest log type is empty, it will not be printed.
            return true;
        }
        // Rpc-2-jvm special handling, adapting rpc2jvm to close digest and only print stat
        if (SzTracerConstant.RPC_2_JVM_DIGEST_LOG_NAME.equals(logType)) {
            if (Boolean.FALSE.toString().equalsIgnoreCase(
                SzTracerConfiguration.getProperty("enable_rpc_2_jvm_digest_log"))) {
                return true;
            }
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(disableConfiguration.get(logType));
    }

}
