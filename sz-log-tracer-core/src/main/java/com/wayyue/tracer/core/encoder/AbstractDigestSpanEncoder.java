package com.wayyue.tracer.core.encoder;


import com.wayyue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayyue.tracer.core.appender.builder.XStringBuilder;
import com.wayyue.tracer.core.appender.encoder.SpanEncoder;
import com.wayyue.tracer.core.appender.sefllog.Timestamp;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import io.opentracing.tag.Tags;

import java.io.IOException;
import java.util.Map;


public abstract class AbstractDigestSpanEncoder implements SpanEncoder<SzTracerSpan> {

    @Override
    public String encode(SzTracerSpan span) throws IOException {
        if ("false".equalsIgnoreCase(SzTracerConfiguration.getProperty(SzTracerConfiguration.JSON_FORMAT_OUTPUT))) {
            return encodeXsbSpan(span);
        } else {
            return encodeJsbSpan(span);
        }
    }

    /**
     * encodeJsbSpan
     * @param span
     * @return
     */
    private String encodeJsbSpan(SzTracerSpan span) {
        JsonStringBuilder jsb = new JsonStringBuilder();
        // common tag
        appendJsonCommonSlot(jsb, span);
        // component tag
        appendComponentSlot(null, jsb, span);
        // baggage
        jsb.append(CommonSpanTags.SYS_BAGGAGE, baggageSystemSerialized(span.getSzTracerSpanContext()));
        jsb.appendEnd(CommonSpanTags.BIZ_BAGGAGE, baggageSerialized(span.getSzTracerSpanContext()));
        return jsb.toString();
    }

    /**
     * encodeXsbSpan
     * @param span
     * @return
     */
    private String encodeXsbSpan(SzTracerSpan span) {
        XStringBuilder xsb = new XStringBuilder();
        // common tag
        appendXsbCommonSlot(xsb, span);
        // component tag
        appendComponentSlot(xsb, null, span);
        // sys baggage
        xsb.append(baggageSystemSerialized(span.getSzTracerSpanContext()));
        // biz baggage
        xsb.appendEnd(baggageSerialized(span.getSzTracerSpanContext()));
        return xsb.toString();
    }

    /**
     * override by sub class
     * @param xsb
     * @param jsb
     * @param span
     */
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {
    }

    /**
     * System transparent transmission of data
     * @param spanContext span context
     * @return String
     */
    protected String baggageSystemSerialized(SzTracerSpanContext spanContext) {
        return spanContext.getSysSerializedBaggage();
    }

    /**
     * Business transparent transmission of data
     * @param spanContext span context
     * @return
     */
    protected String baggageSerialized(SzTracerSpanContext spanContext) {
        return spanContext.getBizSerializedBaggage();
    }

    /**
     * common tag to json format
     * @param jsb
     * @param span
     */
    protected void appendJsonCommonSlot(JsonStringBuilder jsb, SzTracerSpan span) {
        SzTracerSpanContext context = span.getSzTracerSpanContext();
        Map<String, String> tagWithStr = span.getTagsWithStr();
        //span end time
        jsb.appendBegin(CommonSpanTags.TIME, Timestamp.format(span.getEndTime()));
        //app
        jsb.append(CommonSpanTags.LOCAL_APP, tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        jsb.append(CommonSpanTags.TRACE_ID, context.getTraceId());
        //SpanId
        jsb.append(CommonSpanTags.SPAN_ID, context.getSpanId());
        //Span Kind
        jsb.append(Tags.SPAN_KIND.getKey(), tagWithStr.get(Tags.SPAN_KIND.getKey()));
        // result code
        jsb.append(CommonSpanTags.RESULT_CODE, tagWithStr.get(CommonSpanTags.RESULT_CODE));
        // thread name
        jsb.append(CommonSpanTags.CURRENT_THREAD_NAME,
            tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        // time.cost.milliseconds
        jsb.append(CommonSpanTags.TIME_COST_MILLISECONDS, (span.getEndTime() - span.getStartTime()) + SzTracerConstant.MS);
    }

    /**
     *  common tag to XStringBuilder format
     * @param xsb
     * @param span
     */
    protected void appendXsbCommonSlot(XStringBuilder xsb, SzTracerSpan span) {
        SzTracerSpanContext context = span.getSzTracerSpanContext();
        Map<String, String> tagWithStr = span.getTagsWithStr();
        //span end time
        xsb.append(Timestamp.format(span.getEndTime()));
        //appName
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_APP));
        //TraceId
        xsb.append(context.getTraceId());
        //RpcId
        xsb.append(context.getSpanId());
        //span kind
        xsb.append(tagWithStr.get(Tags.SPAN_KIND.getKey()));
        // result code
        xsb.append(tagWithStr.get(CommonSpanTags.RESULT_CODE));
        // thread name
        xsb.append(tagWithStr.get(CommonSpanTags.CURRENT_THREAD_NAME));
        // time.cost.milliseconds
        xsb.append((span.getEndTime() - span.getStartTime()) + SzTracerConstant.MS);
    }
}