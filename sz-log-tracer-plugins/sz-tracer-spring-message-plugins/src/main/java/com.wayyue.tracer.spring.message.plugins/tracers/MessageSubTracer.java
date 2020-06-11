
package com.wayyue.tracer.spring.message.plugins.tracers;


import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractServerTracer;
import com.wayyue.tracer.spring.message.plugins.encodes.MessageSubDigestEncoder;
import com.wayyue.tracer.spring.message.plugins.encodes.MessageSubDigestJsonEncoder;
import com.wayyue.tracer.spring.message.plugins.enums.SpringMessageLogEnum;
import com.wayyue.tracer.spring.message.plugins.repoters.MessageSubStatJsonReporter;
import com.wayyue.tracer.spring.message.plugins.repoters.MessageSubStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class MessageSubTracer extends AbstractServerTracer {

    private volatile static MessageSubTracer messageSubTracer = null;

    public static MessageSubTracer getMessageSubTracerSingleton() {
        if (messageSubTracer == null) {
            synchronized (MessageSubTracer.class) {
                if (messageSubTracer == null) {
                    messageSubTracer = new MessageSubTracer();
                }
            }
        }
        return messageSubTracer;
    }

    protected MessageSubTracer() {
        super(ComponentNameConstants.MSG_SUB);
    }

    @Override
    protected String getServerDigestReporterLogName() {
        return SpringMessageLogEnum.MESSAGE_SUB_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getServerDigestReporterRollingKey() {
        return SpringMessageLogEnum.MESSAGE_SUB_DIGEST.getRollingKey();
    }

    @Override
    protected String getServerDigestReporterLogNameKey() {
        return SpringMessageLogEnum.MESSAGE_SUB_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getServerDigestEncoder() {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new MessageSubDigestJsonEncoder();
        } else {
            return new MessageSubDigestEncoder();
        }
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateServerStatReporter() {
        SpringMessageLogEnum logEnum = SpringMessageLogEnum.MESSAGE_SUB_STAT;
        String statLog = logEnum.getDefaultLogName();
        String statRollingPolicy = SzTracerConfiguration
            .getRollingPolicy(logEnum.getRollingKey());
        String statLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(logEnum
            .getLogNameKey());
        return this.getStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSzTracerStatisticReporter getStatJsonReporter(String statTracerName,
                                                                      String statRollingPolicy,
                                                                      String statLogReserveConfig) {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new MessageSubStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new MessageSubStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
