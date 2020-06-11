package com.wayue.tracer.core.appender.manager;


import disruptor.event.EventFactory;

public class SzTracerSpanEventFactory implements EventFactory<SzTracerSpanEvent> {

    @Override
    public SzTracerSpanEvent newInstance() {
        return new SzTracerSpanEvent();
    }
}