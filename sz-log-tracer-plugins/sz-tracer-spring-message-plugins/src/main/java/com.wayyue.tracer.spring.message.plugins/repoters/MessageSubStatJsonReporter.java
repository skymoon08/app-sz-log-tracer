
package com.wayyue.tracer.spring.message.plugins.repoters;


import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/3/13 3:31 PM
 * @since:
 **/
public class MessageSubStatJsonReporter extends AbstractSzTracerStatisticReporter {

    public MessageSubStatJsonReporter(String statTracerName, String rollingPolicy,
                                      String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan sofaTracerSpan) {
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        statKey.addKey(CommonSpanTags.LOCAL_APP, tagsWithStr.get(CommonSpanTags.LOCAL_APP));
        statKey.addKey("operationName", sofaTracerSpan.getOperationName());
        statKey.addKey(CommonSpanTags.MSG_CHANNEL, tagsWithStr.get(CommonSpanTags.MSG_CHANNEL));
        statKey.addKey(CommonSpanTags.MSG_TOPIC, tagsWithStr.get(CommonSpanTags.MSG_TOPIC));
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = isWebHttpClientSuccess(resultCode);
        statKey.setResult(success ? SzTracerConstant.STAT_FLAG_SUCCESS
            : SzTracerConstant.STAT_FLAG_FAILS);
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));
        //end
        statKey.setEnd(TracerUtils.getLoadTestMark(sofaTracerSpan));
        //value the count and duration
        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}
