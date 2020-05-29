package com.wayyue.tracer.plugins.dubbo.enums;

public enum DubboLogEnum {

    DUBBO_SERVER_DIGEST("dubbo_server_digest_log_name", "dubbo-server-digest.log",
            "dubbo_server_digest_rolling"),

    DUBBO_SERVER_STAT("dubbo_server_stat_log_name", "dubbo-server-stat.log",
            "dubbo_server_stat_rolling"),

    DUBBO_CLIENT_DIGEST("dubbo_client_digest_log_name", "dubbo-client-digest.log",
            "dubbo_client_digest_rolling"),

    DUBBO_CLIENT_STAT("dubbo_client_stat_log_name", "dubbo-client-stat.log",
            "dubbot_client_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    DubboLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
