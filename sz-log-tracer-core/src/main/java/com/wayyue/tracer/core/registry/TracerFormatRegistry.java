package com.wayyue.tracer.core.registry;

import io.opentracing.propagation.Format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TracerFormatRegistry
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class TracerFormatRegistry {

    private final static Map<Format<?>, RegistryExtractorInjector<?>> injectorsAndExtractors = new ConcurrentHashMap<Format<?>, RegistryExtractorInjector<?>>();

    static {
        TextMapFormatter textMapFormatter = new TextMapFormatter();
        HttpHeadersFormatter httpHeadersFormatter = new HttpHeadersFormatter();
        BinaryFormater binaryFormater = new BinaryFormater();
        TextMapB3Formatter textMapB3Formatter = new TextMapB3Formatter();
        HttpHeadersB3Formatter httpHeadersB3Formatter = new HttpHeadersB3Formatter();
        injectorsAndExtractors.put(textMapFormatter.getFormatType(), textMapFormatter);
        injectorsAndExtractors.put(httpHeadersFormatter.getFormatType(), httpHeadersFormatter);
        injectorsAndExtractors.put(binaryFormater.getFormatType(), binaryFormater);
        injectorsAndExtractors.put(textMapB3Formatter.getFormatType(), textMapB3Formatter);
        injectorsAndExtractors.put(httpHeadersB3Formatter.getFormatType(), httpHeadersB3Formatter);
    }

    @SuppressWarnings("unchecked")
    public static <T> RegistryExtractorInjector<T> getRegistry(Format<T> format) {
        return (RegistryExtractorInjector<T>) injectorsAndExtractors.get(format);
    }

    public static <T> void register(Format<T> format, RegistryExtractorInjector<T> extractor) {
        injectorsAndExtractors.put(format, extractor);
    }
}
