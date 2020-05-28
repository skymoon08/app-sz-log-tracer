package com.wayyue.tracer.core.appender.sefllog;

import com.wayyue.tracer.core.appender.TraceAppender;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracer's Daemon thread, mainly to do the cleanup log
 *
 */
public class TracerDaemon implements Runnable {

    private static final long          ONE_HOUR         = 60 * 60;
    private static AtomicBoolean       running          = new AtomicBoolean(false);
    private static List<TraceAppender> watchedAppenders = new CopyOnWriteArrayList<TraceAppender>();
    private static long                scanInterval     = ONE_HOUR;

    /**
     * Register the Appender being monitored
     *
     * @param traceAppender
     */
    public static void watch(TraceAppender traceAppender) {
        watchedAppenders.add(traceAppender);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            try {
                for (TraceAppender traceAppender : watchedAppenders) {
                    traceAppender.cleanup();
                }

                TimeUnit.SECONDS.sleep(scanInterval);
            } catch (Throwable e) {
                SelfDefineLog.error("Error occurred while cleaning up logs.", e);
            }
        }
    }

    /**
     * Adjust the scanning cycle of the Daemon thread for testing convenience
     *
     * @param scanInterval Scan period in seconds
     */
    public static void setScanInterval(long scanInterval) {
        TracerDaemon.scanInterval = scanInterval;
    }

    public static void start() {
        if (running.compareAndSet(false, true)) {
            Thread deleteLogThread = new Thread(new TracerDaemon());
            deleteLogThread.setDaemon(true);
            deleteLogThread.setName("Tracer-Daemon-Thread");
            deleteLogThread.start();
        }
    }
}
