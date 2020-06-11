
package com.wayue.tracer.plugins.springmvc.mvcencoder;


import com.wayue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.Map;

/**
 * DigestEncoder
 *
 */
public class DigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //URL
        xsb.append(tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //method
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        // requestSize
        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        //Request Body bytes
        xsb.append((requestSize == null ? 0L : requestSize.longValue()) + SzTracerConstant.BYTE);
        // responseSize
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes
        xsb.append((responseSize == null ? 0L : responseSize.longValue()) + SzTracerConstant.BYTE);
    }
}