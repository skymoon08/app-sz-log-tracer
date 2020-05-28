package com.wayyue.tracer.core.tracer;


import com.wayyue.tracer.core.SzTracer;
import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.reporter.digest.DiskReporterImpl;
import com.wayyue.tracer.core.reporter.facade.Reporter;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.LogData;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * AbstractTracer
 *
 */
public abstract class AbstractTracer {

    protected SzTracer szTracer;

    public AbstractTracer(String tracerType) {
        this(tracerType, true, true);
    }

    public AbstractTracer(String tracerType, boolean clientTracer, boolean serverTracer) {
        SzTracer.Builder builder = new SzTracer.Builder(tracerType);
        if (clientTracer) {
            Reporter clientReporter = this.generateReporter(this.generateClientStatReporter(),
                this.getClientDigestReporterLogName(), this.getClientDigestReporterRollingKey(),
                this.getClientDigestReporterLogNameKey(), this.getClientDigestEncoder());
            if (clientReporter != null) {
                builder.withClientReporter(clientReporter);
            }
        }
        if (serverTracer) {
            Reporter serverReporter = this.generateReporter(this.generateServerStatReporter(),
                this.getServerDigestReporterLogName(), this.getServerDigestReporterRollingKey(),
                this.getServerDigestReporterLogNameKey(), this.getServerDigestEncoder());
            if (serverReporter != null) {
                builder.withServerReporter(serverReporter);
            }
        }
        //build
        this.szTracer = builder.build();
    }

    protected Reporter generateReporter(AbstractSzTracerStatisticReporter statReporter,
                                        String logName, String logRollingKey, String logNameKey,
                                        SpanEncoder<SzTracerSpan> spanEncoder) {
        String digestRollingPolicy = SzTracerConfiguration.getRollingPolicy(logRollingKey);
        String digestLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(logNameKey);
        DiskReporterImpl reporter = new DiskReporterImpl(logName, digestRollingPolicy,
            digestLogReserveConfig, spanEncoder, statReporter, logNameKey);
        return reporter;
    }

    protected abstract String getClientDigestReporterLogName();

    protected abstract String getClientDigestReporterRollingKey();

    protected abstract String getClientDigestReporterLogNameKey();

    protected abstract SpanEncoder<SzTracerSpan> getClientDigestEncoder();

    protected abstract AbstractSzTracerStatisticReporter generateClientStatReporter();

    protected abstract String getServerDigestReporterLogName();

    protected abstract String getServerDigestReporterRollingKey();

    protected abstract String getServerDigestReporterLogNameKey();

    protected abstract SpanEncoder<SzTracerSpan> getServerDigestEncoder();

    protected abstract AbstractSzTracerStatisticReporter generateServerStatReporter();

