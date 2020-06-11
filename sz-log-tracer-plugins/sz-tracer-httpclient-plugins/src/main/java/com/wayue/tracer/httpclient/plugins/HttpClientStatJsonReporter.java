package com.wayue.tracer.httpclient.plugins;


import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.TracerUtils;

import java.util.Map;

/**
 * HttpClientStatJsonReporter
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class HttpClientStatJsonReporter extends AbstractSzTracerStatisticReporter {

    public HttpClientStatJsonReporter(String statTracerName, String rollingPolicy,
                                      String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan tracerSpan) {
        Map<String, String> tagsWithStr = tracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        statKey.addKey(CommonSpanTags.LOCAL_APP, tagsWithStr.get(CommonSpanTags.LOCAL_APP));
        statKey.addKey(CommonSpanTags.REQUEST_URL, tagsWithStr.get(CommonSpanTags.REQUEST_URL));
        statKey.addKey(CommonSpanTags.METHOD, tagsWithStr.get(CommonSpanTags.METHOD));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(tracerSpan));
        //success
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = isWebHttpClientSuccess(resultCode);
        statKey.setResult(success ? SzTracerConstant.STAT_FLAG_SUCCESS : SzTracerConstant.STAT_FLAG_FAILS);
        //end
        statKey.setEnd(TracerUtils.getLoadTestMark(tracerSpan));
        //value the count and duration
        long duration = tracerSpan.getEndTime() - tracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}