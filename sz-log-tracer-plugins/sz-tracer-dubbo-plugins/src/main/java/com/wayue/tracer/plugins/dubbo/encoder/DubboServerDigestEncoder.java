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

public class DubboServerDigestEncoder extends AbstractDigestSpanEncoder {

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
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_HOST));
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_PORT));
        xsb.append(tagWithStr.get(CommonSpanTags.LOCAL_HOST));
        xsb.append(tagWithNum.get(AttachmentKeyConstants.CLIENT_SERIALIZE_TIME) + "");
        xsb.append(tagWithNum.get(AttachmentKeyConstants.CLIENT_DESERIALIZE_TIME) + "");
        //Request Body bytes length
        long serializeTime = getTime(tagWithNum.get(AttachmentKeyConstants.SERVER_SERIALIZE_TIME));
        long deserializeTime = getTime(tagWithNum.get(AttachmentKeyConstants.SERVER_DESERIALIZE_TIME));
        xsb.append(String.valueOf(serializeTime));
        xsb.append(String.valueOf(deserializeTime));
        Number reqSizeNum = tagWithNum.get(AttachmentKeyConstants.SERVER_DESERIALIZE_SIZE);
        xsb.append(reqSizeNum == null ? 0 : reqSizeNum.longValue());
        Number respSizeNum = tagWithNum.get(AttachmentKeyConstants.SERVER_SERIALIZE_SIZE);
        xsb.append(respSizeNum == null ? 0 : respSizeNum.longValue());

        xsb.append(StringUtils.isBlank(tagWithStr.get(Tags.ERROR.getKey())) ? "" : tagWithStr.get(Tags.ERROR.getKey()));
    }

    private long getTime(Number number) {
        if (number != null) {
            return number.longValue();
        }
        return 0;
    }

}
