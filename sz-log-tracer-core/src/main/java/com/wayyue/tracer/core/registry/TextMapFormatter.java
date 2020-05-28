package com.wayyue.tracer.core.registry;

import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

/**
 * TextMapFormatter
 *
 */
public class TextMapFormatter extends AbstractTextFormatter {

    @Override
    public Format<TextMap> getFormatType() {
        return Format.Builtin.TEXT_MAP;
    }

    @Override
    protected String encodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        return value;
    }

    @Override
    protected String decodedValue(String value) {
        if (StringUtils.isBlank(value)) {
            return StringUtils.EMPTY_STRING;
        }
        return value;
    }
}
