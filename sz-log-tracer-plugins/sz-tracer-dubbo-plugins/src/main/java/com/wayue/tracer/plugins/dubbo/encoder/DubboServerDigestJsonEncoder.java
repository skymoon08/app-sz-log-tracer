package com.wayue.tracer.plugins.dubbo.encoder;

import com.wayue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.StringUtils;
import com.wayue.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import io.opentracing.tag.Tags;

import java.util.Map;


public class DubboServerDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {
        Map<String, String> tagStr = span.getTagsWithStr();
        Map<String, Number> tagNum = span.getTagsWithNumber();

        //protocol
        jsb.append(CommonSpanTags.PROTOCOL, tagStr.get(CommonSpanTags.PROTOCOL));
        //serviceName
        jsb.append(CommonSpanTags.SERVICE, tagStr.get(CommonSpanTags.SERVICE));
        //method
        jsb.append(CommonSpanTags.METHOD, tagStr.get(CommonSpanTags.METHOD));
        //local ip
        jsb.append(CommonSpanTags.LOCAL_HOST, tagStr.get(CommonSpanTags.LOCAL_HOST));
        //local port
        jsb.append(CommonSpanTags.LOCAL_PORT, tagStr.get(CommonSpanTags.LOCAL_PORT));

        long serializeTime = getTime(tagNum.get(AttachmentKeyConstants.SERVER_SERIALIZE_TIME));
        jsb.append(CommonSpanTags.SERVER_SERIALIZE_TIME, serializeTime);
        long deserializeTime = getTime(tagNum.get(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME));
        jsb.append(CommonSpanTags.SERVER_DESERIALIZE_TIME, deserializeTime);

        Number reqSizeNum = tagNum.get(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE);
        jsb.append(CommonSpanTags.REQ_SIZE, reqSizeNum == null ? 0 : reqSizeNum.longValue());
        Number respSizeNum = tagNum.get(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE);
        jsb.append(CommonSpanTags.RESP_SIZE, respSizeNum == null ? 0 : respSizeNum.longValue());
        //error message
        if (StringUtils.isNotBlank(tagStr.get(Tags.ERROR.getKey()))) {
            jsb.append(Tags.ERROR.getKey(), tagStr.get(Tags.ERROR.getKey()));
        } else {
            jsb.append(Tags.ERROR.getKey(), StringUtils.EMPTY_STRING);
        }
    }

    private long getTime(Number number) {
        if (number != null) {
            return number.longValue();
        }
        return 0;
    }
}
