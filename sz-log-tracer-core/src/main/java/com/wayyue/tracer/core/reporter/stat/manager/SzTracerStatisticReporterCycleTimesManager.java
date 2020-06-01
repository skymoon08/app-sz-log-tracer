package com.wayyue.tracer.core.reporter.stat.manager;


import com.wayyue.tracer.core.reporter.stat.SzTracerStatisticReporter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SzTracerStatisticReporterCycleTimesManager
 *
 */
public class SzTracerStatisticReporterCycleTimesManager {

    private final static Map<Long, SzTracerStatisticReporterManager> cycleTimesManager = new ConcurrentHashMap<Long, SzTracerStatisticReporterManager>();

    public static Map<Long, SzTracerStatisticReporterManager> getCycleTimesManager() {
        return cycleTimesManager;
    }

    /**
     * period: second
     * @param statisticReporter statisticReporter
     */
    public static void registerStatReporter(SzTracerStatisticReporter statisticReporter) {
        SzTracerStatisticReporterManager tracerStatisticReporterManager = SzTracerStatisticReporterCycleTimesManager.getSzTracerStatisticReporterManager(statisticReporter.getPeriodTime());
        if (tracerStatisticReporterManager != null) {
            tracerStatisticReporterManager.addStatReporter(statisticReporter);
        }
    }

    /**
     * The timed task uses this as the entry: Get the scheduled task with the specified cycle time
     * @param cycleTime period: second
     * @return SofaTracerStatisticReporterManager Fixed-cycle task manager
     */
    public static SzTracerStatisticReporterManager getSzTracerStatisticReporterManager(Long cycleTime) {
        if (cycleTime == null) {
            return null;
        }
        if (cycleTime <= 0) {
            return null;
        }
        SzTracerStatisticReporterManager existedManager = cycleTimesManager.get(cycleTime);
        if (existedManager == null) {
            synchronized (cycleTimesManager) {
                if (cycleTimesManager.get(cycleTime) == null) {
                    cycleTimesManager.put(cycleTime, new SzTracerStatisticReporterManager(cycleTime));
                    existedManager = cycleTimesManager.get(cycleTime);
                }
            }
        }
        return existedManager;
    }
}
