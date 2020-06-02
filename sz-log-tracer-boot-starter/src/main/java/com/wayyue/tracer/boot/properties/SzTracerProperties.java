package com.wayyue.tracer.boot.properties;


import com.wayyue.tracer.core.appender.file.TimedRollingFileAppender;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.reporter.stat.manager.SzTracerStatisticReporterManager;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

import static com.wayyue.tracer.core.configuration.SzTracerConfiguration.DEFAULT_LOG_RESERVE_DAY;


/**
 * OpenTracingSpringMvcProperties
 */
@ConfigurationProperties(SzTracerProperties.SZ_TRACER_CONFIGURATION_PREFIX)
public class SzTracerProperties {

    public final static String SZ_TRACER_CONFIGURATION_PREFIX = "com.wayyue.tracer";

    /***
     * com.wayyue.tracer.disableDigestLog Configure disable all digest
     *
     */
    private String disableDigestLog = "false";

    /***
     *  com.wayyue.tracer.disableConfiguration[logType]=true
     *  disable custom log digest
     */
    private Map<String, String> disableConfiguration = new HashMap<String, String>();

    /***
     * com.wayyue.tracer.tracerGlobalRollingPolicy=.yyyy-MM-dd
     */
    private String tracerGlobalRollingPolicy = TimedRollingFileAppender.DAILY_ROLLING_PATTERN;

    /***
     * com.wayyue.tracer.tracerGlobalLogReserveDay
     */
    private String tracerGlobalLogReserveDay = String.valueOf(DEFAULT_LOG_RESERVE_DAY);

    /***
     * com.wayyue.tracer.statLogInterval
     */
    private String statLogInterval = String
            .valueOf(SzTracerStatisticReporterManager.DEFAULT_CYCLE_SECONDS);

    /***
     *  com.wayyue.tracer.baggageMaxLength
     */
    private String baggageMaxLength = String
            .valueOf(SzTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD);

    private String samplerName;
    private float samplerPercentage = 100;
    private String samplerCustomRuleClassName;

    private String reporterName;

    /**
     * json output : com.wayyue.tracer.jsonOutput=true,
     */
    private boolean jsonOutput = true;

    public String getDisableDigestLog() {
        return disableDigestLog;
    }

    public void setDisableDigestLog(String disableDigestLog) {
        this.disableDigestLog = disableDigestLog;
    }

    public Map<String, String> getDisableConfiguration() {
        return disableConfiguration;
    }

    public void setDisableConfiguration(Map<String, String> disableConfiguration) {
        this.disableConfiguration = disableConfiguration;
    }

    public String getTracerGlobalRollingPolicy() {
        return tracerGlobalRollingPolicy;
    }

    public void setTracerGlobalRollingPolicy(String tracerGlobalRollingPolicy) {
        this.tracerGlobalRollingPolicy = tracerGlobalRollingPolicy;
    }

    public String getTracerGlobalLogReserveDay() {
        return tracerGlobalLogReserveDay;
    }

    public void setTracerGlobalLogReserveDay(String tracerGlobalLogReserveDay) {
        this.tracerGlobalLogReserveDay = tracerGlobalLogReserveDay;
    }

    public String getStatLogInterval() {
        return statLogInterval;
    }

    public void setStatLogInterval(String statLogInterval) {
        this.statLogInterval = statLogInterval;
    }

    public String getBaggageMaxLength() {
        return baggageMaxLength;
    }

    public void setBaggageMaxLength(String baggageMaxLength) {
        this.baggageMaxLength = baggageMaxLength;
    }

    public String getSamplerName() {
        return samplerName;
    }

    public void setSamplerName(String samplerName) {
        this.samplerName = samplerName;
    }

    public float getSamplerPercentage() {
        return samplerPercentage;
    }

    public void setSamplerPercentage(float samplerPercentage) {
        this.samplerPercentage = samplerPercentage;
    }

    public String getSamplerCustomRuleClassName() {
        return samplerCustomRuleClassName;
    }

    public void setSamplerCustomRuleClassName(String samplerCustomRuleClassName) {
        this.samplerCustomRuleClassName = samplerCustomRuleClassName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }

    public void setJsonOutput(boolean jsonOutput) {
        this.jsonOutput = jsonOutput;
    }
}