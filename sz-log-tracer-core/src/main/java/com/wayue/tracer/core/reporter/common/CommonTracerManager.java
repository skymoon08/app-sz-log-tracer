package com.wayue.tracer.core.reporter.common;


import com.wayue.tracer.core.appender.TraceAppender;
import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.appender.file.LoadTestAwareAppender;
import com.wayue.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;
import com.wayue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.reporter.type.TracerSystemLogEnum;
import com.wayue.tracer.core.span.CommonLogSpan;
import com.wayue.tracer.core.utils.StringUtils;

/**
 * CommonTracerManager
 *
 * @author jinming.xiao
 * @since 2020/06/01
 * @since 2017/06/28
 */
public class CommonTracerManager {

    /**
     * Asynchronous log print, all middleware common to print general logs
     */
    private static volatile AsyncCommonDigestAppenderManager commonReporterAsyncManager = new AsyncCommonDigestAppenderManager(
                                                                                            1024);

    private static SpanEncoder commonSpanEncoder = new CommonSpanEncoder();

    static {
        String logName = TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName();
        TraceAppender traceAppender = LoadTestAwareAppender
                .createLoadTestAwareTimedRollingFileAppender(logName,
                        SzTracerConfiguration.getProperty(TracerSystemLogEnum.MIDDLEWARE_ERROR.getRollingKey()),
                        SzTracerConfiguration.getProperty(TracerSystemLogEnum.MIDDLEWARE_ERROR.getLogReverseKey()));
        commonReporterAsyncManager.addAppender(logName, traceAppender, commonSpanEncoder);

        String profileLogName = TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName();
        TraceAppender profileTraceAppender = LoadTestAwareAppender
                .createLoadTestAwareTimedRollingFileAppender(profileLogName,
                        SzTracerConfiguration.getProperty(TracerSystemLogEnum.RPC_PROFILE.getRollingKey()),
                        SzTracerConfiguration.getProperty(TracerSystemLogEnum.RPC_PROFILE.getLogReverseKey()));
        commonReporterAsyncManager.addAppender(profileLogName, profileTraceAppender, commonSpanEncoder);
        //start
        commonReporterAsyncManager.start("CommonProfileErrorAppender");
    }

    /**
     * Register a general log
     * @param logFileName   logFileName
     * @param rollingPolicy rollingPolicy
     * @param logReserveDay logReserveDay
     */
    public static void register(String logFileName, String rollingPolicy, String logReserveDay) {
        if (StringUtils.isBlank(logFileName)) {
            return;
        }
        if (commonReporterAsyncManager.isAppenderAndEncoderExist(logFileName)) {
            SelfDefineLog.warn(logFileName + " has existed in CommonTracerManager");
            return;
        }
        TraceAppender traceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(logFileName, rollingPolicy, logReserveDay);
        commonReporterAsyncManager.addAppender(logFileName, traceAppender, commonSpanEncoder);
    }

    /**
     * Deprecated registration method
     * @param logType       logType
     * @param logFileName   logFileName
     * @param rollingPolicy rollingPolicy
     * @param logReserveDay logReserveDay
     */
    @Deprecated
    public static void register(char logType, String logFileName, String rollingPolicy,
                                String logReserveDay) {
        String logTypeStr = new String(new char[] { logType });
        if (CommonTracerManager.isAppenderExist(logTypeStr)) {
            SelfDefineLog.warn(logTypeStr + " has existed in CommonTracerManager");
            return;
        }
        TraceAppender traceAppender = LoadTestAwareAppender
            .createLoadTestAwareTimedRollingFileAppender(logFileName, rollingPolicy, logReserveDay);
        commonReporterAsyncManager.addAppender(logTypeStr, traceAppender, commonSpanEncoder);
    }

    /**
     * Determine if the output of the specified log type meets the requirements
     * @param logType logType
     * @return true:exist
     */
    public static boolean isAppenderExist(String logType) {
        if (StringUtils.isBlank(logType)) {
            return false;
        }
        return commonReporterAsyncManager.isAppenderAndEncoderExist(logType);
    }

    /**
     * Note: The logType of this {@link CommonLogSpan} must be set, otherwise it will not print.
     * @param commonLogSpan The span will be printed
     */
    public static void reportCommonSpan(CommonLogSpan commonLogSpan) {
        if (commonLogSpan == null) {
            return;
        }
        String logType = commonLogSpan.getLogType();
        if (StringUtils.isBlank(logType)) {
            SelfDefineLog.error("Log Type can't be empty when report!");
            return;
        }
        commonReporterAsyncManager.append(commonLogSpan);
    }

    public static void reportProfile(CommonLogSpan szTracerSpan) {
        if (szTracerSpan == null) {
            return;
        }
        szTracerSpan.setLogType(TracerSystemLogEnum.RPC_PROFILE.getDefaultLogName());
        commonReporterAsyncManager.append(szTracerSpan);

    }

    public static void reportError(CommonLogSpan szTracerSpan) {
        if (szTracerSpan == null) {
            return;
        }
        szTracerSpan.setLogType(TracerSystemLogEnum.MIDDLEWARE_ERROR.getDefaultLogName());
        commonReporterAsyncManager.append(szTracerSpan);
    }
}
