package com.wayue.tracer.core.reporter.common;


import com.wayue.tracer.core.appender.builder.XStringBuilder;
import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.appender.sefllog.Timestamp;
import com.wayue.tracer.core.context.span.SzTracerSpanContext;
import com.wayue.tracer.core.span.CommonLogSpan;
import com.wayue.tracer.core.tags.SpanTags;
import com.wayue.tracer.core.utils.StringUtils;

import java.io.IOException;

/**
 * CommonSpanEncoder
 * <p>
 *      Client errors and server errors are printed in the same file
 * </p>
 *
 * Note that there is a stateless instance: an instance of multiple log prints shared
 *
 */
public class CommonSpanEncoder implements SpanEncoder<CommonLogSpan> {

    @Override
    public String encode(CommonLogSpan commonLogSpan) throws IOException {
        if (commonLogSpan.getSzTracerSpanContext() == null) {
            return StringUtils.EMPTY_STRING;
        }
        SzTracerSpanContext spanContext = commonLogSpan.getSzTracerSpanContext();
        XStringBuilder xsb = new XStringBuilder();
        //The time when the report started as the time of printing, there is no completion time
        xsb.append(Timestamp.format(commonLogSpan.getStartTime()))
            //Ensure that the construct common is also carried
            .append(commonLogSpan.getTagsWithStr().get(SpanTags.CURR_APP_TAG.getKey()))
            .append(spanContext.getTraceId()).append(spanContext.getSpanId());
        this.appendStr(xsb, commonLogSpan);
        return xsb.toString();
    }

    private void appendStr(XStringBuilder xsb, CommonLogSpan commonLogSpan) {
        int slotSize = commonLogSpan.getSlots().size();

        for (int i = 0; i < slotSize; i++) {
            if (i + 1 != slotSize) {
                xsb.append(commonLogSpan.getSlots().get(i));
            } else {
                xsb.appendRaw(commonLogSpan.getSlots().get(i));
            }
        }
        xsb.appendRaw(StringUtils.NEWLINE);
    }
}
