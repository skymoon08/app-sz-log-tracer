
package com.wayyue.tracer.mvcencoder;


import com.wayyue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayyue.tracer.core.appender.builder.XStringBuilder;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;

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