package com.wayyue.tracer.core.appender.info;


import com.wayyue.tracer.core.appender.TraceAppender;
import com.wayyue.tracer.core.appender.file.TimedRollingFileAppender;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.io.IOException;

/**
 * StaticInfoLog
 *
 */
public class StaticInfoLog {

    static private TraceAppender appender;

    public synchronized static void logStaticInfo() {
        try {
            if (appender == null) {
                appender = new TimedRollingFileAppender("static-info.log", true);
            }
            String log = TracerUtils.getPID() + ",";
            log = log + (TracerUtils.getInetAddress() + ",");
            log = log + (TracerUtils.getCurrentZone() + ",");
            log = log + (TracerUtils.getDefaultTimeZone());
            appender.append(log + "\n");
            appender.flush();
        } catch (IOException e) {
            SelfDefineLog.error("", e);
        }
    }
}
