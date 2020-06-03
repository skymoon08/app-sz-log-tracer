package com.wayyue.tracer.core.appender.manager;


import disruptor.event.EventFactory;

/**
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class StringEventFactory implements EventFactory<StringEvent> {
    @Override
    public StringEvent newInstance() {
        return new StringEvent();
    }
}