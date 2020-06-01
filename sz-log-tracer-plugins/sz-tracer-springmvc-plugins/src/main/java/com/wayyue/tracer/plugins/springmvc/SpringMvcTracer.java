package com.wayyue.tracer.plugins.springmvc;


import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.ComponentNameConstants;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.tracer.AbstractServerTracer;
import com.wayyue.tracer.plugins.springmvc.mvcencoder.DigestEncoder;
import com.wayyue.tracer.plugins.springmvc.mvcencoder.DigestJsonEncoder;

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
        return generateSofaMvcStatReporter();
    }

    private SpringMvcStatReporter generateSofaMvcStatReporter() {
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