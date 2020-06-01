package com.wayyue.tracer.flexible.plugins;


import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.core.utils.TracerUtils;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 **/
public class FlexibleStatJsonReporter extends AbstractSzTracerStatisticReporter {

    public FlexibleStatJsonReporter(String statTracerName, String rollingPolicy,
                                    String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan tracerSpan) {
        Map<String, String> tagsWithStr = tracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        statKey.addKey(CommonSpanTags.LOCAL_APP, tagsWithStr.get(CommonSpanTags.LOCAL_APP));
        statKey.addKey(CommonSpanTags.METHOD, tagsWithStr.get(CommonSpanTags.METHOD));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(tracerSpan));
        //success
        String error = tagsWithStr.get(Tags.ERROR.getKey());
        statKey.setResult(StringUtils.isBlank(error) ? SzTracerConstant.STAT_FLAG_SUCCESS
            : SzTracerConstant.STAT_FLAG_FAILS);
        //end
        statKey.setEnd(TracerUtils.getLoadTestMark(tracerSpan));
        //value the count and duration
        long duration = tracerSpan.getEndTime() - tracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}