package com.wayyue.tracer.core.reporter.stat;

import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.reporter.stat.model.StatValues;
import com.wayyue.tracer.core.span.SzTracerSpan;

import java.util.Map;


/**
 * SzTracerStatisticReporter
 * <p>
 * Reference: {StatTracer}
 * </p>
 */
public interface SzTracerStatisticReporter {

    /**
     * get the period time
     * @return
     */
    long getPeriodTime();

    /**
     * Get the unique identifier of the statistic type
     * @return
     */
    String getStatTracerName();

    /**
     * Update data to the slot
     * @param sofaTracerSpan
     */
    void reportStat(SzTracerSpan sofaTracerSpan);

    /**
     * Switch the current subscript and return the stat before switching
     * @return
     */
    Map<StatKey, StatValues> shiftCurrentIndex();

    /**
     * When the method is called, it indicates that a cycle has passed,
     * to determine whether enough cycles have passed, and whether flush is needed.
     *
     * @return true:stat log can be printed and the framework will call {@link SzTracerStatisticReporter#print}
     */
    boolean shouldPrintNow();

    /**
     * Print, you can print to a local disk, or you can report to a remote server
     * @param statKey
     * @param values
     */
    void print(StatKey statKey, long[] values);

    /**
     * close print
     */
    void close();
}
