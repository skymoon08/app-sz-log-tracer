package com.wayyue.tracer.plugins.springmvc;


import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * SpringMvcJsonStatReporter
 *
 */
public class SpringMvcJsonStatReporter extends SpringMvcStatReporter {

    public SpringMvcJsonStatReporter(String statTracerName, String rollingPolicy,
                                     String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        statKey.addKey(CommonSpanTags.LOCAL_APP, tagsWithStr.get(CommonSpanTags.LOCAL_APP));
        statKey.addKey(CommonSpanTags.REQUEST_URL, tagsWithStr.get(CommonSpanTags.REQUEST_URL));
        statKey.addKey(CommonSpanTags.METHOD, tagsWithStr.get(CommonSpanTags.METHOD));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //success
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = (resultCode != null && resultCode.length() > 0 && this
            .isHttpOrMvcSuccess(resultCode));
        statKey.setResult(success ? SzTracerConstant.STAT_FLAG_SUCCESS : SzTracerConstant.STAT_FLAG_FAILS);
        //end
        statKey.setEnd(TracerUtils.getLoadTestMark(sofaTracerSpan));
        //duration
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}