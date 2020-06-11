package com.wayue.tracer.plugins.dubbo.stat;


import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.reporter.stat.model.StatKey;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.utils.TracerUtils;

import java.util.Map;

public class DubboServerStatReporter extends AbstractSzTracerStatisticReporter {

    public DubboServerStatReporter(String statTracerName, String rollingPolicy,
                                   String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan szTracerSpan) {
        Map<String, String> tagsWithStr = szTracerSpan.getTagsWithStr();
        StatKey statKey = new StatKey();
        String appName = tagsWithStr.get(CommonSpanTags.LOCAL_APP);
        //service name
        String serviceName = tagsWithStr.get(CommonSpanTags.SERVICE);
        //method name
        String methodName = tagsWithStr.get(CommonSpanTags.METHOD);
        statKey.setKey(buildString(new String[] { appName, serviceName, methodName }));
        String resultCode = tagsWithStr.get(CommonSpanTags.RESULT_CODE);
        statKey.setResult(SzTracerConstant.RESULT_CODE_SUCCESS.equals(resultCode) ?
                SzTracerConstant.STAT_FLAG_SUCCESS : SzTracerConstant.STAT_FLAG_FAILS);
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
