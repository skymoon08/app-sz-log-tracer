
package com.wayyue.tracer.spring.message.plugins.enums;


public enum SpringMessageLogEnum {

    MESSAGE_PUB_DIGEST("message_pub_digest_log_name", "message-pub-digest.log",
                       "message_pub_digest_rolling"), MESSAGE_PUB_STAT("message_pub_stat_log_name",
                                                                       "message-pub-stat.log",
                                                                       "message_pub_stat_rolling"),

    MESSAGE_SUB_DIGEST("message_sub_digest_log_name", "message-sub-digest.log",
                       "message_sub_digest_rolling"), MESSAGE_SUB_STAT("message_sub_stat_log_name",
                                                                       "message-sub-stat.log",
                                                                       "message_sub_stat_rolling");

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    SpringMessageLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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
