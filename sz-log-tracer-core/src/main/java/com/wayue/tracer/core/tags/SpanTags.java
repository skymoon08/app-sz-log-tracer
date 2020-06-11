package com.wayue.tracer.core.tags;

import com.wayue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.holder.SzTraceContextHolder;
import com.wayue.tracer.core.span.SzTracerSpan;
import io.opentracing.tag.StringTag;

/**
 * SpanTags
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
