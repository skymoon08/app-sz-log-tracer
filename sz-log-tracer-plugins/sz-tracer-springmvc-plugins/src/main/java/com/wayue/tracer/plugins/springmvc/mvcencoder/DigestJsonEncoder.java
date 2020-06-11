package com.wayue.tracer.plugins.springmvc.mvcencoder;


import com.wayue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.Map;

/**
 * DigestJsonEncoder
 *
 */
public class DigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //Request URL
        jsb.append(CommonSpanTags.REQUEST_URL, tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //Request method
        jsb.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        //Request Body Size (byte)
        jsb.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        //Response Body Size，(byte)
        jsb.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L : responseSize.longValue()));
    }
}