    /**
     * Stage CS , This stage will produce a new span
     * If there is a span in the current SzTraceContext, it is the parent of the current Span
     *
     * @param operationName as span name
     * @return              a new spam
     */
    public SzTracerSpan clientSend(String operationName) {
        SzTraceContext szTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan serverSpan = szTraceContext.pop();
        SzTracerSpan clientSpan = null;
        try {
            clientSpan = (SzTracerSpan) this.szTracer.buildSpan(operationName)
                .asChildOf(serverSpan).start();
            // Need to actively cache your own serverSpan, because: asChildOf is concerned about spanContext
            clientSpan.setParentSzTracerSpan(serverSpan);
            return clientSpan;
        } catch (Throwable throwable) {
            SelfDefineLog.errorWithTraceId("Client Send Error And Restart by Root Span", throwable);
            SelfDefineLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSzTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSzTracerSpanContext().getSysBaggage();
            }
            clientSpan = this.errorRecover(bizBaggage, sysBaggage);
        } finally {
            if (clientSpan != null) {
                clientSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
                clientSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                    .getName());
                // log
                clientSpan.log(LogData.CLIENT_SEND_EVENT_VALUE);
                // Put into the thread context
                szTraceContext.push(clientSpan);
            }
        }
        return clientSpan;
    }

    /**
     *
     * Stage CR, This stage will end a span
     *
     * @param resultCode resultCode to mark success or fail
     */
    public void clientReceive(String resultCode) {
        SzTraceContext SzTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan clientSpan = SzTraceContext.pop();
        if (clientSpan == null) {
            return;
        }
        // finish and to report
        this.clientReceiveTagFinish(clientSpan, resultCode);
        // restore parent span
        if (clientSpan.getParentSzTracerSpan() != null) {
            SzTraceContext.push(clientSpan.getParentSzTracerSpan());
        }
    }

    /**
     * Span finished and append tags
     * @param clientSpan current finished span
     * @param resultCode result status code
     */
    public void clientReceiveTagFinish(SzTracerSpan clientSpan, String resultCode) {
        if (clientSpan != null) {
            // log event
            clientSpan.log(LogData.CLIENT_RECV_EVENT_VALUE);
            // set resultCode
            clientSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
            // finish client span
            clientSpan.finish();
        }
    }

    /**
     * Stage SR , This stage will produce a new span.
     *
     * For example, the SpringMVC component accepts a network request,
     * we need to create an mvc span to record related information.
     *
     * we do not care SzTracerSpanContext, just as root span
     *
     * @return SzTracerSpan
     */
    public SzTracerSpan serverReceive() {
        return this.serverReceive(null);
    }

    /**
     * server receive request
     * @param SzTracerSpanContext The context to restore
     * @return SzTracerSpan
     */
    public SzTracerSpan serverReceive(SzTracerSpanContext SzTracerSpanContext) {
        SzTracerSpan newSpan = null;
        // pop LogContext
        SzTraceContext szTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan serverSpan = szTraceContext.pop();
        try {
            if (serverSpan == null) {
                newSpan = (SzTracerSpan) this.szTracer.buildSpan(StringUtils.EMPTY_STRING).asChildOf(SzTracerSpanContext).start();
            } else {
                newSpan = (SzTracerSpan) this.szTracer.buildSpan(StringUtils.EMPTY_STRING).asChildOf(serverSpan).start();
            }
        } catch (Throwable throwable) {
            SelfDefineLog.errorWithTraceId("Middleware server received and restart root span", throwable);
            SelfDefineLog.flush();
            Map<String, String> bizBaggage = null;
            Map<String, String> sysBaggage = null;
            if (serverSpan != null) {
                bizBaggage = serverSpan.getSzTracerSpanContext().getBizBaggage();
                sysBaggage = serverSpan.getSzTracerSpanContext().getSysBaggage();
            }
            newSpan = this.errorRecover(bizBaggage, sysBaggage);
        } finally {
            if (newSpan != null) {
                // log
                newSpan.log(LogData.SERVER_RECV_EVENT_VALUE);
                // server tags
                newSpan.setTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
                newSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread().getName());
                // push to SzTraceContext
                szTraceContext.push(newSpan);
            }
        }
        return newSpan;
    }

    /**
     * Stage SS, This stage will end a span
     *
     * @param resultCode
     */
    public void serverSend(String resultCode) {
        try {
            SzTraceContext SzTraceContext = SzTraceContextHolder.getSzTraceContext();
            SzTracerSpan serverSpan = SzTraceContext.pop();
            if (serverSpan == null) {
                return;
            }
            // log
            serverSpan.log(LogData.SERVER_SEND_EVENT_VALUE);
            // resultCode
            serverSpan.setTag(CommonSpanTags.RESULT_CODE, resultCode);
            serverSpan.finish();
        } finally {
            // clear TreadLocalContext
            this.clearTreadLocalContext();
        }
    }

    protected SzTracerSpan genSeverSpanInstance(long startTime, String operationName,
                                                  SzTracerSpanContext SzTracerSpanContext,
                                                  Map<String, ?> tags) {
        return new SzTracerSpan(this.szTracer, startTime, null, operationName,
            SzTracerSpanContext, tags);
    }

    /**
     * Clean up all call context information: Note that the server can be cleaned up after receiving it;
     * the client does not have the right time to clean up (can only judge size <= 1)
     */
    private void clearTreadLocalContext() {
        SzTraceContext SzTraceContext = SzTraceContextHolder.getSzTraceContext();
        SzTraceContext.clear();
    }

    /**
     *
     * When an error occurs to remedy, start counting from the root node
     *
     * @param bizBaggage Business transparent transmission
     * @param sysBaggage System transparent transmission
     * @return root span
     */
    protected SzTracerSpan errorRecover(Map<String, String> bizBaggage,
                                          Map<String, String> sysBaggage) {
        SzTracerSpanContext spanContext = SzTracerSpanContext.rootStart();
        spanContext.addBizBaggage(bizBaggage);
        spanContext.addSysBaggage(sysBaggage);
        SzTracerSpan span = this.genSeverSpanInstance(System.currentTimeMillis(),
            StringUtils.EMPTY_STRING, spanContext, null);
        return span;
    }

    public SzTracer getSzTracer() {
        return szTracer;
    }

}