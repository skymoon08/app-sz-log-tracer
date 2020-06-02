package com.wayyue.tracer.plugins.zipkin.adapter;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.LogData;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;
import zipkin2.Endpoint;
import zipkin2.Span;

import java.net.InetAddress;
import java.util.Map;


/***
 * ZipkinV2SpanAdapter : convent sofaTracer span model to zipkin span model
 * @author guolei.sgl 05/09/2018
 * @since v.2.3.0
 */
public class ZipkinV2SpanAdapter {

    /**
     * cache and performance improve
     */
    private InetAddress localIpAddress = null;

    /**
     * convent szTracerSpan model to zipKinSpan model
     *
     * @param szTracerSpan original span
     * @return zipkinSpan model
     */
    public Span convertToZipkinSpan(SzTracerSpan szTracerSpan) {
        if (szTracerSpan == null) {
            return null;
        }
        // spanId、parentId、tracerId
        Span.Builder zipkinSpanBuilder = Span.newBuilder();
        SzTracerSpanContext context = szTracerSpan.getSzTracerSpanContext();
        zipkinSpanBuilder.traceId(context.getTraceId());
        zipkinSpanBuilder.id(spanIdToLong(context.getSpanId()));
        if (StringUtils.isNotBlank(context.getParentId())) {
            zipkinSpanBuilder.parentId(spanIdToLong(context.getParentId()));
        }

        // timestamp & duration
        long start = szTracerSpan.getStartTime() * 1000;
        long finish = szTracerSpan.getEndTime() * 1000;
        zipkinSpanBuilder.timestamp(start);
        zipkinSpanBuilder.duration(finish - start);

        // kind
        Map<String, String> tagsWithStr = szTracerSpan.getTagsWithStr();
        String kindStr = tagsWithStr.get(Tags.SPAN_KIND.getKey());
        if (StringUtils.isNotBlank(kindStr) && kindStr.equals(Tags.SPAN_KIND_SERVER)) {
            zipkinSpanBuilder.kind(Span.Kind.SERVER);
        } else {
            zipkinSpanBuilder.kind(Span.Kind.CLIENT);
        }

        // Endpoint
        Endpoint endpoint = getZipkinEndpoint(szTracerSpan);
        zipkinSpanBuilder.localEndpoint(endpoint);

        // Tags
        this.addZipkinTags(zipkinSpanBuilder, szTracerSpan);

        // span name
        String operationName = szTracerSpan.getOperationName();
        if (StringUtils.isNotBlank(operationName)) {
            zipkinSpanBuilder.name(operationName);
        } else {
            zipkinSpanBuilder.name(StringUtils.EMPTY_STRING);
        }

        // Annotations
        this.addZipkinAnnotations(zipkinSpanBuilder, szTracerSpan);

        return zipkinSpanBuilder.build();
    }

    public static long spanIdToLong(String spanId) {
        return FNV64HashCode(spanId);
    }

    /**
     * from http://en.wikipedia.org/wiki/Fowler_Noll_Vo_hash
     *
     * @param data String data
     * @return fnv hash code
     */
    public static long FNV64HashCode(String data) {
        //hash FNVHash64 : http://www.isthe.com/chongo/tech/comp/fnv/index.html#FNV-param
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < data.length(); ++i) {
            char c = data.charAt(i);
            hash ^= c;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private Endpoint getZipkinEndpoint(SzTracerSpan span) {
        if (localIpAddress == null) {
            localIpAddress = NetUtils.getLocalAddress();
        }
        String appName = span.getTagsWithStr().get(CommonSpanTags.LOCAL_APP);
        return Endpoint.newBuilder().serviceName(appName).ip(localIpAddress).build();
    }

    /**
     * Put the baggage data into the tags
     *
     * @param zipkinSpan
     * @param span
     */
    private void addZipkinTagsWithBaggage(Span.Builder zipkinSpan, SzTracerSpan span) {
        SzTracerSpanContext sofaTracerSpanContext = span.getSzTracerSpanContext();
        if (sofaTracerSpanContext != null) {
            Map<String, String> sysBaggage = sofaTracerSpanContext.getSysBaggage();
            for (Map.Entry<String, String> e : sysBaggage.entrySet()) {
                zipkinSpan.putTag(e.getKey(), e.getValue());
            }
            Map<String, String> bizBaggage = sofaTracerSpanContext.getBizBaggage();
            for (Map.Entry<String, String> e : bizBaggage.entrySet()) {
                zipkinSpan.putTag(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * convent Annotations
     *
     * @param zipkinSpan
     * @param span
     */
    private void addZipkinAnnotations(Span.Builder zipkinSpan, SzTracerSpan span) {
        for (LogData logData : span.getLogs()) {
            Map<String, ?> fields = logData.getFields();
            if (fields == null || fields.size() <= 0) {
                continue;
            }
            for (Map.Entry<String, ?> entry : fields.entrySet()) {
                // zipkin has been support default log event depend on span kind & serviceName
                if (!(entry.getValue().toString().equals(LogData.CLIENT_RECV_EVENT_VALUE)
                      || entry.getValue().toString().equals(LogData.CLIENT_SEND_EVENT_VALUE)
                      || entry.getValue().toString().equals(LogData.SERVER_RECV_EVENT_VALUE) || entry
                    .getValue().toString().equals(LogData.SERVER_SEND_EVENT_VALUE))) {
                    zipkinSpan.addAnnotation(logData.getTime() * 1000, entry.getValue().toString());
                }
            }
        }
    }

    /**
     * convent tags
     *
     * @param zipkinSpan
     * @param span
     */
    private void addZipkinTags(Span.Builder zipkinSpan, SzTracerSpan span) {

        for (Map.Entry<String, String> e : span.getTagsWithStr().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Number> e : span.getTagsWithNumber().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue().toString());
        }
        for (Map.Entry<String, Boolean> e : span.getTagsWithBool().entrySet()) {
            zipkinSpan.putTag(e.getKey(), e.getValue().toString());
        }

        addZipkinTagsWithBaggage(zipkinSpan, span);
    }
}
