package com.wayyue.tracer.core.appender.encoder;

import io.opentracing.Span;

import java.io.IOException;

/**
 * SpanEncoder
 * <p>
 * Tracer Span log encoder, optimized for asynchronous queue calls, does not allow multi-threaded concurrent calls
 * </p>
 * @author yangguanchao
 * @since 2017/06/25
 */
public interface SpanEncoder<T extends Span> {

    /**
     * Separate fields according to custom rules and prepare to output to file
     *
     * @param span current span
     * @throws IOException
     * @return formatted output string
     */
    String encode(T span) throws IOException;
}
