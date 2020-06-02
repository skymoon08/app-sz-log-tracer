package com.wayyue.tracer.core.registry;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

public interface ExtendFormat<C> extends Format<C> {
    final class Builtin<C> implements ExtendFormat<C> {
        private final String name;

        private Builtin(String name) {
            this.name = name;
        }

        public final static Format<TextMap> B3_TEXT_MAP     = new ExtendFormat.Builtin<TextMap>(
                                                                "B3_TEXT_MAP");
        public final static Format<TextMap> B3_HTTP_HEADERS = new ExtendFormat.Builtin<TextMap>(
                                                                "B3_HTTP_HEADERS");

        @Override
        public String toString() {
            return ExtendFormat.Builtin.class.getSimpleName() + "." + name;
        }
    }
}
