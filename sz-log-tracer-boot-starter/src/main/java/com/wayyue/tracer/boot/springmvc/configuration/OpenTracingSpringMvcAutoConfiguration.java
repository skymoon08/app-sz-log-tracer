package com.wayyue.tracer.boot.springmvc.configuration;

import com.wayyue.tracer.boot.configuration.SzTracerAutoConfiguration;
import com.wayyue.tracer.boot.springmvc.properties.OpenTracingSpringMvcProperties;
import com.wayyue.tracer.plugins.springmvc.SpringMvcSzTracerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(OpenTracingSpringMvcProperties.class)
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "com.wayyue.tracer.plugins.springmvc", value = "enable", matchIfMissing = true)
@AutoConfigureAfter(SzTracerAutoConfiguration.class)
public class OpenTracingSpringMvcAutoConfiguration {

    @Autowired
    private OpenTracingSpringMvcProperties openTracingSpringProperties;

    @Bean
    public FilterRegistrationBean springMvcDelegatingFilterProxy() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        SpringMvcSzTracerFilter filter = new SpringMvcSzTracerFilter();
        filterRegistrationBean.setFilter(filter);
        List<String> urlPatterns = openTracingSpringProperties.getUrlPatterns();
        if (urlPatterns == null || urlPatterns.size() <= 0) {
            filterRegistrationBean.addUrlPatterns("/*");
        } else {
            filterRegistrationBean.setUrlPatterns(urlPatterns);
        }
        filterRegistrationBean.setName(filter.getFilterName());
        filterRegistrationBean.setAsyncSupported(true);
        filterRegistrationBean.setOrder(openTracingSpringProperties.getFilterOrder());
        return filterRegistrationBean;
    }

}