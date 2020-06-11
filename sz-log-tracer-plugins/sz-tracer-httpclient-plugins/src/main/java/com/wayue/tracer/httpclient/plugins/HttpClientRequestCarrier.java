package com.wayue.tracer.httpclient.plugins;

import io.opentracing.propagation.TextMap;
import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHeader;

import java.util.Iterator;
import java.util.Map;

/**
 * HttpClientRequestCarrier
 *
 * @author zhanglong
 * @since 2020/06/01
 */
public class HttpClientRequestCarrier implements TextMap {

    private final HttpRequest request;

    public HttpClientRequestCarrier(HttpRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        // no operation
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(String key, String value) {
        request.addHeader(new BasicHeader(key, value));
    }
}
