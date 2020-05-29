package com.wayyue.tracer.plugins.dubbo.encoder;


import com.wayyue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayyue.tracer.core.appender.builder.XStringBuilder;
import com.wayyue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.plugins.dubbo.constants.AttachmentKeyConstants;
import io.opentracing.tag.Tags;

import java.util.Map;

public class DubboClientDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {
        Map<String, String> tagStr = span.getTagsWithStr();
        Map<String, Number> tagNum = span.getTagsWithNumber();

        // protocol
        jsb.append(CommonSpanTags.PROTOCOL, tagStr.get(CommonSpanTags.PROTOCOL));
        // serviceName
        jsb.append(CommonSpanTags.SERVICE, tagStr.get(CommonSpanTags.SERVICE));
        // method
        jsb.append(CommonSpanTags.METHOD, tagStr.get(CommonSpanTags.METHOD));
        //invoke type
        jsb.append(CommonSpanTags.INVOKE_TYPE, tagStr.get(CommonSpanTags.INVOKE_TYPE));
        //target ip
        jsb.append(CommonSpanTags.REMOTE_HOST, tagStr.get(CommonSpanTags.REMOTE_HOST));
        //target port
        jsb.append(CommonSpanTags.REMOTE_PORT, tagStr.get(CommonSpanTags.REMOTE_PORT));
        //local ip
        jsb.append(CommonSpanTags.LOCAL_HOST, tagStr.get(CommonSpanTags.LOCAL_HOST));
        //request serialize time
        jsb.append(CommonSpanTags.CLIENT_SERIALIZE_TIME, tagNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME));
        //response deserialize time
        jsb.append(CommonSpanTags.CLIENT_DESERIALIZE_TIME, tagNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME));
        //Request Body bytes length
        Number reqSizeNum = tagNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE);
        jsb.append(CommonSpanTags.REQ_SIZE, reqSizeNum == null ? 0 : reqSizeNum.longValue());
        //Response Body bytes length
        Number respSizeNum = tagNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE);
        jsb.append(CommonSpanTags.RESP_SIZE, respSizeNum == null ? 0 : respSizeNum.longValue());

        //error message
        if (StringUtils.isNotBlank(tagStr.get(Tags.ERROR.getKey()))) {
            jsb.append(Tags.ERROR.getKey(), tagStr.get(Tags.ERROR.getKey()));
        } else {
            jsb.append(Tags.ERROR.getKey(), StringUtils.EMPTY_STRING);
        }
    }
}
