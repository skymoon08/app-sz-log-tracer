
package com.wayyue.tracer.plugins.springmvc;


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
    public void doReportStat(SzTracerSpan tracerSpan) {
        Map<String, String> tagsWithStr = tracerSpan.getTagsWithStr();
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
        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(tracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(tracerSpan));
        //duration
        long duration = tracerSpan.getEndTime() - tracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        this.addStat(statKey, values);
    }
}