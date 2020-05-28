package com.wayyue.tracer.core.reporter.digest.manager;


import com.wayyue.tracer.core.appender.manager.AsyncCommonDigestAppenderManager;

/**
 * SzTracerDigestReporterAsyncManager
 *
 */
public final class SzTracerDigestReporterAsyncManager {

    /**
     * Asynchronous log print, all middleware digest logs share a SofaTracerDigestReporterAsyncManager AsyncAppender to print logs
     */
    private static volatile AsyncCommonDigestAppenderManager asyncCommonDigestAppenderManager;

    /**
     * get singleton instance
     * @return asyncCommonDigestAppenderManager
     */
    public static AsyncCommonDigestAppenderManager getSzTracerDigestReporterAsyncManager() {
        if (asyncCommonDigestAppenderManager == null) {
            synchronized (SzTracerDigestReporterAsyncManager.class) {
                if (asyncCommonDigestAppenderManager == null) {
                    AsyncCommonDigestAppenderManager localManager = new AsyncCommonDigestAppenderManager(
                        1024);
                    localManager.start("NetworkAppender");
                    asyncCommonDigestAppenderManager = localManager;
                }
            }
        }
        return asyncCommonDigestAppenderManager;
    }
}
