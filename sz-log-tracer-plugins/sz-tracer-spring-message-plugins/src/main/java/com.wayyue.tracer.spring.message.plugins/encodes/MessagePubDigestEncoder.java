
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
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/9/1 3:52 PM
 * @since:
 **/
public class MessagePubDigestEncoder extends AbstractDigestSpanEncoder {

    @Override
    protected void appendComponentSlot(XStringBuilder xsb, JsonStringBuilder jsb,
                                       SzTracerSpan span) {

        Map<String, String> tagWithStr = span.getTagsWithStr();
        if (StringUtils.isNotBlank(tagWithStr.get(Tags.ERROR.getKey()))) {
            xsb.append(tagWithStr.get(Tags.ERROR.getKey()));
        } else {
            xsb.append(StringUtils.EMPTY_STRING);
        }
        xsb.append(tagWithStr.get(CommonSpanTags.MSG_CHANNEL));
        xsb.append(tagWithStr.get(CommonSpanTags.MSG_ID));
        xsb.append(tagWithStr.get(CommonSpanTags.REMOTE_APP));
    }
}
