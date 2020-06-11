package com.wayue.tracer.httpclient.plugins.interceptor;


import com.wayue.tracer.core.context.trace.SzTraceContext;
import com.wayue.tracer.core.holder.SzTraceContextHolder;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractTracer;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * SzTracerHttpInterceptor
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class SzTracerHttpInterceptor extends AbstractHttpRequestInterceptor implements HttpRequestInterceptor, HttpResponseInterceptor {

    public SzTracerHttpInterceptor(AbstractTracer httpClientTracer, String appName, String targetAppName) {
        super(httpClientTracer, appName, targetAppName);
    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        //lazy init
        RequestLine requestLine = httpRequest.getRequestLine();
        String methodName = requestLine.getMethod();
        //span generated
        SzTracerSpan httpClientSpan = httpClientTracer.clientSend(methodName);
        super.appendHttpClientRequestSpanTags(httpRequest, httpClientSpan);
    }

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        SzTraceContext traceContext = SzTraceContextHolder.getSzTraceContext();
        SzTracerSpan httpClientSpan = traceContext.getCurrentSpan();
        //tag append
        super.appendHttpClientResponseSpanTags(httpResponse, httpClientSpan);
        //finish
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        httpClientTracer.clientReceive(String.valueOf(statusCode));
    }
}
