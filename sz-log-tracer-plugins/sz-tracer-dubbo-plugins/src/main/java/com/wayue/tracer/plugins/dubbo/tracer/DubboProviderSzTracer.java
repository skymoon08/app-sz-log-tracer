package com.wayue.tracer.plugins.dubbo.tracer;

import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractServerTracer;
import com.wayue.tracer.plugins.dubbo.encoder.DubboServerDigestEncoder;
import com.wayue.tracer.plugins.dubbo.encoder.DubboServerDigestJsonEncoder;
import com.wayue.tracer.plugins.dubbo.enums.DubboLogEnum;
import com.wayue.tracer.plugins.dubbo.stat.DubboServerStatJsonReporter;
import com.wayue.tracer.plugins.dubbo.stat.DubboServerStatReporter;

public class DubboProviderSzTracer extends AbstractServerTracer {

    private volatile static DubboProviderSzTracer dubboProviderSzTracer = null;

    public static DubboProviderSzTracer getDubboProviderSzTracerSingleton() {
        if (dubboProviderSzTracer == null) {
            synchronized (DubboProviderSzTracer.class) {
                if (dubboProviderSzTracer == null) {
                    dubboProviderSzTracer = new DubboProviderSzTracer(ComponentNameConstants.DUBBO_SERVER);
                }
            }
        }
        return dubboProviderSzTracer;
    }

    public DubboProviderSzTracer(String tracerType) {
        super(tracerType);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return DubboLogEnum.DUBBO_SERVER_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return DubboLogEnum.DUBBO_SERVER_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return DubboLogEnum.DUBBO_SERVER_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getServerDigestEncoder() {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new DubboServerDigestJsonEncoder();
        } else {
            return new DubboServerDigestEncoder();
        }
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateServerStatReporter() {
        DubboLogEnum dubboClientStat = DubboLogEnum.DUBBO_SERVER_STAT;
        String statLog = dubboClientStat.getDefaultLogName();
        String statRollingPolicy = SzTracerConfiguration.getRollingPolicy(dubboClientStat.getRollingKey());
        String statLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(dubboClientStat.getLogNameKey());
        if (SzTracerConfiguration.isJsonOutput()) {
            return new DubboServerStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new DubboServerStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }
}
