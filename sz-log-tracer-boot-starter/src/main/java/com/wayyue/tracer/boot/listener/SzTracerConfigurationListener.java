
package com.wayyue.tracer.boot.listener;

import com.wayyue.tracer.boot.properties.SzTracerProperties;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.utils.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

public class SzTracerConfigurationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        // set loggingPath
        String loggingPath = environment.getProperty("logging.path");
        if (StringUtils.isNotBlank(loggingPath)) {
            System.setProperty("logging.path", loggingPath);
        }
        // check spring.application.name
        String applicationName = environment.getProperty(SzTracerConfiguration.TRACER_APPNAME_KEY);
        Assert.isTrue(!StringUtils.isBlank(applicationName), SzTracerConfiguration.TRACER_APPNAME_KEY + " must be configured!");
        SzTracerConfiguration.setProperty(SzTracerConfiguration.TRACER_APPNAME_KEY, applicationName);

        SzTracerProperties tempTarget = new SzTracerProperties();
//        PropertiesConfigurationFactory<SzTracerProperties> binder = new PropertiesConfigurationFactory<SzTracerProperties>(tempTarget);
//        ConfigurationProperties configurationPropertiesAnnotation = this.getConfigurationPropertiesAnnotation(tempTarget);
//        if (configurationPropertiesAnnotation != null && StringUtils.isNotBlank(configurationPropertiesAnnotation.prefix())) {
//            //consider compatible Spring Boot 1.5.X and 2.x
//            binder.setIgnoreInvalidFields(configurationPropertiesAnnotation.ignoreInvalidFields());
//            binder.setIgnoreUnknownFields(configurationPropertiesAnnotation.ignoreUnknownFields());
//            binder.setTargetName(configurationPropertiesAnnotation.prefix());
//        } else {
//            binder.setTargetName(SzTracerProperties.SZ_TRACER_CONFIGURATION_PREFIX);
//        }
//        binder.setConversionService(new DefaultConversionService());
//        binder.setPropertySources(environment.getPropertySources());
//        try {
//            binder.bindPropertiesToTarget();
//        } catch (BindException ex) {
//            throw new IllegalStateException("Cannot bind to SzTracerProperties", ex);
//        }

        //properties convert to tracer
        SzTracerConfiguration.setProperty(SzTracerConfiguration.DISABLE_MIDDLEWARE_DIGEST_LOG_KEY, tempTarget.getDisableDigestLog());
        SzTracerConfiguration.setProperty(SzTracerConfiguration.DISABLE_DIGEST_LOG_KEY, tempTarget.getDisableConfiguration());
        SzTracerConfiguration.setProperty(SzTracerConfiguration.TRACER_GLOBAL_ROLLING_KEY, tempTarget.getTracerGlobalRollingPolicy());
        SzTracerConfiguration.setProperty(SzTracerConfiguration.TRACER_GLOBAL_LOG_RESERVE_DAY, tempTarget.getTracerGlobalLogReserveDay());
        //stat log interval
        SzTracerConfiguration.setProperty(SzTracerConfiguration.STAT_LOG_INTERVAL, tempTarget.getStatLogInterval());
        //baggage length
        SzTracerConfiguration.setProperty(SzTracerConfiguration.TRACER_PENETRATE_ATTRIBUTE_MAX_LENGTH, tempTarget.getBaggageMaxLength());
        SzTracerConfiguration.setProperty(SzTracerConfiguration.TRACER_SYSTEM_PENETRATE_ATTRIBUTE_MAX_LENGTH, tempTarget.getBaggageMaxLength());

        //sampler config
        if (tempTarget.getSamplerName() != null) {
            SzTracerConfiguration.setProperty(SzTracerConfiguration.SAMPLER_STRATEGY_NAME_KEY, tempTarget.getSamplerName());
        }
        if (StringUtils.isNotBlank(tempTarget.getSamplerCustomRuleClassName())) {
            SzTracerConfiguration.setProperty(SzTracerConfiguration.SAMPLER_STRATEGY_CUSTOM_RULE_CLASS_NAME, tempTarget.getSamplerCustomRuleClassName());
        }
        SzTracerConfiguration.setProperty(SzTracerConfiguration.SAMPLER_STRATEGY_PERCENTAGE_KEY, String.valueOf(tempTarget.getSamplerPercentage()));
        SzTracerConfiguration.setProperty(SzTracerConfiguration.JSON_FORMAT_OUTPUT, String.valueOf(tempTarget.isJsonOutput()));
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 20;
    }

    private ConfigurationProperties getConfigurationPropertiesAnnotation(Object targetObject) {
        return AnnotationUtils.findAnnotation(targetObject.getClass(), ConfigurationProperties.class);
    }
}