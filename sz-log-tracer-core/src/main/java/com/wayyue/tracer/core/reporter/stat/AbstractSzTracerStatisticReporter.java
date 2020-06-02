package com.wayyue.tracer.core.reporter.stat;

import com.wayyue.tracer.core.appender.TraceAppender;
import com.wayyue.tracer.core.appender.builder.JsonStringBuilder;
import com.wayyue.tracer.core.appender.builder.XStringBuilder;
import com.wayyue.tracer.core.appender.file.LoadTestAwareAppender;
import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.appender.sefllog.Timestamp;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.reporter.stat.manager.SzTracerStatisticReporterCycleTimesManager;
import com.wayyue.tracer.core.reporter.stat.manager.SzTracerStatisticReporterManager;
import com.wayyue.tracer.core.reporter.stat.model.StatKey;
import com.wayyue.tracer.core.reporter.stat.model.StatMapKey;
import com.wayyue.tracer.core.reporter.stat.model.StatValues;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.AssertUtils;
import com.wayyue.tracer.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractSzTracerStatisticReporter
 *
 */
public abstract class AbstractSzTracerStatisticReporter implements SzTracerStatisticReporter {

    /**
     * The default period is 0 (starting at 0), that is,
     * the output interval is a cycle time (how long a cycle can be set, the default is 60s),
     * {@link com.wayyue.tracer.core.reporter.stat.manager.SzTracerStatisticReporterManager#DEFAULT_CYCLE_SECONDS}
     */
    public static final int DEFAULT_CYCLE = 0;

    /**
     * Used to control the concurrency when initializing the slot
     */
    private static final ReentrantLock initLock = new ReentrantLock(false);

    private static XStringBuilder buffer = new XStringBuilder();
    private static JsonStringBuilder jsonBuffer = new JsonStringBuilder();

    /**
     * Whether to turn off stat log print, the default is not closed
     */
    protected AtomicBoolean isClosePrint = new AtomicBoolean(false);

    protected TraceAppender appender = null;

    /**
     * The name of the stat log
     */
    protected String statTracerName;

    /**
     * period time(Unit:second)
     */
    private long periodTime;
    private String rollingPolicy;
    private String logReserveConfig;
    /**
     * Output cycle interval
     */
    private int printCycle = 0;

    /**
     * The number of cycles currently counted
     */
    private long countCycle = 0;

    /**
     * "Statistics" scrolling array
     */
    private Map<StatKey, StatValues>[] statDatasPair = new ConcurrentHashMap[2];

    /**
     * The current subscript of the "statistics" scrolling array
     */
    private int currentIndex = 0;

    /**
     * Statistical data
     */
    protected Map<StatKey, StatValues> statDatas;

    public AbstractSzTracerStatisticReporter(String statTracerName, String rollingPolicy,
                                             String logReserveConfig) {
        this(statTracerName, SzTracerStatisticReporterManager.DEFAULT_CYCLE_SECONDS,
                DEFAULT_CYCLE, rollingPolicy, logReserveConfig);
    }

    public AbstractSzTracerStatisticReporter(String statTracerName, long periodTime,
                                             int outputCycle, String rollingPolicy,
                                             String logReserveConfig) {

        AssertUtils.hasText(statTracerName, "Statistics tracer name cat't be empty.");
        this.statTracerName = statTracerName;
        this.periodTime = this.globalConfiguredCycleTime(periodTime);
        this.printCycle = outputCycle;
        this.rollingPolicy = rollingPolicy;
        this.logReserveConfig = logReserveConfig;
        for (int i = 0; i < 2; i++) {
            this.statDatasPair[i] = new ConcurrentHashMap<StatKey, StatValues>(100);
        }
        this.statDatas = statDatasPair[currentIndex];
        //Register a scheduled task and start
        SzTracerStatisticReporterCycleTimesManager.registerStatReporter(this);
    }

    /**
     * Get the output interval of the stat log
     *
     * @param defaultCycle default interval is 60s
     * @return
     */
    private long globalConfiguredCycleTime(long defaultCycle) {
        long cycleTime = defaultCycle;
        try {
            String statLogInterval = SzTracerConfiguration
                    .getProperty(SzTracerConfiguration.STAT_LOG_INTERVAL);
            if (StringUtils.isNotBlank(statLogInterval)) {
                cycleTime = Long.parseLong(statLogInterval);
            }
        } catch (Exception e) {
            SelfDefineLog.error("Parse stat log interval configure error", e);
        }
        SelfDefineLog.warn(this.getStatTracerName() + " configured "
                + SzTracerConfiguration.STAT_LOG_INTERVAL + "=" + cycleTime
                + " second and default cycle=" + defaultCycle);
        return cycleTime;
    }

    @Override
    public long getPeriodTime() {
        return this.periodTime;
    }

    @Override
    public String getStatTracerName() {
        return this.statTracerName;
    }

    @Override
    public void reportStat(SzTracerSpan szTracerSpan) {
        if (szTracerSpan == null) {
            return;
        }
        if (this.appender == null) {
            synchronized (this) {
                if (this.appender == null) {
                    this.appender = LoadTestAwareAppender
                            .createLoadTestAwareTimedRollingFileAppender(statTracerName, rollingPolicy, logReserveConfig);
                }
            }
        }
        this.doReportStat(szTracerSpan);
    }

    /**
     * report stat log,and call {@link AbstractSzTracerStatisticReporter#addStat}
     *
     * @param SzTracerSpan
     */
    public abstract void doReportStat(SzTracerSpan SzTracerSpan);

