package com.wayyue.tracer.core.reporter.stat.manager;


import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.reporter.stat.SzTracerStatisticReporter;
import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.reporter.stat.model.StatValues;
import com.wayyue.tracer.core.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SzTracerStatisticReporterManager
 * <p>
 * Reporter with a fixed time period, one clock cycle corresponds to one instance, and the cycle is started after initialization
 * </p>
 */
public class SzTracerStatisticReporterManager {

    /**
     * Threshold, if the number of keys in the stat log (map format) exceeds this value, the map is cleared, non-final for testability
     */
    public static int CLEAR_STAT_KEY_THRESHOLD = 5000;

    /**
     * The default output period is 60 seconds.
     */
    public static final long DEFAULT_CYCLE_SECONDS = 60;

    /**
     * Thread count
     */
    static final AtomicInteger THREAD_NUMBER = new AtomicInteger(0);

    /**
     * Every fixed-cycle schedule will have such an instance.
     */
    private Map<String, SzTracerStatisticReporter> statReporters = new ConcurrentHashMap<String, SzTracerStatisticReporter>();

    /**
     * Period time, default {@link SzTracerStatisticReporterManager#DEFAULT_CYCLE_SECONDS}=60 s
     */
    private long cycleTime;

    private ScheduledExecutorService executor;

    SzTracerStatisticReporterManager() {
        this(DEFAULT_CYCLE_SECONDS);
    }

    SzTracerStatisticReporterManager(final long cycleTime) {
        this.cycleTime = cycleTime;
        this.executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread thread = new Thread(r, "Tracer-TimedAppender-"
                        + THREAD_NUMBER.incrementAndGet() + "-"
                        + cycleTime);
                thread.setDaemon(true);
                return thread;
            }
        });
        start();
    }

    private void start() {
        executor.scheduleAtFixedRate(new StatReporterPrinter(), 0, cycleTime, TimeUnit.SECONDS);
    }

    /**
     * Get a stat Reporter instance by statTracerName
     *
     * @param statTracerName Stat log tracer name
     * @return
     */
    public SzTracerStatisticReporter getStatTracer(String statTracerName) {
        if (StringUtils.isBlank(statTracerName)) {
            return null;
        }
        return statReporters.get(statTracerName);
    }

    /**
     * Save Stat Reporter instance
     *
     * @param statisticReporter statisticReporter
     */
    public synchronized void addStatReporter(SzTracerStatisticReporter statisticReporter) {
        if (statisticReporter == null) {
            return;
        }
        String statTracerName = statisticReporter.getStatTracerName();
        if (statReporters.containsKey(statTracerName)) {
            return;
        }
        statReporters.put(statTracerName, statisticReporter);
    }

    public Map<String, SzTracerStatisticReporter> getStatReporters() {
        return statReporters;
    }

    class StatReporterPrinter implements Runnable {
        @Override
        public void run() {
            SzTracerStatisticReporter st = null;
            try {
                // once/60s
                for (SzTracerStatisticReporter statTracer : statReporters.values()) {
                    if (statTracer.shouldPrintNow()) {
                        st = statTracer;
                        // Switch subscripts and get statDatas for a while
                        Map<StatKey, StatValues> statDatas = statTracer.shiftCurrentIndex();
                        for (Map.Entry<StatKey, StatValues> e : statDatas.entrySet()) {
                            StatKey statKeys = e.getKey();
                            StatValues values = e.getValue();
                            // print log
                            long[] tobePrint = values.getCurrentValue();
                            // print when the count is greater than 0
                            if (tobePrint[0] > 0) {
                                statTracer.print(statKeys, tobePrint);
                            }
                            // Update the slot value to clear the printed content
                            // Here you must ensure that the input params is the value of the array used in the print process.
                            values.clear(tobePrint);
                        }
                        // If the number of keys in the statistics log is greater than the threshold,
                        // it indicates that the key may have variable parameters,
                        // so clearing it prevents taking up too much memory.
                        if (statDatas.size() > CLEAR_STAT_KEY_THRESHOLD) {
                            statDatas.clear();
                        }
                    }
                }
            } catch (Throwable t) {
                if (st != null) {
                    SelfDefineLog.error("Stat log <" + st.getStatTracerName() + "> flush failure.", t);
                }
            }

        }
    }
}
