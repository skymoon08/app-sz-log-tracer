package com.wayyue.tracer.boot.zipkin.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ZipkinSofaTracerProperties
 *
 * @author guolei.sgl
 * @since v2.3.0
 */
@ConfigurationProperties("com.alipay.sofa.tracer.zipkin")
public class ZipkinSzTracerProperties {

    /**
     * URL of the zipkin query server instance.
     */
    private String  baseUrl = "http://localhost:9411/";
    /**
     * zipkin reporter is disabled by default
     */
    private boolean enabled = true;
    /**
     * When enabled, spans are gzipped before sent to the zipkin server
     */
    private boolean gzipped = false;

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isGzipped() {
        return gzipped;
    }

    public void setGzipped(boolean gzipped) {
        this.gzipped = gzipped;
    }
}
