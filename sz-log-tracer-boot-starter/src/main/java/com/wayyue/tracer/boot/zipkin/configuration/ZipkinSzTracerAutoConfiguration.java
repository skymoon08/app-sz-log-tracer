/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wayyue.tracer.boot.zipkin.configuration;

import com.wayyue.tracer.boot.zipkin.properties.ZipkinSzTracerProperties;
import com.wayyue.tracer.plugins.zipkin.ZipkinSzTracerRestTemplateCustomizer;
import com.wayyue.tracer.plugins.zipkin.ZipkinSzTracerSpanRemoteReporter;
import com.wayyue.tracer.plugins.zipkin.initialize.ZipkinReportRegisterBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ZipkinSofaTracerAutoConfiguration
 *
 * @author yangguanchao
 * @since 2018/05/01
 */
@Configuration
@EnableConfigurationProperties(ZipkinSzTracerProperties.class)
@ConditionalOnProperty(value = "com.alipay.sofa.tracer.zipkin.enabled", matchIfMissing = false)
@ConditionalOnClass({ zipkin2.Span.class, zipkin2.reporter.AsyncReporter.class, RestTemplate.class })
public class ZipkinSzTracerAutoConfiguration {

    @Autowired
    private ZipkinSzTracerProperties zipkinProperties;

    @Bean
    @ConditionalOnMissingBean
    public ZipkinSzTracerRestTemplateCustomizer zipkinSofaTracerRestTemplateCustomizer() {
        return new ZipkinSzTracerRestTemplateCustomizer(zipkinProperties.isGzipped());
    }

    @Bean
    @ConditionalOnMissingBean
    public ZipkinSzTracerSpanRemoteReporter zipkinSofaTracerSpanReporter(ZipkinSzTracerRestTemplateCustomizer zipkinSofaTracerRestTemplateCustomizer) {
        RestTemplate restTemplate = new RestTemplate();
        zipkinSofaTracerRestTemplateCustomizer.customize(restTemplate);
        return new ZipkinSzTracerSpanRemoteReporter(restTemplate, zipkinProperties.getBaseUrl());
    }
    @Bean
    public ZipkinReportRegisterBean initZipkinReportRegisterBean() {
        return new ZipkinReportRegisterBean();
    }
}
