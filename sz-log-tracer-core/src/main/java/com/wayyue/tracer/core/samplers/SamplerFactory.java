package com.wayyue.tracer.core.samplers;


import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.utils.StringUtils;

/**
 * SamplerFactory
 *
 */
public class SamplerFactory {

    public static SamplerProperties samplerProperties;

    static {
        samplerProperties = new SamplerProperties();
        try {
            float percentage = 100;

            String perStr = SzTracerConfiguration.getProperty(SzTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY);
            if (StringUtils.isNotBlank(perStr)) {
                percentage = Float.parseFloat(perStr);
            }
            samplerProperties.setPercentage(percentage);
        } catch (Exception e) {
            SelfDefineLog.error("It will be use default percentage value :100;", e);
            samplerProperties.setPercentage(100);
        }
        samplerProperties.setRuleClassName(SzTracerConfiguration.getProperty(SzTracerConfiguration.SAMPLER_STRATEGY_CUSTOM_RULE_CLASS_NAME));
    }

    /**
     * getSampler by samplerName
     *
     * the samplerName is the user configuration
     * @return Sampler
     * @throws Exception
     */
    public static Sampler getSampler() throws Exception {
        // User-defined rules have high priority
        if (StringUtils.isNotBlank(samplerProperties.getRuleClassName())) {
            return (Sampler) Class.forName(samplerProperties.getRuleClassName()).newInstance();
        }
        // default instance
        return new SzTracerPercentageBasedSampler(samplerProperties);
    }
}
