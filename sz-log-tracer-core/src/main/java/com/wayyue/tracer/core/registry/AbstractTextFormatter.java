/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wayyue.tracer.core.registry;


import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.utils.StringUtils;
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
