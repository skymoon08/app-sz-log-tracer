package com.wayue.tracer.core.registry;

import com.wayue.tracer.core.constants.SzTracerConstant;
import com.wayue.tracer.core.utils.StringUtils;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * HttpHeadersFormatter
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class HttpHeadersFormatter extends AbstractTextFormatter {

    @Override
    public Format<TextMap> getFormatType() {
        return Format.Builtin.HTTP_HEADERS;
    }

    @Override
    protected String encodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        try {
            return URLEncoder.encode(value, SzTracerConstant.DEFAULT_UTF8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // not much we can do, try raw value
            return value;
        }
    }

    @Override
    protected String decodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        try {
            return URLDecoder.decode(value, SzTracerConstant.DEFAULT_UTF8_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // not much we can do, try raw value
            return value;
        }
    }
}
