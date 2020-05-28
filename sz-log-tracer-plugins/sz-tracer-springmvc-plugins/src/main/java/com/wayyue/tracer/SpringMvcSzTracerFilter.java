package com.wayyue.tracer;


import com.wayyue.tracer.core.SzTracer;
import com.wayyue.tracer.core.configuration.SzTracerConfiguration;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.registry.AbstractTextB3Formatter;
import com.wayyue.tracer.core.registry.ExtendFormat;
import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * SpringMvcSzTracerFilter
 */
public class SpringMvcSzTracerFilter implements Filter {

    private String appName = StringUtils.EMPTY_STRING;

    private SpringMvcTracer springMvcTracer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no operation and lazy init
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) {

        if (this.springMvcTracer == null) {
            this.springMvcTracer = springMvcTracer.getSpringMvcTracerSingleton();
        }
        SzTracerSpan springMvcSpan = null;
        long responseSize = -1;
        int httpStatus = -1;
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            SzTracerSpanContext spanContext = getSpanContextFromRequest(request);
            // sr
            springMvcSpan = springMvcTracer.serverReceive(spanContext);

            if (StringUtils.isBlank(this.appName)) {
                this.appName = SzTracerConfiguration.getProperty(SzTracerConfiguration.TRACER_APPNAME_KEY);
            }
            //set service name
            springMvcSpan.setOperationName(request.getRequestURL().toString());
            //app name
            springMvcSpan.setTag(CommonSpanTags.LOCAL_APP, this.appName);
            springMvcSpan.setTag(CommonSpanTags.REQUEST_URL, request.getRequestURL().toString());
            springMvcSpan.setTag(CommonSpanTags.METHOD, request.getMethod());
            springMvcSpan.setTag(CommonSpanTags.REQ_SIZE, request.getContentLength());
            //wrapper
            ResponseWrapper responseWrapper = new ResponseWrapper(response);
            //filter begin
            filterChain.doFilter(servletRequest, responseWrapper);
            //filter end
            httpStatus = responseWrapper.getStatus();
            responseSize = responseWrapper.getContentLength();
        } catch (Throwable t) {
            httpStatus = 500;
            throw new RuntimeException(t);
        } finally {
            if (springMvcSpan != null) {
                springMvcSpan.setTag(CommonSpanTags.RESP_SIZE, responseSize);
                //ss
                springMvcTracer.serverSend(String.valueOf(httpStatus));
            }
        }
    }

    @Override
    public void destroy() {
        // no operation
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFilterName() {
        return "SpringMvcSofaTracerFilter";
    }

    /***
     * Extract tracing context from request received from previous node
     * @param request Servlet http request object
     * @return SofaTracerSpanContext Tracing context extract from request
     */
    public SzTracerSpanContext getSpanContextFromRequest(HttpServletRequest request) {
        HashMap<String, String> headers = new HashMap<String, String>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            headers.put(key, value);
        }
        // Delay the initialization of the SofaTracerSpanContext to execute the serverReceive method
        if (headers.isEmpty() || !isContainSofaTracerMark(headers)) {
            return null;
        }

        SzTracer tracer = springMvcTracer.getSzTracer();
        SzTracerSpanContext spanContext = (SzTracerSpanContext) tracer.extract(
                ExtendFormat.Builtin.B3_HTTP_HEADERS, new SpringMvcHeadersCarrier(headers));
        return spanContext;
    }

    /**
     * To check is contain sofaTracer mark
     *
     * @param headers
     * @return
     */
    private boolean isContainSofaTracerMark(HashMap<String, String> headers) {
        return (headers.containsKey(AbstractTextB3Formatter.TRACE_ID_KEY_HEAD.toLowerCase()) || headers
                .containsKey(AbstractTextB3Formatter.TRACE_ID_KEY_HEAD))
                && (headers.containsKey(AbstractTextB3Formatter.SPAN_ID_KEY_HEAD.toLowerCase()) || headers
                .containsKey(AbstractTextB3Formatter.SPAN_ID_KEY_HEAD));
    }

    class ResponseWrapper extends HttpServletResponseWrapper {

        int contentLength = 0;

        /**
         * @param httpServletResponse httpServletResponse
         */
        public ResponseWrapper(HttpServletResponse httpServletResponse) throws IOException {
            super(httpServletResponse);
        }

        /**
         * @see ServletResponseWrapper#setContentLength(int)
         */
        @Override
        public void setContentLength(int len) {
            contentLength = len;
            super.setContentLength(len);
        }

        public int getContentLength() {
            return contentLength;
        }
    }

}