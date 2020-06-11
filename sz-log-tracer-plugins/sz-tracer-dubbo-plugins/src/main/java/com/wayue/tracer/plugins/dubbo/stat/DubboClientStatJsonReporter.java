package com.wayue.tracer.plugins.dubbo.stat;

import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.TracerUtils;

import java.util.Map;

public class DubboClientStatJsonReporter extends AbstractSzTracerStatisticReporter {

    public DubboClientStatJsonReporter(String statTracerName, String rollingPolicy,
                                       String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan tracerSpan) {
        //tags
        Map<String, String> tagsWithStr = tracerSpan.getTagsWithStr();
        StatMapKey statKey = new StatMapKey();
        String appName = tagsWithStr.get(CommonSpanTags.LOCAL_APP);
        //service name
        String serviceName = tagsWithStr.get(CommonSpanTags.SERVICE);
        //method name
        String methodName = tagsWithStr.get(CommonSpanTags.METHOD);
        statKey.addKey(CommonSpanTags.LOCAL_APP, appName);
        statKey.addKey(CommonSpanTags.SERVICE, serviceName);
        statKey.addKey(CommonSpanTags.METHOD, methodName);

        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        statKey.setResult(SzTracerConstant.RESULT_CODE_SUCCESS.equals(resultCode) ?
                SzTracerConstant.STAT_FLAG_SUCCESS : SzTracerConstant.STAT_FLAG_FAILS);
        statKey.setEnd(buildString(new String[] { getLoadTestMark(tracerSpan) }));
        statKey.setLoadTest(TracerUtils.isLoadTest(tracerSpan));

        long duration = tracerSpan.getEndTime() - tracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        this.addStat(statKey, values);
    }

    protected String getLoadTestMark(SzTracerSpan span) {
        if (TracerUtils.isLoadTest(span)) {
            return "T";
        } else {
            return "F";
        }
    }
}
