package com.wayue.tracer.core.reporter.facade;


import com.wayue.tracer.core.span.SzTracerSpan;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractDiskReporter
 *
 * Abstract class definition for the Reporter
 */
public abstract class AbstractReporter implements Reporter {

    /**
     * Whether to turn off digest log print, the default is not closed;
     * closing means closing the digest and stat log
     */
    private AtomicBoolean isClosePrint = new AtomicBoolean(false);

    /**
     * report span
     * @param span
     */
    @Override
    public void report(SzTracerSpan span) {
        if (span == null) {
            return;
        }
        //close print
        if (isClosePrint.get()) {
            return;
        }
        this.doReport(span);
    }

    /**
     * Subclass needs to implement the report method
     * @param span
     */
    public abstract void doReport(SzTracerSpan span);

    @Override
    public void close() {
        isClosePrint.set(true);
    }

    public AtomicBoolean getIsClosePrint() {
        return isClosePrint;
    }

    public void setIsClosePrint(AtomicBoolean isClosePrint) {
        if (isClosePrint == null) {
            return;
        }
        this.isClosePrint.set(isClosePrint.get());
    }
}
