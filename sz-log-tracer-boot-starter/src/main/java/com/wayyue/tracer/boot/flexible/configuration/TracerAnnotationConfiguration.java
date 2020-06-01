package com.wayyue.tracer.boot.flexible.configuration;

import com.wayyue.tracer.boot.configuration.SzTracerAutoConfiguration;
import com.wayyue.tracer.boot.flexible.aop.SzTracerAdvisingBeanPostProcessor;
import com.wayyue.tracer.boot.flexible.processor.MethodInvocationProcessor;
import com.wayyue.tracer.boot.flexible.processor.SzTracerIntroductionInterceptor;
import com.wayyue.tracer.boot.flexible.processor.SzTracerMethodInvocationProcessor;
import io.opentracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "com.wayyue.tracer.flexible", value = "enable", matchIfMissing = true)
@AutoConfigureAfter(SzTracerAutoConfiguration.class)
@ConditionalOnBean(Tracer.class)
public class TracerAnnotationConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MethodInvocationProcessor szMethodInvocationProcessor(Tracer tracer) {
        return new SzTracerMethodInvocationProcessor(tracer);
    }

    @Bean
    @ConditionalOnMissingBean
    SzTracerIntroductionInterceptor szTracerIntroductionInterceptor(MethodInvocationProcessor methodInvocationProcessor) {
        return new SzTracerIntroductionInterceptor(methodInvocationProcessor);
    }

    @Bean
    @ConditionalOnMissingBean
    SzTracerAdvisingBeanPostProcessor tracerAnnotationBeanPostProcessor(SzTracerIntroductionInterceptor methodInterceptor) {
        return new SzTracerAdvisingBeanPostProcessor(methodInterceptor);
    }

}
