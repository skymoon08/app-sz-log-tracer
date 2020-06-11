package com.wayue.tracer.httpclient.plugins;

/**
 * HttpClientLogEnum
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public enum HttpClientLogEnum {

    // Http Client Digest Log
    HTTP_CLIENT_DIGEST("httpclient_digest_log_name", "httpclient-digest.log", "httpclient_digest_rolling"),
    // Http Client Stat Log
    HTTP_CLIENT_STAT("httpclient_stat_log_name", "httpclient-stat.log", "httpclient_stat_rolling"),
    ;

    private String logNameKey;
    private String defaultLogName;
    private String rollingKey;

    HttpClientLogEnum(String logNameKey, String defaultLogName, String rollingKey) {
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