package com.wayyue.tracer.httpclient.plugins.interceptor;

import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.tracer.AbstractTracer;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * SzTracerAsyncHttpInterceptor
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class SzTracerAsyncHttpInterceptor extends AbstractHttpRequestInterceptor
        implements HttpRequestInterceptor,HttpResponseInterceptor {

    public SzTracerAsyncHttpInterceptor(AbstractTracer httpClientTracer, String appName, String targetAppName) {
        super(httpClientTracer, appName, targetAppName);
    }

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException,
                                                                         IOException {
        //lazy init
        RequestLine requestLine = httpRequest.getRequestLine();
        String methodName = requestLine.getMethod();
        //span generated
        SzTracerSpan httpClientSpan = httpClientTracer.clientSend(methodName);
        super.appendHttpClientRequestSpanTags(httpRequest, httpClientSpan);
        //async handle
        httpContext.setAttribute(CURRENT_ASYNC_HTTP_SPAN_KEY, httpClientSpan);
        SzTraceContext traceContext = SzTraceContextHolder.getSzTraceContext();
        //client span
        if (httpClientSpan.getParentSzTracerSpan() != null) {
            //restore parent
            traceContext.push(httpClientSpan.getParentSzTracerSpan());
        } else {
            //pop async span
            traceContext.pop();
        }
    }

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
        SzTracerSpan httpClientSpan = (SzTracerSpan) httpContext.getAttribute(CURRENT_ASYNC_HTTP_SPAN_KEY);
        //tag append
        super.appendHttpClientResponseSpanTags(httpResponse, httpClientSpan);
        //finish
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        httpClientTracer.clientReceiveTagFinish(httpClientSpan, String.valueOf(statusCode));
    }
}
