package com.wayue.tracer.plugins.springmvc;


import io.opentracing.propagation.TextMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class SpringMvcHeadersCarrier implements TextMap {
    private HashMap<String, String> headers;

    public SpringMvcHeadersCarrier(HashMap<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public void put(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return headers.entrySet().iterator();
    }
}
