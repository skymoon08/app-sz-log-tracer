package com.wayyue.tracer.httpclient.plugins;


import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.util.Map;


public class HttpClientStatReporter extends AbstractSzTracerStatisticReporter {

    public HttpClientStatReporter(String statTracerName, String rollingPolicy,
                                  String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan SzTracerSpan) {
        Map<String, String> tagsWithStr = SzTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        String localApp = tagsWithStr.get(CommonSpanTags.LOCAL_APP);
        String requestUrl = tagsWithStr.get(CommonSpanTags.REQUEST_URL);
        //method name
        String methodName = tagsWithStr.get(CommonSpanTags.METHOD);
        statKey.setKey(buildString(new String[] { localApp, requestUrl, methodName }));

        //success
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        boolean success = isWebHttpClientSuccess(resultCode);
        statKey.setResult(success ? SzTracerConstant.STAT_FLAG_SUCCESS : SzTracerConstant.STAT_FLAG_FAILS);

        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(SzTracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(SzTracerSpan));
        //value the count and duration
        long duration = SzTracerSpan.getEndTime() - SzTracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }
}
