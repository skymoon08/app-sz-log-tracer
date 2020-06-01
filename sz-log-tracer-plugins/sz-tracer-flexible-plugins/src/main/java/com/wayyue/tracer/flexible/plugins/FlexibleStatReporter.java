
package com.wayyue.tracer.flexible.plugins;

import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.core.utils.TracerUtils;
import io.opentracing.tag.Tags;

import java.util.Map;



public class FlexibleStatReporter extends AbstractSzTracerStatisticReporter {

    public FlexibleStatReporter(String statTracerName, String rollingPolicy, String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        String error = tagsWithStr.get(Tags.ERROR.getKey());
        statKey.setKey(buildString(new String[] { tagsWithStr.get(CommonSpanTags.LOCAL_APP), tagsWithStr.get(CommonSpanTags.METHOD) }));

        statKey.setResult(StringUtils.isBlank(error) ? SzTracerConstant.DIGEST_FLAG_SUCCESS : SzTracerConstant.DIGEST_FLAG_FAILS);

        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(sofaTracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //duration
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        this.addStat(statKey, values);
    }
}
