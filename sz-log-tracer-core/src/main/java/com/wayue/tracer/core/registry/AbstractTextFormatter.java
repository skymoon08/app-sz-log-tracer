package com.wayue.tracer.core.registry;


import com.wayue.tracer.core.context.span.SzTracerSpanContext;
import com.wayue.tracer.core.utils.StringUtils;
import io.opentracing.propagation.TextMap;

import java.util.Map;

/**
 * AbstractTextFormatter
 *
 */
public abstract class AbstractTextFormatter implements RegistryExtractorInjector<TextMap> {

    @Override
    public SzTracerSpanContext extract(TextMap carrier) {
        if (carrier == null) {
            return null;
        }
        SzTracerSpanContext SzTracerSpanContext = null;
        for (Map.Entry<String, String> entry : carrier) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (FORMATER_KEY_HEAD.equalsIgnoreCase(key) && !StringUtils.isBlank(value)) {
                SzTracerSpanContext = SzTracerSpanContext.deserializeFromString(this
                    .decodedValue(value));
            }
        }
        if (SzTracerSpanContext == null) {
            return null;
        }
        return SzTracerSpanContext;
    }

    @Override
    public void inject(SzTracerSpanContext spanContext, TextMap carrier) {
        if (carrier == null || spanContext == null) {
            return;
        }
        carrier.put(FORMATER_KEY_HEAD, this.encodedValue(spanContext.serializeSpanContext()));
    }

    /**
     * Encode the specified value
     * @param value
     * @return encoded value
     */
    protected abstract String encodedValue(String value);

    /**
     * Decode the specified value
     * @param value
     * @return decoded value
     */
    protected abstract String decodedValue(String value);
}
