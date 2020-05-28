package com.wayyue.tracer.core.registry;

import com.wayyue.tracer.core.SzTracer;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.propagation.TextMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractTextB3Formatter implements RegistryExtractorInjector<TextMap> {
    /**
     * 128/64 bit traceId lower-hex string (required)
     */
    public static final String TRACE_ID_KEY_HEAD       = "X-B3-TraceId";
    /**
     * 64 bit spanId lower-hex string (required)
     */
    public static final String SPAN_ID_KEY_HEAD        = "X-B3-SpanId";
    /**
     * 64 bit parentSpanId lower-hex string (absent on root span)
     */
    public static final String PARENT_SPAN_ID_KEY_HEAD = "X-B3-ParentSpanId";
    /**
     * "1" means report this span to the tracing system, "0" means do not. (absent means defer the
     * decision to the receiver of this header).
     */
    public static final String SAMPLED_KEY_HEAD        = "X-B3-Sampled";
    /**
     * "1" implies sampled and is a request to override collection-tier sampling policy.
     */
    public static final String FLAGS_KEY_HEAD          = "X-B3-Flags";
    /**
     * Baggage items prefix
     */
    public static final String BAGGAGE_KEY_PREFIX      = "baggage-";
    /**
     * System Baggage items prefix
     */
    public static final String BAGGAGE_SYS_KEY_PREFIX  = "baggage-sys-";

    @Override
    public SzTracerSpanContext extract(TextMap carrier) {
        if (carrier == null) {
            //There not have tracing propagation head,start root span
            return SzTracerSpanContext.rootStart();
        }

        String traceId = null;
        String spanId = null;
        String parentId = null;
        boolean sampled = false;
        boolean isGetSampled = false;
        //sysBaggage
        Map<String, String> sysBaggage = new ConcurrentHashMap<String, String>();
        //bizBaggage
        Map<String, String> bizBaggage = new ConcurrentHashMap<String, String>();

        //Get others trace context items, the first value wins.
        for (Map.Entry<String, String> entry : carrier) {
            String key = entry.getKey();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (traceId == null && TRACE_ID_KEY_HEAD.equalsIgnoreCase(key)) {
                traceId = decodedValue(entry.getValue());
            }
            if (spanId == null && SPAN_ID_KEY_HEAD.equalsIgnoreCase(key)) {
                spanId = decodedValue(entry.getValue());
            }
            if (parentId == null && PARENT_SPAN_ID_KEY_HEAD.equalsIgnoreCase(key)) {
                parentId = decodedValue(entry.getValue());
            }
            if (!isGetSampled && SAMPLED_KEY_HEAD.equalsIgnoreCase(key)) {
                String valueTmp = decodedValue(entry.getValue());
                if ("1".equals(valueTmp)) {
                    sampled = true;
                } else if ("0".equals(valueTmp)) {
                    sampled = false;
                } else {
                    sampled = Boolean.parseBoolean(valueTmp);
                }
                isGetSampled = true;
            }
            if (key.indexOf(BAGGAGE_SYS_KEY_PREFIX) == 0) {
                String keyTmp = StringUtils.unescapeEqualAndPercent(key).substring(
                    BAGGAGE_SYS_KEY_PREFIX.length());
                String valueTmp = StringUtils
                    .unescapeEqualAndPercent(decodedValue(entry.getValue()));
                sysBaggage.put(keyTmp, valueTmp);
            }
            if (key.indexOf(BAGGAGE_KEY_PREFIX) == 0) {
                String keyTmp = StringUtils.unescapeEqualAndPercent(key).substring(
                    BAGGAGE_KEY_PREFIX.length());
                String valueTmp = StringUtils
                    .unescapeEqualAndPercent(decodedValue(entry.getValue()));
                bizBaggage.put(keyTmp, valueTmp);
            }
        }

        if (traceId == null) {
            //There not have trace id, assumed not have tracing propagation head also,start root span
            return SzTracerSpanContext.rootStart();
        }

        if (spanId == null) {
            spanId = SzTracer.ROOT_SPAN_ID;
        }
        if (parentId == null) {
            parentId = StringUtils.EMPTY_STRING;
        }
        SzTracerSpanContext SzTracerSpanContext = new SzTracerSpanContext(traceId, spanId,
            parentId, sampled);
        if (sysBaggage.size() > 0) {
            SzTracerSpanContext.addSysBaggage(sysBaggage);
        }
        if (bizBaggage.size() > 0) {
            SzTracerSpanContext.addBizBaggage(bizBaggage);
        }

        return SzTracerSpanContext;
    }

    @Override
    public void inject(SzTracerSpanContext spanContext, TextMap carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        //Tracing Context
        carrier.put(TRACE_ID_KEY_HEAD, encodedValue(spanContext.getTraceId()));
        carrier.put(SPAN_ID_KEY_HEAD, encodedValue(spanContext.getSpanId()));
        carrier.put(PARENT_SPAN_ID_KEY_HEAD, encodedValue(spanContext.getParentId()));
        carrier.put(SPAN_ID_KEY_HEAD, encodedValue(spanContext.getSpanId()));
        carrier.put(SAMPLED_KEY_HEAD, encodedValue(String.valueOf(spanContext.isSampled())));
        //System Baggage items
        for (Map.Entry<String, String> entry : spanContext.getSysBaggage().entrySet()) {
            String key = BAGGAGE_SYS_KEY_PREFIX + StringUtils.escapePercentEqualAnd(entry.getKey());
            String value = encodedValue(StringUtils.escapePercentEqualAnd(entry.getValue()));
            carrier.put(key, value);
        }
        //Business Baggage items
        for (Map.Entry<String, String> entry : spanContext.getBizBaggage().entrySet()) {
            String key = BAGGAGE_KEY_PREFIX + StringUtils.escapePercentEqualAnd(entry.getKey());
            String value = encodedValue(StringUtils.escapePercentEqualAnd(entry.getValue()));
            carrier.put(key, value);
        }
    }

    /**
     * encode string
     * @param value string will be encoded
     * @return  encoded value
     */
    protected abstract String encodedValue(String value);

    /**
     * decode string
     * @param value string will be decoded
     * @return decoded value
     */
    protected abstract String decodedValue(String value);
}
