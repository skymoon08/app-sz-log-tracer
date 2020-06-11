package com.wayue.tracer.core.appender.file;

import com.wayue.tracer.core.appender.config.LogReserveConfig;
import com.wayue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayue.tracer.core.appender.sefllog.TracerDaemon;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.utils.StringUtils;
import com.wayue.tracer.core.utils.TracerUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * FileName: TimedRollingFileAppender
 * Author:   zhanglong
 * Date:     2020/5/27 20:49
 * Description:
 */
public class TimedRollingFileAppender extends AbstractRollingFileAppender {

    /**
     * The code assumes that the following constants are in a increasing sequence.
     */
    static final int            TOP_OF_TROUBLE          = -1;
    /**
     * Adding seconds of scrolling is mainly for testing convenience
     */
    static final int            TOP_OF_SECONDS          = 0;
    static final int            TOP_OF_MINUTE           = 1;
    static final int            TOP_OF_HOUR             = 2;
    static final int            HALF_DAY                = 3;
    static final int            TOP_OF_DAY              = 4;
    static final int            TOP_OF_WEEK             = 5;
    static final int            TOP_OF_MONTH            = 6;

    static final TimeZone gmtTimeZone             = TimeZone.getTimeZone("GMT");

    public static final String  DAILY_ROLLING_PATTERN   = "'.'yyyy-MM-dd";
    public static final String  HOURLY_ROLLING_PATTERN  = "'.'yyyy-MM-dd_HH";
    private static final String DEFAULT_ROLLING_PATTERN = DAILY_ROLLING_PATTERN;

    /**
     * The next time you log RollOver, the log file will be renamed to this file.
     */
    private String              scheduledFilename;
    /**
     * Expect the time of the next RollOver
     */
    private long                nextCheck               = System.currentTimeMillis() - 1;
    /**
     * Suffix mode of the backed up file
     */
    private String              datePattern;
    /**
     * Date formatting, mainly used to format file names
     */
    private SimpleDateFormat sdf;

    private Date now                     = new Date();
    /**
     * This Calender is mainly used to calculate the time when the next RollOver occurs.
     */
    private RollingCalendar     rc                      = new RollingCalendar();

    /**
     * Log retention time
     */
    private LogReserveConfig logReserveConfig        = new LogReserveConfig(SzTracerConfiguration.DEFAULT_LOG_RESERVE_DAY, 0);

    public TimedRollingFileAppender(String file, boolean append) {
        this(file, DEFAULT_BUFFER_SIZE, append, DEFAULT_ROLLING_PATTERN);
    }

    public TimedRollingFileAppender(String file, String datePattern) {
        this(file, DEFAULT_BUFFER_SIZE, true, datePattern);
    }

    public TimedRollingFileAppender(String file, String datePattern, String logReserveConfigString) {
        this(file, DEFAULT_BUFFER_SIZE, true, datePattern);
        this.logReserveConfig = TracerUtils.parseLogReserveConfig(logReserveConfigString);
    }

    public TimedRollingFileAppender(String file, int bufferSize, boolean append) {
        this(file, bufferSize, append, DEFAULT_ROLLING_PATTERN);
    }

    /**
     * @param file fileName
     * @param bufferSize bufferSize
     * @param append default is true
     * @param datePatternParam date format
     */
    public TimedRollingFileAppender(String file, int bufferSize, boolean append, String datePatternParam) {
        super(file, bufferSize, append);
        if (StringUtils.isBlank(datePatternParam)) {
            this.datePattern = DEFAULT_ROLLING_PATTERN;
        } else {
            this.datePattern = datePatternParam;
        }
        sdf = new SimpleDateFormat(this.datePattern);
        rc.setType(computeCheckPeriod());
        scheduledFilename = fileName + sdf.format(new Date(logFile.lastModified()));
        TracerDaemon.watch(this);
    }

    /**
     * Determine if RollOver should be done now
     * @return true:Now RollOver
     */
    @Override
    public boolean shouldRollOverNow() {
        long n = System.currentTimeMillis();
        if (n >= nextCheck) {
            now.setTime(n);
            nextCheck = rc.getNextCheckMillis(now);
            return true;
        }

        return false;
    }

