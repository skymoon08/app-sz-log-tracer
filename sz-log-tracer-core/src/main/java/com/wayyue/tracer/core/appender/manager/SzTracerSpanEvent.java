package com.wayyue.tracer.core.appender.manager;


import com.wayyue.tracer.core.span.SzTracerSpan;

/**
 */
public class SzTracerSpanEvent {
    private volatile SzTracerSpan szTracerSpan;

    /**
     * Getter method for property <tt>szTracerSpan</tt>.
     *
     * @return property value of szTracerSpan
     */
    public SzTracerSpan getTracerSpan() {
        return szTracerSpan;
    }

    /**
     * Setter method for property <tt>szTracerSpan</tt>.
     *
     * @param szTracerSpan  value to be assigned to property szTracerSpan
     */
    public void setTracerSpan(SzTracerSpan szTracerSpan) {
        this.szTracerSpan = szTracerSpan;
    }
}