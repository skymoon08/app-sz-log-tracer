package com.wayyue.tracer.core.registry;

import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import io.opentracing.propagation.Format;

public interface RegistryExtractorInjector<T> {

    /**
     * As the keyword key or header identification information of the cross-process transmission field,
     * its value is the serialization representation of {@link com.wayyue.tracer.core.context.span.SzTracerSpanContext}: sofa tracer head
     */
    String FORMATER_KEY_HEAD = "sftc_head";

    /**
     * Get supported format types
     * @return Format type {@link Format}
     */
    Format<T> getFormatType();

    /**
     * Extract the Span context from the payload
     *
     * @param carrier payload
     * @return SpanContext
     */
    SzTracerSpanContext extract(T carrier);

    /**
     * Inject a Span context into the payload
     * @param spanContext The span context to be injected or serialized
     * @param carrier payload
     */
    void inject(SzTracerSpanContext spanContext, T carrier);
}