    /**
     * clean log
     */
    @Override
    public void cleanup() {
        try {
            File parentDirectory = logFile.getParentFile();
            if (parentDirectory == null || !parentDirectory.isDirectory()) {
                return;
            }

            final String baseName = logFile.getName();

            if (StringUtils.isBlank(baseName)) {
                return;
            }

            File[] logFiles = parentDirectory.listFiles((dir,name)-> StringUtils.isNotBlank(name) && name.startsWith(baseName));

            if (logFiles == null || logFiles.length == 0) {
                return;
            }

            for (File logFile : logFiles) {
                String logFileName = logFile.getName();

                int lastDot = logFileName.lastIndexOf(".");

                if (lastDot < 0) {
                    continue;
                }

                String logTime = logFileName.substring(lastDot);
                SimpleDateFormat dailyRollingSdf = new SimpleDateFormat(DAILY_ROLLING_PATTERN);
                SimpleDateFormat hourlyRollingSdf = new SimpleDateFormat(HOURLY_ROLLING_PATTERN);

                if (".log".equalsIgnoreCase(logTime)) {
                    continue;
                }

                Date date = null;
                try {
                    date = hourlyRollingSdf.parse(logTime);
                } catch (ParseException e) {
                    try {
                        date = dailyRollingSdf.parse(logTime);
                    } catch (ParseException pe) {
                        SelfDefineLog.error("Unable to get log time of log file " + logFileName
                                + ", the reason is " + pe.getMessage());
                    }
                }

                if (date == null) {
                    continue;
                }

                Calendar now = Calendar.getInstance();
                now.add(Calendar.DATE, 0 - logReserveConfig.getDay());
                if (logReserveConfig.getHour() > 0) {
                    now.add(Calendar.HOUR_OF_DAY, 0 - logReserveConfig.getHour());
                } else {
                    now.set(Calendar.HOUR_OF_DAY, 0);
                }
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);

                Calendar compareCal = Calendar.getInstance();
                compareCal.clear();
                compareCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                compareCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                compareCal.set(Calendar.DATE, now.get(Calendar.DATE));
                compareCal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));

                Calendar logCal = Calendar.getInstance();
                logCal.setTime(date);

                if (!logCal.before(compareCal)) {
                    continue;
                }

                boolean success = logFile.delete() && !logFile.exists();

                if (success) {
                    SelfDefineLog.info("Deleted log file: " + logFileName);
                } else {
                    SelfDefineLog.error("Fail to delete log file: " + logFileName);
                }
            }
        } catch (Throwable e) {
            SelfDefineLog.error("Failed to clean up log file", e);
        }
    }

    @Override
    public void rollOver() {
        // Compute filename, but only if datePattern is specified
        if (datePattern == null) {
            SelfDefineLog.error("No Settings for file rolling suffix's model");
            return;
        }

        String datedFilename = fileName + sdf.format(now);
        // It is too early to roll over because we are still within the
        // bounds of the current interval. Rollover will occur once the
        // next interval is reached.
        if (scheduledFilename.equals(datedFilename)) {
            return;
        }

        try {
            bos.close();
        } catch (IOException e) {
            SelfDefineLog.error("Failed to closing the output stream", e);
        }

        File target = new File(scheduledFilename);
        if (target.exists()) {
            target.delete();
        }

        boolean result = logFile.renameTo(target);
        if (result) {
            SelfDefineLog.info(fileName + " -> " + scheduledFilename);
        } else {
            SelfDefineLog.error("Failed to rename [" + fileName + "] to [" + scheduledFilename + "].");
        }

        this.setFile(false);
        scheduledFilename = datedFilename;
    }

    // This method computes the roll over period by looping over the
    // periods, starting with the shortest, and stopping when the r0 is
    // different from from r1, where r0 is the epoch formatted according
    // the datePattern (supplied by the user) and r1 is the
    // epoch+nextMillis(i) formatted according to datePattern. All date
    // formatting is done in GMT and not local format because the test
    // logic is based on comparisons relative to 1970-01-01 00:00:00
    // GMT (the epoch).

    int computeCheckPeriod() {
        RollingCalendar rollingCalendar = new RollingCalendar(gmtTimeZone, Locale.getDefault());
        // set sate to 1970-01-01 00:00:00 GMT
        Date epoch = new Date(0);
        if (datePattern != null) {
            for (int i = TOP_OF_SECONDS; i <= TOP_OF_MONTH; i++) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
                // do all date formatting in GMT
                simpleDateFormat.setTimeZone(gmtTimeZone);
                String r0 = simpleDateFormat.format(epoch);
                rollingCalendar.setType(i);
                Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
                String r1 = simpleDateFormat.format(next);
                if (r0 != null && r1 != null && !r0.equals(r1)) {
                    return i;
                }
            }
        }
        // Deliberately head for trouble...
        return TOP_OF_TROUBLE;
    }
}