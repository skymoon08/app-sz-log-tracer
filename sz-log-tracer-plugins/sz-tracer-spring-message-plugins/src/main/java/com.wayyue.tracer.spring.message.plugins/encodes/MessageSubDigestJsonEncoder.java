package com.wayyue.tracer.spring.message.plugins.encodes;


import com.wayue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.encoder.AbstractDigestSpanEncoder;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.StringUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 3:30 PM
 * @since:
 **/
public class MessageSubDigestJsonEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        if (StringUtils.isNotBlank(tagWithStr.get(Tags.ERROR.getKey()))) {
            jsb.append(Tags.ERROR.getKey(), tagWithStr.get(Tags.ERROR.getKey()));
        } else {
            jsb.append(Tags.ERROR.getKey(), StringUtils.EMPTY_STRING);
        }
        jsb.append(CommonSpanTags.MSG_TOPIC, tagWithStr.get(CommonSpanTags.MSG_TOPIC));
        jsb.append(CommonSpanTags.MSG_CHANNEL, tagWithStr.get(CommonSpanTags.MSG_CHANNEL));
        jsb.append(CommonSpanTags.MSG_ID, tagWithStr.get(CommonSpanTags.MSG_ID));
        jsb.append(CommonSpanTags.REMOTE_APP, tagWithStr.get(CommonSpanTags.REMOTE_APP));
    }
}
