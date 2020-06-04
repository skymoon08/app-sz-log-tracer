package com.wayyue.tracer.boot.configuration;


import com.wayyue.tracer.boot.properties.SzTracerProperties;
import com.wayyue.tracer.core.listener.SpanReportListener;
import com.wayyue.tracer.core.listener.SpanReportListenerHolder;
import com.wayyue.tracer.core.reporter.facade.Reporter;
import com.wayyue.tracer.core.samplers.Sampler;
import com.wayyue.tracer.core.samplers.SamplerFactory;
import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(SzTracerProperties.class)
public class SzTracerAutoConfiguration {

    @Autowired(required = false)
    private List<SpanReportListener> spanReportListenerList;

    @Bean
    @ConditionalOnMissingBean
    public SpanReportListenerHolder szTracerSpanReportListener() {
        if (this.spanReportListenerList != null && this.spanReportListenerList.size() > 0) {
            //cache in tracer listener core
            SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
        }
        return null;
    }
}