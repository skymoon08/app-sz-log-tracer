package com.wayyue.tracer.core.context.span;

import com.wayyue.tracer.core.SzTracer;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.generator.TraceIdGenerator;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.core.utils.TracerUtils;
import io.opentracing.SpanContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SzTracerSpanContext implements SpanContext {

    //spanId separator
    public static final String RPC_ID_SEPARATOR = ".";

    //======================== The following is the key for serializing data ========================

    private static final String TRACE_ID_KET = "tcid";

    private static final String SPAN_ID_KET = "spid";

    private static final String PARENT_SPAN_ID_KET = "pspid";

    private static final String SAMPLE_KET = "sample";

    /**
     * The serialization system transparently passes the prefix of the attribute key
     */
    private static final String SYS_BAGGAGE_PREFIX_KEY = "_sys_";

    private String traceId = StringUtils.EMPTY_STRING;

    private String spanId = StringUtils.EMPTY_STRING;

    private String parentId = StringUtils.EMPTY_STRING;

    /**
     * Default will not be sampled
     */
    private boolean isSampled = false;

    /**
     * The system transparently transmits data,
     * mainly refers to the transparent transmission data of the system dimension.
     * Note that this field cannot be used for transparent transmission of business.
     */
    private final Map<String, String> sysBaggage = new ConcurrentHashMap<String, String>();

    /**
     * Transparent transmission of data, mainly refers to the transparent transmission data of the business
     */
    private final Map<String, String> bizBaggage = new ConcurrentHashMap<String, String>();

    /**
     * sub-context counter
     */
    private AtomicInteger childContextIndex = new AtomicInteger(0);

    /**
     * clone a SzTracerSpanContext instance
     *
     * @return
     */
    public SzTracerSpanContext cloneInstance() {
        SzTracerSpanContext spanContext = new SzTracerSpanContext(this.traceId, this.spanId, this.parentId, this.isSampled);
        spanContext.addSysBaggage(this.sysBaggage);
        spanContext.addBizBaggage(this.bizBaggage);
        spanContext.childContextIndex = this.childContextIndex;
        return spanContext;
    }

    public SzTracerSpanContext() {
        //Default will not be sampled
        this(StringUtils.EMPTY_STRING, StringUtils.EMPTY_STRING, null, false);
    }

    public SzTracerSpanContext(String traceId, String spanId) {
        //Default will not be sampled
        this(traceId, spanId, null, false);
    }

    public SzTracerSpanContext(String traceId, String spanId, String parentId) {
        //Default will not be sampled
        this(traceId, spanId, parentId, false);
    }

    public SzTracerSpanContext(String traceId, String spanId, String parentId, boolean isSampled) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = StringUtils.isBlank(parentId) ? this.genParentSpanId(spanId) : parentId;
        this.isSampled = isSampled;
    }

    public SzTracerSpanContext addBizBaggage(Map<String, String> bizBaggage) {
        if (bizBaggage != null && bizBaggage.size() > 0) {
            this.bizBaggage.putAll(bizBaggage);
        }
        return this;
    }

    public SzTracerSpanContext addSysBaggage(Map<String, String> sysBaggage) {
        if (sysBaggage != null && sysBaggage.size() > 0) {
            this.sysBaggage.putAll(sysBaggage);
        }
        return this;
    }

    /**
     * return both system and business baggage
     *
     * @return Iterable
     */
    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        Map<String, String> allBaggage = new HashMap<String, String>();
        if (this.bizBaggage != null && this.bizBaggage.size() > 0) {
            allBaggage.putAll(this.bizBaggage);
        }
        if (this.sysBaggage != null && this.sysBaggage.size() > 0) {
            allBaggage.putAll(this.sysBaggage);
        }
        return allBaggage.entrySet();
    }

    /**
     * return key information string for SzTracerSpanContext
     *
     * @return
     */
    private String contextAsString() {
        return String.format("%s:%s:%s:%s", traceId, spanId, parentId, isSampled);
    }

    /**
     * Serialize the Penetration property in Tracer into a String
     * <p>
     * This method is generally used internally by Tracer or directly integrated with Tracer.
     * </p>
     *
     * @return
     */
    public String getBizSerializedBaggage() {
        return StringUtils.mapToString(bizBaggage);
    }

    public String getSysSerializedBaggage() {
        return StringUtils.mapToString(this.sysBaggage);
    }

    /**
     * deserialize string to map
     *
     * <p>
     * This method is generally used internally by Tracer or directly integrated with Tracer.
     * </p>
     *
     * @param bizBaggageAttrs serialized penetration properties
     */
    public void deserializeBizBaggage(String bizBaggageAttrs) {
        StringUtils.stringToMap(bizBaggageAttrs, this.bizBaggage);

        if (StringUtils.isNotBlank(bizBaggageAttrs)) {
            if (bizBaggageAttrs.length() > TracerUtils.getBaggageMaxLength() / 2) {
                SelfDefineLog.infoWithTraceId("Get biz baggage from upstream system, and the length is "
                        + bizBaggageAttrs.length());
            }
        }
    }

    public void deserializeSysBaggage(String sysBaggageAttrs) {
        StringUtils.stringToMap(sysBaggageAttrs, this.sysBaggage);
        if (StringUtils.isNotBlank(sysBaggageAttrs)) {
            if (sysBaggageAttrs.length() > TracerUtils.getSysBaggageMaxLength() / 2) {
                SelfDefineLog.infoWithTraceId("Get system baggage from upstream system, and the length is " + sysBaggageAttrs.length());
            }
        }
    }

    /**
     * Serialize the SpanContext to a string that is reciprocal to {@link SzTracerSpanContext#deserializeFromString}
     *
     * @return Serialized string, format: tcid:0,spid:1
     */
    public String serializeSpanContext() {
        StringBuilder serializedValue = new StringBuilder();
        serializedValue.append(TRACE_ID_KET).append(StringUtils.EQUAL).append(traceId).append(StringUtils.AND);
        serializedValue.append(SPAN_ID_KET).append(StringUtils.EQUAL).append(spanId).append(StringUtils.AND);
        serializedValue.append(PARENT_SPAN_ID_KET).append(StringUtils.EQUAL).append(parentId).append(StringUtils.AND);
        serializedValue.append(SAMPLE_KET).append(StringUtils.EQUAL).append(isSampled).append(StringUtils.AND);
        //system bizBaggage
        if (this.sysBaggage.size() > 0) {
            serializedValue.append(StringUtils.mapToStringWithPrefix(this.sysBaggage, SYS_BAGGAGE_PREFIX_KEY));
        }
        //bizBaggage
        if (this.bizBaggage.size() > 0) {
            serializedValue.append(StringUtils.mapToString(bizBaggage));
        }
        return serializedValue.toString();
    }

    /**
     * Deserialize and restore a SzTracerSpanContext, reciprocal with {@link SzTracerSpanContext#serializeSpanContext()}
     *
     * @param deserializeValue deserialize string, format: tcid:0,spid:1
     * @return SzTracerSpanContext
     */
    public static SzTracerSpanContext deserializeFromString(String deserializeValue) {
        if (StringUtils.isBlank(deserializeValue)) {
            return SzTracerSpanContext.rootStart();
        }
        //default value for SzTracerSpanContext
        // TODO: 2020/6/2 traceId生成
        String traceId = TraceIdGenerator.generate();
        String spanId = SzTracer.ROOT_SPAN_ID;
        String parentId = StringUtils.EMPTY_STRING;
        //sampled default is false
        boolean sampled = false;
        //sys bizBaggage
        Map<String, String> sysBaggage = new HashMap<String, String>();
        //bizBaggage
        Map<String, String> baggage = new HashMap<String, String>();

        Map<String, String> spanContext = new HashMap<String, String>();
        StringUtils.stringToMap(deserializeValue, spanContext);

        for (Map.Entry<String, String> entry : spanContext.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
                continue;
            }
            if (TRACE_ID_KET.equals(key)) {
                traceId = value;
                continue;
            }
            if (SPAN_ID_KET.equals(key)) {
                spanId = value;
                continue;
            }
            if (PARENT_SPAN_ID_KET.equals(key)) {
                parentId = value;
                continue;
            }
            if (SAMPLE_KET.equals(key)) {
                sampled = Boolean.parseBoolean(value);
                continue;
            }
            int sysIndex = key.indexOf(SYS_BAGGAGE_PREFIX_KEY);
            if (sysIndex == 0) {
                //must have a prefix
                String sysKey = key.substring(SYS_BAGGAGE_PREFIX_KEY.length());
                sysBaggage.put(sysKey, value);
            } else {
                //bizBaggage
                baggage.put(key, value);
            }
        }
        SzTracerSpanContext SzTracerSpanContext = new SzTracerSpanContext(traceId, spanId, parentId, sampled);
        if (sysBaggage.size() > 0) {
            SzTracerSpanContext.addSysBaggage(sysBaggage);
        }
        if (baggage.size() > 0) {
            SzTracerSpanContext.addBizBaggage(baggage);
        }
        return SzTracerSpanContext;
    }

    /**
     * As root start ,it will be return a new SzTracerSpanContext
     * <p>
     * Note:1.Leave this interface, do not dock the specific tracer implementation, mainly to remedy when an exception occurs in serialization or deserialization
     * 2.This method cannot be called at will, the correct entry should be {@link SzTracer.SzTracerSpanBuilder#createRootSpanContext()} ()}
     *
     * @return root node
     */
    public static SzTracerSpanContext rootStart() {
        return rootStart(false);
    }

    public static SzTracerSpanContext rootStart(boolean isSampled) {
        //create traceId
        String traceId = TraceIdGenerator.generate();
        return new SzTracerSpanContext(traceId, SzTracer.ROOT_SPAN_ID, StringUtils.EMPTY_STRING, isSampled);
    }

    private String genParentSpanId(String spanId) {
        return (StringUtils.isBlank(spanId) || spanId.lastIndexOf(RPC_ID_SEPARATOR) < 0) ?
                StringUtils.EMPTY_STRING : spanId.substring(0, spanId.lastIndexOf(RPC_ID_SEPARATOR));
    }

    /**
     * Allow to set traceId
     *
     * @param traceId traceId
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * Allow to set spanId
     *
     * @param spanId spanId
     */
    public void setSpanId(String spanId) {
        this.spanId = spanId;
        this.parentId = this.genParentSpanId(spanId);
    }

    public SzTracerSpanContext setBizBaggageItem(String key, String value) {
        if (StringUtils.isBlank(key)) {
            return this;
        }
        this.bizBaggage.put(key, value);
        return this;
    }

    public String getBizBaggageItem(String key) {
        return this.bizBaggage.get(key);
    }

    public SzTracerSpanContext setSysBaggageItem(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return this;
        }
        this.sysBaggage.put(key, value);
        return this;
    }

    public String getSysBaggageItem(String key) {
        return this.sysBaggage.get(key);
    }

    public String getTraceId() {
        return StringUtils.isBlank(this.traceId) ? StringUtils.EMPTY_STRING : traceId;
    }

    public String getSpanId() {
        return StringUtils.isBlank(spanId) ? StringUtils.EMPTY_STRING : spanId;
    }

    public String getParentId() {
        return StringUtils.isBlank(parentId) ? StringUtils.EMPTY_STRING : parentId;
    }

    public Map<String, String> getBizBaggage() {
        return bizBaggage;
    }

    public Map<String, String> getSysBaggage() {
        return sysBaggage;
    }

    public boolean isSampled() {
        return isSampled;
    }

    public void setSampled(boolean sampled) {
        isSampled = sampled;
    }

    public AtomicInteger getChildContextIndex() {
        return childContextIndex;
    }

    /**
     * Get the ID of the next sub context
     *
     * @return next spanId
     */
    public String nextChildContextId() {
        return this.spanId + RPC_ID_SEPARATOR + childContextIndex.incrementAndGet();
    }

    /**
     * Get the spanId of the previous sub context
     *
     * @return prev spanId
     */
    public String lastChildContextId() {
        return this.spanId + RPC_ID_SEPARATOR + childContextIndex.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SzTracerSpanContext)) {
            return false;
        }

        SzTracerSpanContext that = (SzTracerSpanContext) o;

        if (!traceId.equals(that.traceId)) {
            return false;
        }
        if (!spanId.equals(that.spanId)) {
            return false;
        }
        if (StringUtils.isBlank(parentId)) {
            return StringUtils.isBlank(that.parentId);
        }
        return parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        int result = traceId.hashCode();
        result = 31 * result + spanId.hashCode();
        result = 31 * result + parentId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SzTracerSpanContext{" + "traceId='" + traceId + '\'' + ", spanId='" + spanId
                + '\'' + ", parentId='" + parentId + '\'' + ", isSampled=" + isSampled
                + ", bizBaggage=" + bizBaggage + ", sysBaggage=" + sysBaggage
                + ", childContextIndex=" + childContextIndex + '}';
    }

}
