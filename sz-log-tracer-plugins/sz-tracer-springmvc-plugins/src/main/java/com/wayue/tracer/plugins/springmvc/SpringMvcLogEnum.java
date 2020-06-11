package com.wayue.tracer.plugins.springmvc;

/**
 * SpringMvcLogEnum
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public enum SpringMvcLogEnum {

    SPRING_MVC_DIGEST("spring_mvc_digest_log_name", "spring-mvc-digest.log", "spring_mvc_digest_rolling"),
    SPRING_MVC_STAT("spring_mvc_stat_log_name", "spring-mvc-stat.log", "spring_mvc_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    SpringMvcLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
        this.logNameKey = logNameKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogNameKey() {
        return logNameKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }
}