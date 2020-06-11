
package com.wayyue.tracer.spring.message.plugins.tracers;


import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractClientTracer;
import com.wayyue.tracer.spring.message.plugins.encodes.MessagePubDigestEncoder;
import com.wayyue.tracer.spring.message.plugins.encodes.MessagePubDigestJsonEncoder;
import com.wayyue.tracer.spring.message.plugins.enums.SpringMessageLogEnum;
import com.wayyue.tracer.spring.message.plugins.repoters.MessagePubStatJsonReporter;
import com.wayyue.tracer.spring.message.plugins.repoters.MessagePubStatReporter;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 10:52 AM
 * @since:
 **/
public class MessagePubTracer extends AbstractClientTracer {

    private volatile static MessagePubTracer messagePubTracer = null;

    public static MessagePubTracer getMessagePubTracerSingleton() {
        if (messagePubTracer == null) {
            synchronized (MessagePubTracer.class) {
                if (messagePubTracer == null) {
                    messagePubTracer = new MessagePubTracer();
                }
            }
        }
        return messagePubTracer;
    }

    protected MessagePubTracer() {
        super(ComponentNameConstants.MSG_PUB);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return SpringMessageLogEnum.MESSAGE_PUB_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return SpringMessageLogEnum.MESSAGE_PUB_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return SpringMessageLogEnum.MESSAGE_PUB_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getClientDigestEncoder() {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new MessagePubDigestJsonEncoder();
        } else {
            return new MessagePubDigestEncoder();
        }
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateClientStatReporter() {
        SpringMessageLogEnum logEnum = SpringMessageLogEnum.MESSAGE_PUB_STAT;
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
            return new MessagePubStatJsonReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        } else {
            return new MessagePubStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }
    }
}
