package com.wayue.tracer.core.appender.builder;


import com.wayue.tracer.core.utils.StringUtils;

/**
 * JsonStringBuilder
 * <p>
 * String splicing tool for convenient log output
 * </p>
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class JsonStringBuilder {

    private static final int DEFAULT_BUFFER_SIZE = 256;

    private StringBuilder    sb;

    private boolean          isValueNullCheck    = false;

    public JsonStringBuilder() {
        this(false, DEFAULT_BUFFER_SIZE);
    }

    public JsonStringBuilder(boolean isValueNullCheck) {
        this(isValueNullCheck, DEFAULT_BUFFER_SIZE);
    }

    public JsonStringBuilder(boolean isValueNullCheck, int size) {
        this.isValueNullCheck = isValueNullCheck;
        this.sb = new StringBuilder(size);
    }

    public JsonStringBuilder appendBegin() {
        sb.append('{');
        return this;
    }

    public JsonStringBuilder appendBegin(String key, Object value) {
        this.appendBegin();
        this.append(key, value);
        return this;
    }

    public JsonStringBuilder append(String key, Object value) {
        if (value == null) {
            if (this.isValueNullCheck) {
                return this;
            }
        }
        this.append(key, value, ',');
        return this;
    }

    public JsonStringBuilder appendEnd() {
        return this.appendEnd(true);
    }

    public JsonStringBuilder appendEnd(boolean isNewLine) {
        if (this.sb.charAt(sb.length() - 1) == ',') {
            this.sb.deleteCharAt(sb.length() - 1);
        }
        this.sb.append('}');
        if (isNewLine) {
            this.sb.append(StringUtils.NEWLINE);
        }
        return this;
    }

    public JsonStringBuilder appendEnd(String key, Object value) {
        return this.appendEnd(key, value, true);
    }

    public JsonStringBuilder appendEnd(String key, Object value, boolean isNewLine) {
        if (value == null) {
            if (this.isValueNullCheck) {
                return this.appendEnd(isNewLine);
            } else {
                this.append(key, value, '}');
            }
        } else {
            this.append(key, value, '}');
        }
        if (isNewLine) {
            this.sb.append(StringUtils.NEWLINE);
        }
        return this;
    }

    private JsonStringBuilder append(String key, Object value, char endChar) {
        if (value == null) {
            //value blank
            this.sb.append('"').append(key).append('"').append(':').append('"').append("")
                .append('"').append(endChar);
            return this;
        }
        if (value instanceof String) {
            String valueStr = (String) value;
            if (valueStr.length() <= 0 || (valueStr.charAt(0) != '{' && valueStr.charAt(0) != '[')) {
                //string
                this.sb.append('"').append(key).append('"').append(':').append('"').append(value)
                    .append('"').append(endChar);
                return this;
            }
        }
        //array/object/number/boolean
        this.sb.append('"').append(key).append('"').append(':').append(value).append(endChar);
        return this;
    }

    /**
     * @return JsonStringBuilder
     */
    public JsonStringBuilder reset() {
        sb.delete(0, sb.length());
        return this;
    }

    /**
     * @return string
     */
    @Override
    public String toString() {
        return sb.toString();
    }
}
