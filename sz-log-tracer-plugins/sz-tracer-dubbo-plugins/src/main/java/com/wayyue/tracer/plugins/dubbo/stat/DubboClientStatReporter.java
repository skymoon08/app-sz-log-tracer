package com.wayyue.tracer.plugins.dubbo.stat;

import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.util.Map;

public class DubboClientStatReporter extends AbstractSzTracerStatisticReporter {

    public DubboClientStatReporter(String statTracerName, String rollingPolicy,
                                   String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan tracerSpan) {
        Map<String, String> tagsWithStr = tracerSpan.getTagsWithStr();
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
        statKey.setEnd(buildString(new String[] { TracerUtils.getLoadTestMark(tracerSpan) }));
        //pressure mark
        statKey.setLoadTest(TracerUtils.isLoadTest(tracerSpan));
        //value the count and duration
        long duration = tracerSpan.getEndTime() - tracerSpan.getStartTime();
        long[] values = new long[] { 1, duration };
        //reserve
        this.addStat(statKey, values);
    }

}
