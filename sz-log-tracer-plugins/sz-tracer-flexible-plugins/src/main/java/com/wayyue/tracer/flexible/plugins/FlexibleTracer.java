package com.wayyue.tracer.flexible.plugins;

import com.wayyue.tracer.core.SzTracer;
import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.ComponentNameConstants;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.reporter.digest.DiskReporterImpl;
import com.wayyue.tracer.core.reporter.facade.Reporter;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.samplers.Sampler;
import com.wayyue.tracer.core.samplers.SamplerFactory;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.LogData;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * FlexibleTracer for process manual and @Tracer trace
 **/
public class FlexibleTracer extends SzTracer {

    private final Reporter reporter;

    /**
     * support with custom reporter and sampler
     * @param sampler
     * @param reporter
     */
    public FlexibleTracer(Sampler sampler, Reporter reporter) {
        super(ComponentNameConstants.FLEXIBLE, sampler);
        this.reporter = reporter;
    }

    /**
     * support with manual reporter with official reporter type, and the trace log will be record in biz-digest.log and biz-stat.log
     */
    public FlexibleTracer() {
        super(ComponentNameConstants.FLEXIBLE, null, null, initSampler(), null);
        this.reporter = initReporter();

    }

    private static Sampler initSampler() {
        try {
            return SamplerFactory.getSampler();
        } catch (Exception e) {
            SelfDefineLog.error("Failed to get tracer sampler strategy;");
        }
        return null;
    }

    /**
     * override default reportSpan, and also allow to extension ReportListener interface
     *
     * only when provide Reporter implement, It will begin to work
     *
     * @param span
     */
    @Override
    public void reportSpan(SzTracerSpan span) {
        if (span == null) {
            return;
        }
        // sampler is support &  current span is root span
        if (this.getSampler() != null && span.getParentSzTracerSpan() == null) {
            span.getSzTracerSpanContext().setSampled(this.getSampler().sample(span).isSampled());
        }
        //invoke listener
        invokeReportListeners(span);
        if (this.reporter != null) {
            this.reporter.report(span);
        } else {
            SelfDefineLog.warn("No reporter implement in flexible tracer");
        }
    }

    @Override
    public void close() {
        if (reporter != null) {
            reporter.close();
        }
        super.close();
    }

    public Reporter getReporter() {
        return reporter;
    }

    private Reporter initReporter() {
        String logRollingKey = FlexibleLogEnum.FLEXIBLE_DIGEST.getRollingKey();
        String logNameKey = FlexibleLogEnum.FLEXIBLE_DIGEST.getLogNameKey();
        String logName = FlexibleLogEnum.FLEXIBLE_DIGEST.getDefaultLogName();
        String digestRollingPolicy = SzTracerConfiguration.getRollingPolicy(logRollingKey);
        String digestLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(logNameKey);

        SpanEncoder spanEncoder = generateAbstractDigestSpanEncoder();

        AbstractSzTracerStatisticReporter statReporter = generateFlexibleStatJsonReporter();

        DiskReporterImpl reporter = new DiskReporterImpl(logName, digestRollingPolicy, digestLogReserveConfig, spanEncoder, statReporter, logNameKey);

        return reporter;
    }

    private SpanEncoder generateAbstractDigestSpanEncoder() {
        if (SzTracerConfiguration.isJsonOutput()) {
            return new FlexibleDigestJsonEncoder();
        } else {
            return new FlexibleDigestEncoder();
        }
    }

    private AbstractSzTracerStatisticReporter generateFlexibleStatJsonReporter() {
       FlexibleLogEnum flexibleLogEnum =FlexibleLogEnum.FLEXIBLE_STAT;
        String statLog = flexibleLogEnum.getDefaultLogName();
        String statRollingPolicy = SzTracerConfiguration.getRollingPolicy(flexibleLogEnum
            .getRollingKey());
        String statLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(flexibleLogEnum
            .getLogNameKey());

        if (SzTracerConfiguration.isJsonOutput()) {
            return new FlexibleStatJsonReporter(statLog, statRollingPolicy, statLogReserveConfig);
        } else {
            return new FlexibleStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
        }
    }

    public SzTracerSpan beforeInvoke(String operationName) {
        SzTraceContext sofaTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan serverSpan = sofaTraceContext.pop();
        SzTracerSpan methodSpan = null;
        try {
            methodSpan = (SzTracerSpan) this.buildSpan(operationName).asChildOf(serverSpan)
                .start();
            // Need to actively cache your own serverSpan, because: asChildOf is concerned about spanContext
            methodSpan.setParentSzTracerSpan(serverSpan);
        } catch (Throwable throwable) {
            SelfDefineLog.errorWithTraceId("Client Send Error And Restart by Root Span", throwable);
            SelfDefineLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSzTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSzTracerSpanContext().getSysBaggage();
            }
            methodSpan = this.errorSpan(bizBaggage, sysBaggage);
        } finally {
            if (methodSpan != null) {
                // get appName
                String appName = SzTracerConfiguration.getProperty(SzTracerConfiguration.TRACER_APPNAME_KEY);
                methodSpan.setTag(CommonSpanTags.LOCAL_APP, appName);
                methodSpan.setTag(CommonSpanTags.METHOD, operationName);
                // all as client
                methodSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
                methodSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
                methodSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                sofaTraceContext.push(methodSpan);
            }
        }

        return methodSpan;
    }

    public void afterInvoke() {
        afterInvoke(StringUtils.EMPTY_STRING);
    }

    public void afterInvoke(String error) {
        SzTraceContext sofaTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan clientSpan = sofaTraceContext.pop();
        if (clientSpan == null) {
            return;
        }

        if (StringUtils.isNotBlank(error)) {
            clientSpan.setTag(CommonSpanTags.RESULT_CODE, SzTracerConstant.RESULT_CODE_SUCCESS);
        } else {
            clientSpan.setTag(CommonSpanTags.RESULT_CODE, SzTracerConstant.RESULT_CODE_ERROR);
        }

        // log event
        clientSpan.log(LogData.CLIENT_RECV_EVENT_VALUE);
        // set resultCode
        clientSpan.setTag(Tags.ERROR.getKey(), error);
        // finish client span
        clientSpan.finish();
        // restore parent span
        if (clientSpan.getParentSzTracerSpan() != null) {
            sofaTraceContext.push(clientSpan.getParentSzTracerSpan());
        }
    }

    private SzTracerSpan errorSpan(Map<String, String> bizBaggage, Map<String, String> sysBaggage) {
        SzTracerSpanContext spanContext = SzTracerSpanContext.rootStart();
        spanContext.addBizBaggage(bizBaggage);
        spanContext.addSysBaggage(sysBaggage);
        return new SzTracerSpan(this, System.currentTimeMillis(), null, StringUtils.EMPTY_STRING,
            spanContext, null);
    }
}
