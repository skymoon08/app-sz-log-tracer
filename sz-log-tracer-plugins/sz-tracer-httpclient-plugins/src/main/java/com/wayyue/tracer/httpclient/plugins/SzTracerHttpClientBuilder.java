package com.wayyue.tracer.httpclient.plugins;

import com.wayyue.tracer.core.tracer.AbstractTracer;
import com.wayyue.tracer.httpclient.plugins.interceptor.SzTracerAsyncHttpInterceptor;
import com.wayyue.tracer.httpclient.plugins.interceptor.SzTracerHttpInterceptor;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;


/**
 * SzTracerHttpClientBuilder
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class SzTracerHttpClientBuilder {

    protected static AbstractTracer httpClientTracer = null;

    public static HttpClientBuilder clientBuilder(HttpClientBuilder clientBuilder) {
        return clientBuilder(clientBuilder, null, null);
    }

    public static HttpClientBuilder clientBuilder(HttpClientBuilder clientBuilder,
                                                  String currentApp, String targetApp) {
        SzTracerHttpInterceptor interceptor = new SzTracerHttpInterceptor(getHttpClientTracer(), currentApp, targetApp);
        return clientBuilder.addInterceptorFirst((HttpRequestInterceptor) interceptor)
                .addInterceptorFirst((HttpResponseInterceptor) interceptor);
    }

    public static HttpAsyncClientBuilder asyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder) {
        return asyncClientBuilder(httpAsyncClientBuilder, null, null);
    }

    public static HttpAsyncClientBuilder asyncClientBuilder(HttpAsyncClientBuilder httpAsyncClientBuilder,
                                                            String currentApp, String targetApp) {
        SzTracerAsyncHttpInterceptor interceptor = new SzTracerAsyncHttpInterceptor(getHttpClientTracer(), currentApp, targetApp);
        return httpAsyncClientBuilder.addInterceptorFirst((HttpRequestInterceptor) interceptor)
                .addInterceptorFirst((HttpResponseInterceptor) interceptor);
    }

    public static AbstractTracer getHttpClientTracer() {
        if (httpClientTracer == null) {
            synchronized (SzTracerHttpClientBuilder.class) {
                if (httpClientTracer == null) {
                    //default json format
                    httpClientTracer = HttpClientTracer.getHttpClientTracerSingleton();
                }
            }
        }
        return httpClientTracer;
    }
}
