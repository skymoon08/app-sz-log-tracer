package com.wayue.tracer.httpclient.plugins;

import com.wayue.tracer.core.encoder.AbstractDigestSpanEncoder;

import com.wayue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.Map;

/**
 * HttpClientDigestEncoder
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class HttpClientDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb, SzTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //URL
        xsb.append(tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //POST/GET
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        // requestSize
        Number requestSize = tagWithNum.get(CommonSpanTags.REQ_SIZE);
        //Request Body bytes length
        xsb.append(requestSize == null ? 0L : requestSize.longValue());
        Number responseSize = tagWithNum.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes length
        xsb.append((responseSize == null ? 0L : responseSize.longValue()));
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_APP));
    }
}