    /**
     * By default, only the accumulated stat methods are provided.
     * <p>
     * Update the data to the slot. The front is the unique key, followed by the numeric column.
     * The statistical calculation adds the numeric columns of different keys.
     *
     * @param keys   Unique identifier of the key being counted
     * @param values Statistical value
     */
    protected void addStat(StatKey keys, long... values) {
        StatValues oldValues = statDatas.get(keys);
        if (oldValues == null) {
            // need to lock and double judgment
            initLock.lock();
            try {
                oldValues = statDatas.get(keys);
                // check whether the maximum key limit is exceeded
                if (null == oldValues) {
                    // Create a slot with specified value
                    oldValues = new StatValues(values);
                    statDatas.put(keys, oldValues);
                    return;
                }
            } finally {
                initLock.unlock();
            }
        }
        // Other threads have created slots and merge new data
        if (oldValues != null) {
            oldValues.update(values);
        }
    }

    /**
     * Switch the current subscript and return the stat before switching
     */
    @Override
    public Map<StatKey, StatValues> shiftCurrentIndex() {
        Map<StatKey, StatValues> last = statDatasPair[currentIndex];
        currentIndex = 1 - currentIndex;
        statDatas = statDatasPair[currentIndex];
        return last;
    }

    /**
     * Return the currently statistical data
     *
     * @return
     */
    public Map<StatKey, StatValues> getStatData() {
        return new HashMap<>(statDatas);
    }

    /**
     * Get another set of data that is not currently being counted,
     * Total: two sets of data for statistics and printing
     *
     * @return
     */
    public Map<StatKey, StatValues> getOtherStatData() {
        return new HashMap<>(statDatasPair[1 - currentIndex]);
    }

    @Override
    public boolean shouldPrintNow() {
        return 0 == (countCycle >= printCycle ? countCycle = 0 : ++countCycle);
    }

    @Override
    public void print(StatKey statKey, long[] values) {
        if (this.isClosePrint.get()) {
            //Close the statistics log output
            return;
        }
        if ("false".equalsIgnoreCase(SzTracerConfiguration.getProperty(SzTracerConfiguration.JSON_FORMAT_OUTPUT))) {
            printXsbStat(statKey, values);
        } else {
            printJsbStat(statKey, values);
        }
    }

    protected void printXsbStat(StatKey statKey, long[] values) {
        try {
            buffer.reset();
            buffer.append(Timestamp.currentTime()).append(statKey.getKey());
            int i = 0;
            for (; i < values.length - 1; i++) {
                buffer.append(values[i]);
            }
            buffer.append(values[i]);
            buffer.append(statKey.getResult());
            buffer.appendEnd(statKey.getEnd());
            if (appender instanceof LoadTestAwareAppender) {
                ((LoadTestAwareAppender) appender).append(buffer.toString(), statKey.isLoadTest());
            } else {
                appender.append(buffer.toString());
            }
            // Forced to flush
            appender.flush();
        } catch (Throwable t) {
            SelfDefineLog.error("Stat log <" + statTracerName + "> output error!", t);
        }
    }

    protected void printJsbStat(StatKey statKey, long[] values) {

        if (!(statKey instanceof StatMapKey)) {
            return;
        }
        StatMapKey statMapKey = (StatMapKey) statKey;
        try {
            jsonBuffer.reset();
            jsonBuffer.appendBegin();
            jsonBuffer.append(CommonSpanTags.TIME, Timestamp.currentTime());
            jsonBuffer.append(CommonSpanTags.STAT_KEY, this.statKeySplit(statMapKey));
            jsonBuffer.append(CommonSpanTags.COUNT, values[0]);
            jsonBuffer.append(CommonSpanTags.TOTAL_COST_MILLISECONDS, values[1]);
            jsonBuffer.append(CommonSpanTags.SUCCESS, statMapKey.getResult());
            //pressure test mark
            jsonBuffer.appendEnd(CommonSpanTags.LOAD_TEST, statMapKey.getEnd());
            if (appender instanceof LoadTestAwareAppender) {
                ((LoadTestAwareAppender) appender).append(jsonBuffer.toString(), statMapKey.isLoadTest());
            } else {
                appender.append(jsonBuffer.toString());
            }
            // Forced to flush
            appender.flush();
        } catch (Throwable t) {
            SelfDefineLog.error("Stat log<" + statTracerName + "> error!", t);
        }
    }

    private String statKeySplit(StatMapKey statKey) {
        JsonStringBuilder jsonBufferKey = new JsonStringBuilder();
        Map<String, String> keyMap = statKey.getKeyMap();
        jsonBufferKey.appendBegin();
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            jsonBufferKey.append(entry.getKey(), entry.getValue());
        }
        jsonBufferKey.appendEnd(false);
        return jsonBufferKey.toString();
    }

    @Override
    public void close() {
        this.isClosePrint.set(true);
    }

    public AtomicBoolean getIsClosePrint() {
        return isClosePrint;
    }

    public void setIsClosePrint(AtomicBoolean isClosePrint) {
        if (isClosePrint == null) {
            return;
        }
        this.isClosePrint.set(isClosePrint.get());
    }

    protected String buildString(String[] keys) {
        XStringBuilder sb = new XStringBuilder();
        int i;
        for (i = 0; i < keys.length - 1; i++) {
            sb.append(keys[i] == null ? "" : keys[i]);
        }
        sb.appendRaw(keys[i] == null ? "" : keys[i]);
        return sb.toString();
    }

    protected boolean isHttpOrMvcSuccess(String resultCode) {
        return resultCode.charAt(0) == '1' || resultCode.charAt(0) == '2'
                || "302".equals(resultCode.trim()) || ("301".equals(resultCode.trim()));
    }

    protected boolean isWebHttpClientSuccess(String resultCode) {
        return StringUtils.isNotBlank(resultCode)
                && (isHttpOrMvcSuccess(resultCode) || SzTracerConstant.RESULT_CODE_SUCCESS
                .equals(resultCode));
    }
}
