
package com.wayue.tracer.boot.message.configuration;


import com.wayue.tracer.boot.message.processor.StreamRocketMQTracerBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

/**
 * @author: guolei.sgl (guolei.sgl@antfin.com) 2019/12/4 10:34 PM
 * @since:
 **/
@Configuration
@ConditionalOnClass({ AbstractMessageChannel.class, ChannelInterceptor.class})
@ConditionalOnProperty(prefix = "com.alipay.sofa.tracer.message", value = "enable", matchIfMissing = true)
public class SpringMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StreamRocketMQTracerBeanPostProcessor streamRocketMQTracerBeanPostProcessor() {
        return new StreamRocketMQTracerBeanPostProcessor();
    }
}
