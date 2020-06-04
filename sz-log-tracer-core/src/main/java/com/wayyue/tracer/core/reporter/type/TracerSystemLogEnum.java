package com.wayyue.tracer.core.reporter.type;

/**
 * TracerSystemLogEnum
 */
public enum TracerSystemLogEnum {

    /**
     * Profile log
     */
    RPC_PROFILE("rpc_profile_log_name", "rpc-profile.log", "rpc_profile_rolling"),
    /**
     * Business success log
     */
    BIZ_SUCCESS("biz_success_log_name", "biz_success.log", "biz_success_rolling"),
    /**
     * Business fail log
     */
    BIZ_FAIL("biz_fail_log_name", "biz_fail.log", "biz_fail_rolling"),

    /**
     * Middleware error log
     */
    MIDDLEWARE_ERROR("middleware_error_log_name", "middleware_error.log", "middleware_error_rolling");

    private String logReverseKey;

    private String defaultLogName;

    private String rollingKey;

    TracerSystemLogEnum(String logReverseKey, String defaultLogName, String rollingKey) {
        this.logReverseKey = logReverseKey;
        this.defaultLogName = defaultLogName;
        this.rollingKey = rollingKey;
    }

    public String getLogReverseKey() {
        return logReverseKey;
    }

    public String getDefaultLogName() {
        return defaultLogName;
    }

    public String getRollingKey() {
        return rollingKey;
    }
}
