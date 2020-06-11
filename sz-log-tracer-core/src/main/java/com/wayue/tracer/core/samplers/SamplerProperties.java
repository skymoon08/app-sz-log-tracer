package com.wayue.tracer.core.samplers;

/**
 * SamplerProperties
 *
 */
public class SamplerProperties {
    /**
     * Percentage of requests that should be sampled. E.g. 1.0 - 100% requests should be
     * sampled. The precision is whole-numbers only (i.e. there's no support for 1.0 of
     * the traces).
     */
    private float  percentage = 100;

    /**
     * if use custom rule, you can implements Sample interface and provide this class name
     */
    private String ruleClassName;

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public String getRuleClassName() {
        return ruleClassName;
    }

    public void setRuleClassName(String ruleClassName) {
        this.ruleClassName = ruleClassName;
    }

}
