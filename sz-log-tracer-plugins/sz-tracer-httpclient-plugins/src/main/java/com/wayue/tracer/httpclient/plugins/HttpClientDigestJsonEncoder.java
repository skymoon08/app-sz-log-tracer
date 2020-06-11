package com.wayue.tracer.httpclient.plugins;


import com.wayue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.Map;

/**
 * HttpClientDigestJsonEncoder
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class HttpClientDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb, SzTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNumber = span.getTagsWithNumber();
        //URL
        jsb.append(CommonSpanTags.REQUEST_URL, tagWithStr.get(CommonSpanTags.REQUEST_URL));
        //POST/GET
        jsb.append(CommonSpanTags.METHOD, tagWithStr.get(CommonSpanTags.METHOD));
        // requestSize
        Number requestSize = tagWithNumber.get(CommonSpanTags.REQ_SIZE);
        //Request Body bytes length
        jsb.append(CommonSpanTags.REQ_SIZE, (requestSize == null ? 0L : requestSize.longValue()));
        Number responseSize = tagWithNumber.get(CommonSpanTags.RESP_SIZE);
        //Response Body bytes length
        jsb.append(CommonSpanTags.RESP_SIZE, (responseSize == null ? 0L : responseSize.longValue()));
        jsb.append(CommonSpanTags.REMOTE_APP, tagWithStr.get(CommonSpanTags.REMOTE_APP));
    }
}