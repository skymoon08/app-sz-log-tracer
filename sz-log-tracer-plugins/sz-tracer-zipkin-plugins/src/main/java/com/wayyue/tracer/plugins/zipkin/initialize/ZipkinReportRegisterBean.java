package com.wayyue.tracer.plugins.zipkin.initialize;

import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.listener.SpanReportListener;
import com.wayyue.tracer.core.listener.SpanReportListenerHolder;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.plugins.zipkin.ZipkinSzTracerRestTemplateCustomizer;
import com.wayyue.tracer.plugins.zipkin.ZipkinSzTracerSpanRemoteReporter;
import com.wayyue.tracer.plugins.zipkin.properties.ZipkinProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


/**
 * ZipkinReportRegisterBean to parse properties and register zipkin report listeners
 *
 * @author guolei.sgl
 * @since v2.3.0
 */
public class ZipkinReportRegisterBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        // if do not match report condition,it will be return right now
        boolean enabled = true;
        String enabledStr = SzTracerConfiguration
                .getProperty(ZipkinProperties.ZIPKIN_IS_ENABLED_KEY);
        if (StringUtils.isNotBlank(enabledStr) && "true".equalsIgnoreCase(enabledStr)) {
            enabled = true;
        }
        if (!enabled) {
            return;
        }

        boolean gzipped = false;
        String gzippedStr = SzTracerConfiguration
                .getProperty(ZipkinProperties.ZIPKIN_IS_GZIPPED_KEY);
        if (StringUtils.isNotBlank(gzippedStr) && "true".equalsIgnoreCase(gzippedStr)) {
            gzipped = true;
        }

        RestTemplate restTemplate = new RestTemplate();
        ZipkinSzTracerRestTemplateCustomizer zipkinSzTracerRestTemplateCustomizer = new ZipkinSzTracerRestTemplateCustomizer(
                gzipped);
        zipkinSzTracerRestTemplateCustomizer.customize(restTemplate);
        String baseUrl = SzTracerConfiguration.getProperty(ZipkinProperties.ZIPKIN_BASE_URL_KEY);
        SpanReportListener spanReportListener = new ZipkinSzTracerSpanRemoteReporter(
                restTemplate, baseUrl);
        List<SpanReportListener> spanReportListenerList = new ArrayList<SpanReportListener>();
        spanReportListenerList.add(spanReportListener);
        SpanReportListenerHolder.addSpanReportListeners(spanReportListenerList);
    }
}
