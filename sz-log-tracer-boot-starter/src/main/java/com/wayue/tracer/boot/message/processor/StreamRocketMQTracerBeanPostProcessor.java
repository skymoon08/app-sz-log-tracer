
package com.wayue.tracer.boot.message.processor;


import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.spring.message.plugins.interceptor.SzTracerChannelInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.integration.channel.AbstractMessageChannel;


public class StreamRocketMQTracerBeanPostProcessor implements BeanPostProcessor, EnvironmentAware,
        PriorityOrdered {

    private Environment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        if (bean instanceof AbstractMessageChannel) {
            String appName = environment.getProperty(SzTracerConfiguration.TRACER_APPNAME_KEY);
            ((AbstractMessageChannel) bean).addInterceptor(SzTracerChannelInterceptor
                    .create(appName));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
