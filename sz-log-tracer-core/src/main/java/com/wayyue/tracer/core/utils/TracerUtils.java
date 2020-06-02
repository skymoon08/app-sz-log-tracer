package com.wayyue.tracer.core.utils;

import com.wayyue.tracer.core.appender.config.LogReserveConfig;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.span.SzTracerSpan;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.TimeZone;

/**
 * Tracer's tool class, this tool class is an internal tool class, please do not rely on non-Tracer related JAR packages.
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class TracerUtils {

    private static int         TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH         = -1;

    private static int         TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = -1;

    public static final String KEY_OF_CURRENT_ZONE                          = "com.shanzhen.ldc.zone";

    public static final String CURRENT_ZONE = System.getProperty(KEY_OF_CURRENT_ZONE);

    public static String P_ID_CACHE = null;

    /**
     * Get trace id from current tracer context.
     *
     * @return <ol>
     * <li>If current tracer context is not null, but trace id in it is null, returns an empty string.</li>
     * <li>If current tracer context is not null, and trace id in it is not null, returns trace id.</li>
     * <li>If current tracer context is null, returns an empty string.</li>
     * </ol>
     */
    public static String getTraceId() {
        SzTracerSpan currentSpan = SzTraceContextHolder.getSzTraceContext().getCurrentSpan();
        if (currentSpan == null) {
            return StringUtils.EMPTY_STRING;
        }
        SzTracerSpanContext SzTracerSpanContext = currentSpan.getSzTracerSpanContext();

        String traceId = null;
        if (SzTracerSpanContext != null) {
            traceId = SzTracerSpanContext.getTraceId();
        }
        return StringUtils.isBlank(traceId) ? StringUtils.EMPTY_STRING : traceId;
    }

    /**
     * @param SzTracerSpan SzTracerSpan
     * @param key            key
     * @param value          value
     * @return
     */
    public static boolean checkBaggageLength(SzTracerSpan SzTracerSpan, String key, String value) {
        int length = SzTracerSpan.getSzTracerSpanContext().getBizSerializedBaggage().length();
        if (SzTracerSpan.getBaggageItem(key) == null) {
            length += key.length() + value.length();
        } else {
            length += value.length() - SzTracerSpan.getBaggageItem(key).length();
        }

        length = length + StringUtils.AND.length() + StringUtils.EQUAL.length();

        return length <= getBaggageMaxLength();
    }

    /**
     * System penetration data length can be set by different -D parameters
     *
     * @return
     */
    public static int getSysBaggageMaxLength() {
        if (TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH < 0) {
            String length = SzTracerConfiguration
                .getProperty(SzTracerConfiguration.TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH);
            if (StringUtils.isBlank(length)) {
                //default value
                TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = SzTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
            } else {
                try {
                    TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = Integer.parseInt(length);
                } catch (NumberFormatException e) {
                    TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH = SzTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
                }
            }
        }
        return TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH;
    }

    public static int getBaggageMaxLength() {
        if (TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH < 0) {
            String length = SzTracerConfiguration
                .getProperty(SzTracerConfiguration.TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH);
            if (StringUtils.isBlank(length)) {
                //default value
                TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH = SzTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
            } else {
                try {
                    TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH = Integer.parseInt(length);
                } catch (NumberFormatException e) {
                    TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH = SzTracerConfiguration.PEN_ATTRS_LENGTH_TRESHOLD;
                }
            }
        }
        return TRACE_PENETRATE_ATTRIBUTE_MAX_LENGTH;
    }

    /**
     * This method can be a better way under JDK9, but in the current JDK version, it can only be implemented in this way.
     *
     * In Mac OS , JDK6，JDK7，JDK8 ,it's OK
     * In Linux OS,JDK6，JDK7，JDK8 ,it's OK
     *
     * @return Process ID
     */
    public static String getPID() {
        //check pid is cached
        if (P_ID_CACHE != null) {
            return P_ID_CACHE;
        }
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();

        if (StringUtils.isBlank(processName)) {
            return StringUtils.EMPTY_STRING;
        }

        String[] processSplitName = processName.split("@");

        if (processSplitName.length == 0) {
            return StringUtils.EMPTY_STRING;
        }

        String pid = processSplitName[0];

        if (StringUtils.isBlank(pid)) {
            return StringUtils.EMPTY_STRING;
        }
        P_ID_CACHE = pid;
        return pid;
    }

    public static LogReserveConfig parseLogReserveConfig(String logReserveConfig) {
        if (StringUtils.isBlank(logReserveConfig)) {
            return new LogReserveConfig(SzTracerConfiguration.DEFAULT_LOG_RESERVE_DAY, 0);
        }

        int day;
        int hour = 0;
        int dayIndex = logReserveConfig.indexOf("D");

        if (dayIndex >= 0) {
            day = Integer.valueOf(logReserveConfig.substring(0, dayIndex));
        } else {
            day = Integer.valueOf(logReserveConfig);
        }

        int hourIndex = logReserveConfig.indexOf("H");

        if (hourIndex >= 0) {
            hour = Integer.valueOf(logReserveConfig.substring(dayIndex + 1, hourIndex));
        }

        return new LogReserveConfig(day, hour);
    }

    public static boolean isLoadTest(SzTracerSpan SzTracerSpan) {
        if (SzTracerSpan == null || SzTracerSpan.getSzTracerSpanContext() == null) {
            return false;
        } else {
            SzTracerSpanContext spanContext = SzTracerSpan.getSzTracerSpanContext();
            Map<String, String> baggage = spanContext.getBizBaggage();
            return SzTracerConstant.LOAD_TEST_VALUE.equals(baggage
                .get(SzTracerConstant.LOAD_TEST_TAG));
        }
    }

    public static String getLoadTestMark(SzTracerSpan span) {
        if (TracerUtils.isLoadTest(span)) {
            return SzTracerConstant.LOAD_TEST_VALUE;
        } else {
            //non-pressure test
            return SzTracerConstant.NON_LOAD_TEST_VALUE;
        }
    }

    public static String getInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                        return address.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    public static String removeJSessionIdFromUrl(String url) {
        if (url == null) {
            return null;
        }
        int index = url.indexOf(";jsessionid=");
        if (index < 0) {
            return url;
        }
        return url.substring(0, index);
    }

    public static String getCurrentZone() {
        return CURRENT_ZONE;
    }

    public static String getDefaultTimeZone() {
        return TimeZone.getDefault().getID();
    }

    /**
     * Get a value from the Map, or return an empty string if it is null
     *
     * @param map map
     * @param key key
     * @return
     */
    public static String getEmptyStringIfNull(Map<String, String> map, String key) {
        String value = map.get(key);
        return value == null ? StringUtils.EMPTY_STRING : value;
    }

    /**
     * Convert a Host address to a hexadecimal number
     *
     * @param host host address
     * @return hexadecimal number
     */
    public static String hostToHexString(String host) { //NOPMD
        return Integer.toHexString(host.hashCode());
    }
}
