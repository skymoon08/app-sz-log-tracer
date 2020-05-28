package com.wayyue.tracer.core.appender.sefllog;

import com.wayyue.tracer.core.appender.manager.AsyncCommonAppenderManager;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * FileName: SelfDefineLog
 * Author:   zhanglong
 * Date:     2020/5/27 17:34
 * Description:
 */
public class SelfDefineLog {

        static private final String               ERROR_PREFIX  = "[ERROR] ";
        static private final String               WARN_PREFIX   = "[WARN]  ";
        static private final String               INFO_PREFIX   = "[INFO]  ";

        /**
         * Log file name
         */
        static protected final String             SELF_LOG_FILE = "tracer-self.log";

        static private AsyncCommonAppenderManager selfLogAppenderManager;

        static {
        selfLogAppenderManager = new AsyncCommonAppenderManager(1024, SELF_LOG_FILE);
        selfLogAppenderManager.start("SelfLogAppender");
    }

        /**
         * @param log
         * @param e
         */
        public static void error(String log, Throwable e) {
        try {
            String timestamp = Timestamp.currentTime();
            StringWriter sw = new StringWriter(4096);
            PrintWriter pw = new PrintWriter(sw, false);
            pw.append(timestamp).append(ERROR_PREFIX).append(log).append(StringUtils.NEWLINE);
            e.printStackTrace(pw);
            pw.println();
            pw.flush();
            selfLogAppenderManager.append(sw.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

        /**
         * Print error log with current thread's TraceId
         *
         * @param log
         * @param e
         */
        public static void errorWithTraceId(String log, Throwable e) {
        try {
            String timestamp = Timestamp.currentTime();
            StringWriter sw = new StringWriter(4096);
            PrintWriter pw = new PrintWriter(sw, false);
            pw.append(timestamp).append(ERROR_PREFIX).append("[").append(TracerUtils.getTraceId())
                    .append("]").append(log).append(StringUtils.NEWLINE);
            e.printStackTrace(pw);
            pw.println();
            pw.flush();
            selfLogAppenderManager.append(sw.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

        public static void error(String log) {
        doLog(log, ERROR_PREFIX);
    }

        /**
         * Print error log with current thread's TraceId
         *
         * @param log
         * @param traceId traceId
         */
        public static void errorWithTraceId(String log, String traceId) {
        doLog(log, ERROR_PREFIX + "[" + traceId + "]");
    }

        /**
         * Print error log with current thread's TraceId
         *
         * @param log
         */
        public static void errorWithTraceId(String log) {
        doLog(log, ERROR_PREFIX + "[" + TracerUtils.getTraceId() + "]");
    }

        public static void warn(String log) {
        doLog(log, WARN_PREFIX);
    }

        public static void info(String log) {
        doLog(log, INFO_PREFIX);
    }

        public static void infoWithTraceId(String log) {
        doLog(log, INFO_PREFIX + "[" + TracerUtils.getTraceId() + "]");
    }

        public static void flush() {
        //async flush, do nothing.
    }

        static private void doLog(String log, String prefix) {
        try {
            String timestamp = Timestamp.currentTime();
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append(prefix).append(log).append(StringUtils.NEWLINE);
            selfLogAppenderManager.append(sb.toString());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
