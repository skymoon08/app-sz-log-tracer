package com.wayue.tracer.core.listener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SpanReportListenerHolder
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class SpanReportListenerHolder {

    private static List<SpanReportListener> spanReportListenersHolder = new CopyOnWriteArrayList<SpanReportListener>();

    public static List<SpanReportListener> getSpanReportListenersHolder() {
        return spanReportListenersHolder;
    }

    public static void addSpanReportListeners(List<SpanReportListener> spanReportListenersHolder) {
        if (spanReportListenersHolder != null && spanReportListenersHolder.size() > 0) {
            SpanReportListenerHolder.spanReportListenersHolder.addAll(spanReportListenersHolder);
        }
    }

    public static void addSpanReportListener(SpanReportListener spanReportListener) {
        if (spanReportListener != null) {
            SpanReportListenerHolder.spanReportListenersHolder.add(spanReportListener);
        }
    }

    public static void clear() {
        spanReportListenersHolder.clear();
    }
}
