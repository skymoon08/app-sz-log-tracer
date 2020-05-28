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
package com.wayyue.tracer.core.tags;

import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.constants.ComponentNameConstants;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.span.SzTracerSpan;
import io.opentracing.tag.StringTag;

/**
 * SpanTags
 *
 * @author yangguanchao
 * @since 2017/07/01
 */
public class SpanTags {

    /**
     * current span tags
     */
    public static final StringTag CURR_APP_TAG = new StringTag("curr.app");

    public static void putTags(String key, String val) {
        SzTracerSpan currentSpan = SzTraceContextHolder.getSzTraceContext().getCurrentSpan();
        if (checkTags(currentSpan)) {
            currentSpan.setTag(key, val);
        }
    }

    public static void putTags(String key, Number val) {
        SzTracerSpan currentSpan = SzTraceContextHolder.getSzTraceContext().getCurrentSpan();
        if (checkTags(currentSpan)) {
            currentSpan.setTag(key, val);
        }
    }

    public static void putTags(String key, Boolean val) {
        SzTracerSpan currentSpan = SzTraceContextHolder.getSzTraceContext().getCurrentSpan();
        if (checkTags(currentSpan)) {
            currentSpan.setTag(key, val);
        }
    }

    private static boolean checkTags(SzTracerSpan currentSpan) {
        if (currentSpan == null) {
            SelfDefineLog.error("Current stage has no span exist in SzTracerContext.");
            return false;
        }
        String componentType = currentSpan.getSzTracer().getTracerType();
        if (!componentType.equalsIgnoreCase(ComponentNameConstants.FLEXIBLE)) {
            SelfDefineLog.error("Cannot set tag to component. current component is [" + componentType
                          + "]");
            return false;
        }
        return true;
    }
}
