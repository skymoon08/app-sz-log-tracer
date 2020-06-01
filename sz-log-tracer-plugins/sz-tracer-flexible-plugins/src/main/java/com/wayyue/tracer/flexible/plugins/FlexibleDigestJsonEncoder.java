
package com.wayyue.tracer.flexible.plugins;

import com.wayyue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayyue.tracer.core.appender.builder.XStringBuilder;
import com.wayyue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import io.opentracing.tag.Tags;

import java.util.Map;
import java.util.Set;

/**
 * FlexibleDigestJsonEncoder for flexible biz tracer
 *
 **/
public class FlexibleDigestJsonEncoder extends AbstractDigestSpanEncoder {


    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb, SzTracerSpan span) {
        Map<String, String> strTags = span.getTagsWithStr();
        Map<String, Number> numTags = span.getTagsWithNumber();
        Map<String, Boolean> boolTags = span.getTagsWithBool();
        //POST/GET
        jsb.append(CommonSpanTags.METHOD, strTags.get(CommonSpanTags.METHOD));

        Set<String> strKeys = strTags.keySet();
        strKeys.forEach(key->{
            if (!isFlexible(key)){
                jsb.append(key,strTags.get(key));
            }
        });
        Set<String> numKeys = numTags.keySet();
        numKeys.forEach(key->{
            if (!isFlexible(key)){
                jsb.append(key,numTags.get(key));
            }
        });
        Set<String> boolKeys = boolTags.keySet();
        boolKeys.forEach(key->{
            if (!isFlexible(key)){
                jsb.append(key,boolTags.get(key));
            }
        });
    }

    /**
     * common tag and component tag excluded
     * @param key
     * @return
     */
    protected boolean isFlexible(String key) {
        return CommonSpanTags.LOCAL_APP.equalsIgnoreCase(key)
               || CommonSpanTags.TRACE_ID.equalsIgnoreCase(key)
               || CommonSpanTags.SPAN_ID.equalsIgnoreCase(key)
               || CommonSpanTags.CURRENT_THREAD_NAME.equalsIgnoreCase(key)
               || CommonSpanTags.METHOD.equalsIgnoreCase(key)
               || CommonSpanTags.TIME.equalsIgnoreCase(key)
               || CommonSpanTags.TIME_COST_MILLISECONDS.equalsIgnoreCase(key)
               || Tags.SPAN_KIND.getKey().equalsIgnoreCase(key);
    }
}