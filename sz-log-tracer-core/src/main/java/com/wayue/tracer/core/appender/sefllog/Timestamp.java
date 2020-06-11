package com.wayue.tracer.core.appender.sefllog;

import java.text.SimpleDateFormat;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * FileName: Timestamp
 * Author:   zhanglong
 * Date:     2020/5/27 21:26
 * Description:
 */
public class Timestamp  {

    public static String currentTime() {
        return getSimpleDateFormat().format(System.currentTimeMillis());
    }

    public static String format(long time) {
        return getSimpleDateFormat().format(time);
    }

    private static SimpleDateFormat getSimpleDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
}