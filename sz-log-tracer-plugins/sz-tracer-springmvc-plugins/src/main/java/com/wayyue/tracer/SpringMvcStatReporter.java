
package com.wayyue.tracer;


import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * SpringMvcStatReporter
 *
 */
public class SpringMvcStatReporter extends AbstractSzTracerStatisticReporter {

    public SpringMvcStatReporter(String statTracerName, String rollingPolicy,
                                 String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        statKey
            .setKey(buildString(new String[] { tagsWithStr.get(CommonSpanTags.LOCAL_APP),
                    tagsWithStr.get(CommonSpanTags.REQUEST_URL),
                    tagsWithStr.get(CommonSpanTags.METHOD) }));
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = (resultCode != null && resultCode.length() > 0 && this
            .isHttpOrMvcSuccess(resultCode));
        statKey.setResult(success ? SzTracerConstant.DIGEST_FLAG_SUCCESS
            : SzTracerConstant.DIGEST_FLAG_FAILS);
        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(sofaTracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //duration
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        this.addStat(statKey, values);
    }
}