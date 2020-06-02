package com.wayyue.tracer.core.appender.manager;


import disruptor.event.EventFactory;

/**
 *
 * @author jinming.xiao
 * @since 2020/06/01
 * @version $Id: StringEventFactory.java, v 0.1 November 21, 2017 7:00 PM luoguimu123 Exp $
 */
public class StringEventFactory implements EventFactory<StringEvent> {
    @Override
    public StringEvent newInstance() {
        return new StringEvent();
    }
}