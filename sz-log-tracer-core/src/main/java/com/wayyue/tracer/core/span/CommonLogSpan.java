package com.wayyue.tracer.core.span;


import com.wayyue.tracer.core.SzTracer;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CommonLogSpan
 * <p>
 * Mainly for recording specific sequential data
 * <p>
 * <p>
 * The reason to create a new object is to distinguish between the printing of CommonLogSpan and daily digest.
 */
public class CommonLogSpan extends SzTracerSpan {

    private static final int MAX_SLOT_SIZE = 32;

    /**
     * The common slot, all the log data that needs to be printed are placed in it.
     */
    private List<String> slots = new ArrayList<String>();

    private AtomicInteger slotCounter = new AtomicInteger(0);

    public CommonLogSpan(SzTracer SzTracer, long startTime, String operationName,
                         SzTracerSpanContext SzTracerSpanContext, Map<String, ?> tags) {
        this(SzTracer, startTime, null, operationName, SzTracerSpanContext, tags);
    }

    public CommonLogSpan(SzTracer SzTracer, long startTime,
                         List<SzTracerSpanReferenceRelationship> spanReferences,
                         String operationName, SzTracerSpanContext SzTracerSpanContext,
                         Map<String, ?> tags) {
        super(SzTracer, startTime, spanReferences, operationName, SzTracerSpanContext, tags);
    }

    /**
     * Add an item to Slots that needs to be printed
     *
     * @param slot
     */
    public void addSlot(String slot) {
        if (slot == null) {
            slot = StringUtils.EMPTY_STRING;
        }

        if (slotCounter.incrementAndGet() <= MAX_SLOT_SIZE) {
            slots.add(slot);
        } else {
            SelfDefineLog.warn("Slots count（" + MAX_SLOT_SIZE + "）Fully");
        }
    }

    /**
     * Get all the content you need to print
     *
     * @return
     */
    public List<String> getSlots() {
        return slots;
    }

    /**
     * Add slot list
     *
     * @param stringArrayList
     */
    public void addSlots(List<String> stringArrayList) {
        if (stringArrayList == null || stringArrayList.isEmpty()) {
            return;
        }
        for (String slot : stringArrayList) {
            this.addSlot(slot);
        }
    }
}
