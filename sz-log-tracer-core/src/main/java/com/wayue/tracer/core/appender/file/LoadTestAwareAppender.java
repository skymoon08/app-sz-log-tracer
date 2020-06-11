package com.wayue.tracer.core.appender.file;


import com.wayue.tracer.core.appender.TraceAppender;

import java.io.File;
import java.io.IOException;

/**
 * LoadTestAwareAppender
 *
 */
public final class LoadTestAwareAppender implements TraceAppender {

    /** TraceAppender for non-pressure */
    private TraceAppender nonLoadTestTraceAppender;
    /** TraceAppender for pressure*/
    private TraceAppender loadTestTraceAppender;

    private LoadTestAwareAppender(TraceAppender nonLoadTestTraceAppender,
                                  TraceAppender loadTestTraceAppender) {
        this.nonLoadTestTraceAppender = nonLoadTestTraceAppender;
        this.loadTestTraceAppender = loadTestTraceAppender;
    }

    public static LoadTestAwareAppender createLoadTestAwareTimedRollingFileAppender(String logName, boolean append) {
        TraceAppender nonLoadTestTraceAppender = new TimedRollingFileAppender(logName, append);
        TraceAppender loadTestTraceAppender = new TimedRollingFileAppender("shadow"+ File.separator+ logName, append);
        return new LoadTestAwareAppender(nonLoadTestTraceAppender, loadTestTraceAppender);
    }

    public static LoadTestAwareAppender createLoadTestAwareTimedRollingFileAppender(String logName,String rollingPolicy,String logReserveConfig) {
        TraceAppender nonLoadTestTraceAppender = new TimedRollingFileAppender(logName, rollingPolicy, logReserveConfig);
        TraceAppender loadTestTraceAppender = new TimedRollingFileAppender("shadow" + File.separator + logName, rollingPolicy, logReserveConfig);
        return new LoadTestAwareAppender(nonLoadTestTraceAppender, loadTestTraceAppender);
    }

    public void append(String log, boolean loadTest) throws IOException {
        if (loadTest) {
            loadTestTraceAppender.append(log);
        } else {
            nonLoadTestTraceAppender.append(log);
        }
    }

    @Override
    public void flush() throws IOException {
        nonLoadTestTraceAppender.flush();
        loadTestTraceAppender.flush();
    }

    @Override
    public void append(String log) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanup() {
        nonLoadTestTraceAppender.cleanup();
        loadTestTraceAppender.cleanup();
    }
}
