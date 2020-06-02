package com.wayyue.tracer.core.appender;


import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.appender.sefllog.TracerDaemon;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.core.utils.TracerUtils;

import java.io.File;
import java.nio.charset.Charset;

/**
 * TracerLogRootDaemon
 * <p>
 * Not obtained from the configuration project, obtained directly from the system properties
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class TracerLogRootDaemon {

    /**
     * Whether to add pid to log path
     */
    public static final String  TRACER_APPEND_PID_TO_LOG_PATH_KEY = "tracer_append_pid_to_log_path";

    /**
     * Log directory
     */
    public static String        LOG_FILE_DIR;
    /**
     * The encoding is determined by LANG or -Dfile.encoding,
     * so if the system determines the log encoding based on the system encoding,
     * make sure that the application's startup script or startup parameters do not override the LANG or -Dfile.encoding parameters.
     * Generally speaking, in the domestic system creation template,
     * there is LANG=zh_CN.GB18030 in deploy/bin/templates/jbossctl.sh,
     * so no matter what value LANG is set in the environment variable, it will be overwritten at startup.
     */
    static public final Charset DEFAULT_CHARSET                   = Charset.defaultCharset();

    static {
        String loggingRoot = System.getProperty("SZ_TRACER_LOGGING_PATH");
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getenv("SZ_TRACER_LOGGING_PATH");
        }
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("loggingRoot");
        }
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("logging.path");
        }
        if (StringUtils.isBlank(loggingRoot)) {
            loggingRoot = System.getProperty("user.home") + File.separator + "logs";
        }

        String appendPidToLogPathString = System.getProperty(TRACER_APPEND_PID_TO_LOG_PATH_KEY);
        boolean appendPidToLogPath = "true".equalsIgnoreCase(appendPidToLogPathString);

        String tempLogFileDir = loggingRoot + File.separator + "tracelog";

        if (appendPidToLogPath) {
            tempLogFileDir = tempLogFileDir + File.separator + TracerUtils.getPID();
        }

        LOG_FILE_DIR = tempLogFileDir;

        try {
            TracerDaemon.start();
            SelfDefineLog.info("LOG_FILE_DIR is " + LOG_FILE_DIR);
        } catch (Throwable e) {
            SelfDefineLog.error("Failed to start Tracer Daemon Thread", e);
        }
    }
}
