package com.wayue.tracer.core.reporter.digest;


import com.wayue.tracer.core.appender.TraceAppender;
import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.appender.file.LoadTestAwareAppender;
import com.wayue.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;
import com.wayue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.reporter.digest.manager.SzTracerDigestReporterAsyncManager;
import com.wayue.tracer.core.reporter.stat.SzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.AssertUtils;
import com.wayue.tracer.core.utils.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Internal customization : DiskReporterImpl
 */
public class DiskReporterImpl extends AbstractDiskReporter {

    /**
     * Identify the initial state: lazy initializes the disk file,
     * used in initialization, pay attention to concurrent initialization logic
     */
    private final AtomicBoolean isDigestFileInited = new AtomicBoolean(false);

    private final String digestLogType;

    private final String digestRollingPolicy;

    private String digestLogReserveConfig;

    private final SpanEncoder contextEncoder;

    private String logNameKey;

    /**
     * Statistical implementation, the user needs to implement a method of how to count,
     * the cumulative operation is provided by default.
     */
    private SzTracerStatisticReporter statReporter;

    public DiskReporterImpl(String digestLogType, SpanEncoder contextEncoder) {

        this(digestLogType, StringUtils.EMPTY_STRING, StringUtils.EMPTY_STRING, contextEncoder, null);
    }

    public DiskReporterImpl(String digestLogType, String digestRollingPolicy,
                            String digestLogReserveConfig, SpanEncoder contextEncoder) {

        this(digestLogType, digestRollingPolicy, digestLogReserveConfig, contextEncoder, null);
    }

    public DiskReporterImpl(String digestLogType, String digestRollingPolicy,
                            String digestLogReserveConfig, SpanEncoder contextEncoder,
                            SzTracerStatisticReporter statReporter) {
        this(digestLogType, digestRollingPolicy, digestLogReserveConfig, contextEncoder, statReporter, null);
    }

    /**
     * @param digestLogType          digestLogType:log type
     * @param digestRollingPolicy    digestRollingPolicy:digest rolling policy
     * @param digestLogReserveConfig digestLogReserveConfig:Reserved days configuration
     * @param contextEncoder         contextEncoder:Log Encoder
     * @param statReporter           statReporter:User-supplied statistical log reporter implementation
     * @param logNameKey             logNameKey:Log file configuration keyword
     */
    public DiskReporterImpl(String digestLogType, String digestRollingPolicy,
                            String digestLogReserveConfig, SpanEncoder contextEncoder,
                            SzTracerStatisticReporter statReporter, String logNameKey) {
        AssertUtils.hasText(digestLogType, "digestLogType can't be empty");
        this.digestLogType = digestLogType;
        this.digestRollingPolicy = digestRollingPolicy;
        this.digestLogReserveConfig = digestLogReserveConfig;
        this.contextEncoder = contextEncoder;
        this.statReporter = statReporter;
        this.logNameKey = logNameKey;
    }

    public SzTracerStatisticReporter getStatReporter() {
        return statReporter;
    }

    public void setStatReporter(SzTracerStatisticReporter statReporter) {
        this.statReporter = statReporter;
    }

    @Override
    public String getDigestReporterType() {
        return this.digestLogType;
    }

    @Override
    public String getStatReporterType() {
        if (statReporter != null) {
            //get log file name
            return statReporter.getStatTracerName();
        }
        return StringUtils.EMPTY_STRING;
    }

    @Override
    public void digestReport(SzTracerSpan span) {
        //lazy initialization
        if (!this.isDigestFileInited.get()) {
            this.initDigestFile();
        }
        AsyncCommonDigestAppenderManager asyncDigestManager = SzTracerDigestReporterAsyncManager.getSzTracerDigestReporterAsyncManager();
        if (asyncDigestManager.isAppenderAndEncoderExist(this.digestLogType)) {
            //Print only when appender and encoder are present
            asyncDigestManager.append(span);
        } else {
            SelfDefineLog.warn(span.toString() + " have no logType set, so ignore data persistence.");
        }
    }

    @Override
    public void statisticReport(SzTracerSpan span) {
        if (this.statReporter != null) {
            this.statReporter.reportStat(span);
        }
    }

    public AtomicBoolean getIsDigestFileInited() {
        return isDigestFileInited;
    }

    public String getDigestLogType() {
        return digestLogType;
    }

    public String getDigestRollingPolicy() {
        return digestRollingPolicy;
    }

    public String getDigestLogReserveConfig() {
        return digestLogReserveConfig;
    }

    public SpanEncoder getContextEncoder() {
        return contextEncoder;
    }

    public String getLogNameKey() {
        return logNameKey;
    }

    /**
     * Disk file initialization is completed
     */
    private synchronized void initDigestFile() {
        if (this.isDigestFileInited.get()) {
            //double check init
            return;
        }
        if (StringUtils.isNotBlank(logNameKey)) {
            String currentDigestLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(logNameKey);
            if (!currentDigestLogReserveConfig.equals(digestLogReserveConfig)) {
                SelfDefineLog.info("the lognamekey : " + logNameKey + " take effect. the old logreserveconfig is "
                        + digestLogReserveConfig + " and " + "the new logreverseconfig is " + currentDigestLogReserveConfig);
                digestLogReserveConfig = currentDigestLogReserveConfig;
            }
        }
        TraceAppender digestTraceAppender = LoadTestAwareAppender
                .createLoadTestAwareTimedRollingFileAppender(this.digestLogType, this.digestRollingPolicy, this.digestLogReserveConfig);
        //registry digest
        AsyncCommonDigestAppenderManager asyncDigestManager = SzTracerDigestReporterAsyncManager.getSzTracerDigestReporterAsyncManager();
        if (!asyncDigestManager.isAppenderAndEncoderExist(this.digestLogType)) {
            asyncDigestManager.addAppender(this.digestLogType, digestTraceAppender, this.contextEncoder);
        }
        //Already exists or created for the first time
        this.isDigestFileInited.set(true);
    }
}
