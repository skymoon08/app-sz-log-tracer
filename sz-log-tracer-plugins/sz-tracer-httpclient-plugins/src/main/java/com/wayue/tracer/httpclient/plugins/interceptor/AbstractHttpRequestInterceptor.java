package com.wayue.tracer.httpclient.plugins.interceptor;

import com.wayue.tracer.core.SzTracer;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.registry.ExtendFormat;
import com.wayue.tracer.core.span.CommonSpanTags;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractTracer;
import com.wayue.tracer.core.utils.StringUtils;
import com.wayue.tracer.httpclient.plugins.HttpClientRequestCarrier;
import org.apache.http.*;
import org.apache.http.client.methods.HttpRequestWrapper;

/**
 * AbstractHttpRequestInterceptor
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public abstract class AbstractHttpRequestInterceptor {

    protected static final String CURRENT_ASYNC_HTTP_SPAN_KEY = "httpclient.async.span.key";

    protected AbstractTracer httpClientTracer;

    protected String              appName;

    protected String              targetAppName;

    public AbstractHttpRequestInterceptor(AbstractTracer httpClientTracer, String appName,
                                          String targetAppName) {
        this.httpClientTracer = httpClientTracer;
        this.appName = appName;
        this.targetAppName = targetAppName;
    }

    public void appendHttpClientRequestSpanTags(HttpRequest httpRequest, SzTracerSpan httpClientSpan) {
        if (httpClientSpan == null) {
            return;
        }
        if (this.appName == null) {
            this.appName = SzTracerConfiguration.getProperty(SzTracerConfiguration.TRACER_APPNAME_KEY, StringUtils.EMPTY_STRING);
        }
        //lazy init
        RequestLine requestLine = httpRequest.getRequestLine();
        String methodName = requestLine.getMethod();
        //appName
        httpClientSpan.setTag(CommonSpanTags.LOCAL_APP,
            this.appName == null ? StringUtils.EMPTY_STRING : this.appName);
        //targetAppName
        httpClientSpan.setTag(CommonSpanTags.REMOTE_APP,
            this.targetAppName == null ? StringUtils.EMPTY_STRING : this.targetAppName);
        if (httpRequest instanceof HttpRequestWrapper) {
            HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) httpRequest;
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, httpRequestWrapper.getOriginal().getRequestLine().getUri());
        } else {
            httpClientSpan.setTag(CommonSpanTags.REQUEST_URL, requestLine.getUri());
        }
        //method
        httpClientSpan.setTag(CommonSpanTags.METHOD, methodName);
        //length
        if (httpRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) httpRequest;
            HttpEntity httpEntity = httpEntityEnclosingRequest.getEntity();
            httpClientSpan.setTag(CommonSpanTags.REQ_SIZE, httpEntity == null ? -1 : httpEntity.getContentLength());
        }
        //carrier
        this.processHttpClientRequestCarrier(httpRequest, httpClientSpan);
    }

    public void appendHttpClientResponseSpanTags(HttpResponse httpResponse,
                                                 SzTracerSpan httpClientSpan) {
        //length
        if (httpClientSpan != null) {
            HttpEntity httpEntity = httpResponse.getEntity();
            long contentLength = httpEntity == null ? -1 : httpEntity.getContentLength();
            httpClientSpan.setTag(CommonSpanTags.RESP_SIZE, contentLength);
            httpClientSpan.setTag(CommonSpanTags.CURRENT_THREAD_NAME, Thread.currentThread()
                .getName());
        }
    }

    public void processHttpClientRequestCarrier(HttpRequest httpRequest, SzTracerSpan currentSpan) {
        SzTracer SzTracer = this.httpClientTracer.getSzTracer();
        SzTracer.inject(currentSpan.getSzTracerSpanContext(), ExtendFormat.Builtin.B3_HTTP_HEADERS, new HttpClientRequestCarrier(httpRequest));
    }
}
