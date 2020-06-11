package com.wayue.tracer.httpclient.plugins;

import com.wayue.tracer.core.appender.encoder.SpanEncoder;
import com.wayue.tracer.core.configuration.SzTracerConfiguration;
import com.wayue.tracer.core.constants.ComponentNameConstants;
import com.wayue.tracer.core.reporter.stat.AbstractSzTracerStatisticReporter;
import com.wayue.tracer.core.span.SzTracerSpan;
import com.wayue.tracer.core.tracer.AbstractClientTracer;

/**
 * HttpClientTracer
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class HttpClientTracer extends AbstractClientTracer {

    private volatile static HttpClientTracer httpClientTracer = null;

    /***
     * Http Client Tracer Singleton
     * @return singleton
     */
    public static HttpClientTracer getHttpClientTracerSingleton() {
        if (httpClientTracer == null) {
            synchronized (HttpClientTracer.class) {
                if (httpClientTracer == null) {
                    httpClientTracer = new HttpClientTracer();
                }
            }
        }
        return httpClientTracer;
    }

    protected HttpClientTracer() {
        super(ComponentNameConstants.HTTP_CLIENT);
    }

    @Override
    protected String getClientDigestReporterLogName() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getDefaultLogName();
    }

    @Override
    protected String getClientDigestReporterRollingKey() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getRollingKey();
    }

    @Override
    protected String getClientDigestReporterLogNameKey() {
        return HttpClientLogEnum.HTTP_CLIENT_DIGEST.getLogNameKey();
    }

    @Override
    protected SpanEncoder<SzTracerSpan> getClientDigestEncoder() {
        //default json output
        if (SzTracerConfiguration.isJsonOutput()) {
            return new HttpClientDigestJsonEncoder();
        } else {
            return new HttpClientDigestEncoder();
        }
    }

    @Override
    protected AbstractSzTracerStatisticReporter generateClientStatReporter() {
        HttpClientLogEnum httpClientLogEnum = HttpClientLogEnum.HTTP_CLIENT_STAT;
        String statLog = httpClientLogEnum.getDefaultLogName();
        String statRollingPolicy = SzTracerConfiguration.getRollingPolicy(httpClientLogEnum
            .getRollingKey());
        String statLogReserveConfig = SzTracerConfiguration.getLogReserveConfig(httpClientLogEnum
            .getLogNameKey());
        //stat
        return this.getHttpClientStatReporter(statLog, statRollingPolicy, statLogReserveConfig);
    }

    protected AbstractSzTracerStatisticReporter getHttpClientStatReporter(String statTracerName,
                                                                            String statRollingPolicy,
                                                                            String statLogReserveConfig) {

        if (SzTracerConfiguration.isJsonOutput()) {
            return new HttpClientStatJsonReporter(statTracerName, statRollingPolicy, statLogReserveConfig);
        } else {
            return new HttpClientStatReporter(statTracerName, statRollingPolicy,
                statLogReserveConfig);
        }

    }
}
