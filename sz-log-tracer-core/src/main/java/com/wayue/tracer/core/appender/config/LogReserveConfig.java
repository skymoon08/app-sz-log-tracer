package com.wayue.tracer.core.appender.config;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * FileName: LogReserveConfig
 * Author:   zhanglong
 * Date:     2020/5/27 20:52
 * Description:
 */
public class LogReserveConfig {
    private int day;
    private int hour;

    public LogReserveConfig(int day, int hour) {
        this.hour = hour;
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }
}