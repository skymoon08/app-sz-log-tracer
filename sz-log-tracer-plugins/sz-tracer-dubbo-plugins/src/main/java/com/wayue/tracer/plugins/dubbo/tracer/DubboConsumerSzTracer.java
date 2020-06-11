package com.wayue.tracer.plugins.dubbo.tracer;

import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractClientTracer;
import com.wayue.tracer.plugins.dubbo.encoder.DubboClientDigestEncoder;
import com.wayue.tracer.plugins.dubbo.encoder.DubboClientDigestJsonEncoder;
import com.wayue.tracer.plugins.dubbo.enums.DubboLogEnum;
import com.wayue.tracer.plugins.dubbo.stat.DubboClientStatJsonReporter;
import com.wayue.tracer.plugins.dubbo.stat.DubboClientStatReporter;

public class DubboConsumerSzTracer extends AbstractClientTracer {

    private volatile static DubboConsumerSzTracer dubboConsumerSzTracer = null;

    public static DubboConsumerSzTracer getDubboConsumerSzTracerSingleton() {
        if (dubboConsumerSzTracer == null) {
            synchronized (DubboConsumerSzTracer.class) {
                if (dubboConsumerSzTracer == null) {
                    dubboConsumerSzTracer = new DubboConsumerSzTracer(
                        ComponentNameConstants.DUBBO_CLIENT);
                }
            }
        }
        return dubboConsumerSzTracer;
    }

    public DubboConsumerSzTracer(String tracerType) {
        super(tracerType);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return DubboLogEnum.DUBBO_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return DubboLogEnum.DUBBO_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return DubboLogEnum.DUBBO_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getClientDigestEncoder() {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new DubboClientDigestJsonEncoder();
        } else {
            return new DubboClientDigestEncoder();
        }
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateClientStatReporter() {
        DubboLogEnum dubboClientStat = DubboLogEnum.DUBBO_CLIENT_STAT;
        String statLog = dubboClientStat.getDefaultLogName();
        String statRollingPolicy = SzTracerConfiguration.getRollingPolicy(dubboClientStat.getRollingKey());
        String statLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(dubboClientStat.getLogNameKey());
        if (SzTracerConfiguration.isJsonOutput()) {
            return new DubboClientStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new DubboClientStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
