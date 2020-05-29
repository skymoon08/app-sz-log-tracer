/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wayyue.tracer.plugins.dubbo.stat;

import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.util.Map;

public class DubboClientStatJsonReporter extends AbstractSzTracerStatisticReporter {

    public DubboClientStatJsonReporter(String statTracerName, String rollingPolicy,
                                       String logReserveConfig) {
        super(statTracerName, rollingPolicy, logReserveConfig);
    }

    @Override
    public void doReportStat(SzTracerSpan sofaTracerSpan) {
        //tags
        Map<String, String> tagsWithStr = sofaTracerSpan.getTagsWithStr();
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
        statKey.setEnd(buildString(new String[] { getLoadTestMark(sofaTracerSpan) }));
        statKey.setLoadTest(TracerUtils.isLoadTest(sofaTracerSpan));

        long duration = sofaTracerSpan.getEndTime() - sofaTracerSpan.getStartTime();
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
