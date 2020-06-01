package com.wayyue.tracer.flexible.plugins;


import com.wayyue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayyue.tracer.core.appender.builder.XStringBuilder;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FlexibleDigestEncoder extends FlexibleDigestJsonEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb, SzTracerSpan span) {
        Map<String, String> strTags = span.getTagsWithStr();
        Map<String, Number> numTags = span.getTagsWithNumber();
        Map<String, Boolean> boolTags = span.getTagsWithBool();

        xsb.append(strTags.get(CommonSpanTags.METHOD));
        Set<String> strKeys = strTags.keySet();
        Map<String,String> flexibleTags = new HashMap<>();
        strKeys.forEach(key->{
            if (!isFlexible(key)){
                flexibleTags.put(key,strTags.get(key));
            }
        });

        Set<String> numKeys = numTags.keySet();
        numKeys.forEach(key->{
            if (!isFlexible(key)){
                flexibleTags.put(key,String.valueOf(numTags.get(key)));
            }
        });

        Set<String> boolKeys = boolTags.keySet();
        boolKeys.forEach(key->{
            if (!isFlexible(key)){
                flexibleTags.put(key,String.valueOf(boolTags.get(key)));
            }
        });

        String flexibleTagsData = StringUtils.mapToString(flexibleTags);
        xsb.append(flexibleTagsData);
    }
}
