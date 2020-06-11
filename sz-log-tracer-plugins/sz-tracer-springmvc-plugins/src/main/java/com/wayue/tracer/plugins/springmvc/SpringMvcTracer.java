package com.wayue.tracer.plugins.springmvc;


import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractServerTracer;
import com.wayue.tracer.plugins.springmvc.mvcencoder.DigestEncoder;
import com.wayue.tracer.plugins.springmvc.mvcencoder.DigestJsonEncoder;

/**
 * SpringMVCTracer
 * SpringMVC
 */
public class SpringMvcTracer extends AbstractServerTracer {

    private volatile static SpringMvcTracer springMvcTracer = null;

    /***
     * Spring MVC Tracer Singleton
     * @return singleton
     */
    public static SpringMvcTracer getSpringMvcTracerSingleton() {
        if (springMvcTracer == null) {
            synchronized (SpringMvcTracer.class) {
                if (springMvcTracer == null) {
                    springMvcTracer = new SpringMvcTracer();
                }
            }
        }
        return springMvcTracer;
    }

    private SpringMvcTracer() {
        super(ComponentNameConstants.SPRING_MVC);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return SpringMvcLogEnum.SPRING_MVC_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return SpringMvcLogEnum.SPRING_MVC_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return SpringMvcLogEnum.SPRING_MVC_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getServerDigestEncoder() {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new DigestJsonEncoder();
        } else {
            return new DigestEncoder();
        }
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateServerStatReporter() {
        return generateSzMvcStatReporter();
    }

    private SpringMvcStatReporter generateSzMvcStatReporter() {
        SpringMvcLogEnum springMvcLogEnum = SpringMvcLogEnum.SPRING_MVC_STAT;
        String statLog = springMvcLogEnum.getDefaultLogName();
        String statRollingPolicy = SzTracerConfiguration.getRollingPolicy(springMvcLogEnum.getRollingKey());
        String statLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(springMvcLogEnum.getLogNameKey());
        if (SzTracerConfiguration.isJsonOutput()) {
            return new SpringMvcJsonStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new SpringMvcStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}