
package com.wayyue.tracer.flexible.plugins;

/**
 * FlexibleLogEnum for flexible biz tracer
 *
 **/
public enum FlexibleLogEnum {

    // Flexible Digest Log
    FLEXIBLE_DIGEST("biz_digest_log_name", "biz-digest.log", "biz_digest_rolling"),
    // Flexible Stat Log
    FLEXIBLE_STAT("biz_stat_log_name", "biz-stat.log", "biz_stat_rolling"), ;

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    FlexibleLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
        this.logNameKey = logNameKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogNameKey() {
        //log reserve config key
        return logNameKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }
}