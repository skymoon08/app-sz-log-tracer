package com.wayyue.tracer.core.extensions;

import io.opentracing.Span;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;


public class SpanExtensionFactory {

    private static Set<SpanExtension> spanExtensions = new HashSet<SpanExtension>();

    static {
        for (SpanExtension spanExtension : ServiceLoader.load(SpanExtension.class)) {
            spanExtensions.add(spanExtension);
        }
    }

    public static void logStartedSpan(Span currentSpan) {
        if (!spanExtensions.isEmpty() && currentSpan != null) {
            for (SpanExtension spanExtension : spanExtensions) {
                spanExtension.logStartedSpan(currentSpan);
            }
        }
    }

    public static void logStoppedSpan(Span currentSpan) {
        if (!spanExtensions.isEmpty()) {
            for (SpanExtension spanExtension : spanExtensions) {
                spanExtension.logStoppedSpan(currentSpan);
            }
        }
    }

    public static void logStoppedSpanInRunnable(Span currentSpan) {
        if (!spanExtensions.isEmpty()) {
            for (SpanExtension spanExtension : spanExtensions) {
                spanExtension.logStoppedSpanInRunnable(currentSpan);
            }
        }
    }
}