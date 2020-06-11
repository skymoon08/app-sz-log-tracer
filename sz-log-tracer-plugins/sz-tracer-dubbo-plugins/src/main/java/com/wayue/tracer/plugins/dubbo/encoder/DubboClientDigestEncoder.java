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

public class DubboClientDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {
        Map<String, String> tagWithStr = span.getTagsWithStr();
        Map<String, Number> tagWithNum = span.getTagsWithNumber();
        //protocol
        xsb.append(tagWithStr.get(CommonSpanTags.PROTOCOL));
        // service
        xsb.append(tagWithStr.get(CommonSpanTags.SERVICE));
        // method
        xsb.append(tagWithStr.get(CommonSpanTags.METHOD));
        // invoke type
        xsb.append(tagWithStr.get(CommonSpanTags.INVOKE_TYPE));
        // remote host
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_HOST));
        // remote port
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_PORT));
        // local port
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_HOST));
        // client.serialize.time
        xsb.append(tagWithNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME) + "");
        // client.deserialize.time
        xsb.append(tagWithNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME) + "");
        // client.serialize.size
        Number reqSizeNum = tagWithNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_SIZE);
        xsb.append(reqSizeNum == null ? 0 : reqSizeNum.longValue());
        // client.deserialize.size
        Number respSizeNum = tagWithNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_SIZE);
        xsb.append(respSizeNum == null ? 0 : respSizeNum.longValue());
        // error message
        xsb.append(StringUtils.isBlank(tagWithStr.get(Tags.ERROR.getKey())) ? "" : tagWithStr      .get(Tags.ERROR.getKey()));
    }
}
