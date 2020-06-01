package com.wayyue.tracer.boot.springmvc.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenTracingSpringMvcProperties
 */
@ConfigurationProperties("com.wayyue.tracer.plugins.springmvc")
public class OpenTracingSpringMvcProperties {

    /***
     * com.wayyue.tracer.springmvc.filter-order
     */
    private int filterOrder = Ordered.HIGHEST_PRECEDENCE + 1;

    /**
     * Comma-separated list of urlPatterns to create : com.wayyue.tracer.springmvc.url-patterns=/**,
     */
    private List<String> urlPatterns = new ArrayList<String>();

    public int getFilterOrder() {
        return filterOrder;
    }

    public void setFilterOrder(int filterOrder) {
        this.filterOrder = filterOrder;
    }

    public List<String> getUrlPatterns() {
        return urlPatterns;
    }

    public void setUrlPatterns(List<String> urlPatterns) {
        this.urlPatterns = urlPatterns;
    }
}