package com.wayue.tracer.core.appender.manager;


public class StringEvent {

    private volatile String string;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}