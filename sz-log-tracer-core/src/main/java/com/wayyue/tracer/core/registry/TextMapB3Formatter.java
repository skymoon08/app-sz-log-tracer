package com.wayyue.tracer.core.registry;

import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

/**
 * TextMapFormatter
 *
 * @author jinming.xiao
 * @since 2020/06/01
 * @since 2017/06/23
 */
public class TextMapB3Formatter extends AbstractTextB3Formatter {

    @Override
    public Format<TextMap> getFormatType() {
        return ExtendFormat.Builtin.B3_TEXT_MAP;
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
