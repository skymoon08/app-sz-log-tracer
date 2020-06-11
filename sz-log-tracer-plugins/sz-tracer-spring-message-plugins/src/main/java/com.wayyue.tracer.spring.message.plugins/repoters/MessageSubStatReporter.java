
package com.wayyue.tracer.spring.message.plugins.repoters;


import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.reporter.stat.model.StatKey;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/9/1 5:40 PM
 * @since:
 **/
public class MessageSubStatReporter extends AbstractSzTracerStatisticReporter {

    public MessageSubStatReporter(String statTracerName, String rollingPolicy,
                                  String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan szTracerSpan) {
        Map<String, String> tagsWithStr = szTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        String localApp = tagsWithStr.get(CommonSpanTags.LOCAL_APP);
        String operationName = szTracerSpan.getOperationName();
        String channelName = tagsWithStr.get(CommonSpanTags.MSG_CHANNEL);
        String topic = tagsWithStr.get(CommonSpanTags.MSG_TOPIC);
        //method name
        statKey.setKey(buildString(new String[] { localApp, operationName, channelName, topic }));
        //success
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = isWebHttpClientSuccess(resultCode);
        statKey.setResult(success ? SzTracerConstant.STAT_FLAG_SUCCESS
            : SzTracerConstant.STAT_FLAG_FAILS);
        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(szTracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(szTracerSpan));
        //value the count and duration
        long duration = szTracerSpan.getEndTime() - szTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}
