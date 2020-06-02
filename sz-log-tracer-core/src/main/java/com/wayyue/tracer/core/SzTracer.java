
package com.wayyue.tracer.core;

import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.core.constants.ComponentNameConstants;
import com.wayyue.tracer.core.context.span.SzTracerSpanContext;
import com.wayyue.tracer.core.generator.TraceIdGenerator;
import com.wayyue.tracer.core.listener.SpanReportListener;
import com.wayyue.tracer.core.listener.SpanReportListenerHolder;
import com.wayyue.tracer.core.registry.RegistryExtractorInjector;
import com.wayyue.tracer.core.registry.TracerFormatRegistry;
import com.wayyue.tracer.core.reporter.facade.Reporter;
import com.wayyue.tracer.core.samplers.Sampler;
import com.wayyue.tracer.core.samplers.SamplerFactory;
import com.wayyue.tracer.core.samplers.SamplingStatus;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.span.SzTracerSpanReferenceRelationship;
import com.wayyue.tracer.core.utils.AssertUtils;
import com.wayyue.tracer.core.utils.StringUtils;
import io.opentracing.*;
import io.opentracing.propagation.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SzTracer
 */
public class SzTracer implements Tracer {

    /**
     * normal root spanId's default value
     */
    public static final String ROOT_SPAN_ID = "0";

    /**
     * Mark the type of tracer
     */
    private final String tracerType;

    /**
     * Reporter as a client runtime
     */
    private final Reporter clientReporter;

    /**
     * Reporter as a server runtime
     */
    private final Reporter serverReporter;

    /**
     * Cache some information related to the tracer globally
     */
    private final Map<String, Object> tracerTags = new ConcurrentHashMap<>();

    /**
     * Sampler instance
     */
    private final Sampler sampler;

    protected SzTracer(String tracerType, Reporter clientReporter, Reporter serverReporter,
                       Sampler sampler, Map<String, Object> tracerTags) {
        this.tracerType = tracerType;
        this.clientReporter = clientReporter;
        this.serverReporter = serverReporter;
        this.sampler = sampler;
        if (tracerTags != null && tracerTags.size() > 0) {
            this.tracerTags.putAll(tracerTags);
        }
    }

    protected SzTracer(String tracerType, Sampler sampler) {
        this.tracerType = tracerType;
        this.clientReporter = null;
        this.serverReporter = null;
        this.sampler = sampler;
    }

//    // TODO: 2020/5/27
//    @Override
//    public ScopeManager scopeManager() {
//        return null;
//    }
//
//    // TODO: 2020/5/27
//    @Override
//    public Span activeSpan() {
//        return null;
//    }
//
//    // TODO: 2020/5/27
//    @Override
//    public Scope activateSpan(Span span) {
//        return null;
//    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SzTracerSpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        RegistryExtractorInjector<C> registryInjector = TracerFormatRegistry.getRegistry(format);
        if (registryInjector == null) {
            throw new IllegalArgumentException("Unsupported injector format: " + format);
        }
        registryInjector.inject((SzTracerSpanContext) spanContext, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {

        RegistryExtractorInjector<C> registryExtractor = TracerFormatRegistry.getRegistry(format);
        if (registryExtractor == null) {
            throw new IllegalArgumentException("Unsupported extractor format: " + format);
        }
        return registryExtractor.extract(carrier);
    }

    public void reportSpan(SzTracerSpan span) {
        if (span == null) {
            return;
        }
        // //sampler is support &  current span is root span
        if (sampler != null && span.getParentSzTracerSpan() == null) {
            span.getSzTracerSpanContext().setSampled(sampler.sample(span).isSampled());
        }
        //invoke listener
        this.invokeReportListeners(span);
        if (span.isClient() || this.getTracerType().equalsIgnoreCase(ComponentNameConstants.FLEXIBLE)) {
            if (this.clientReporter != null) {
                this.clientReporter.report(span);
            }
        } else if (span.isServer()) {
            if (this.serverReporter != null) {
                this.serverReporter.report(span);
            }
        } else {
            //ignore ,do not statical
            SelfDefineLog.warn("Span reported neither client nor server.Ignore!");
        }
    }

    /**
     * Shuts down the {@link Reporter}  and {@link Sampler}
     */
    public void close() {
        if (this.clientReporter != null) {
            this.clientReporter.close();
        }
        if (this.serverReporter != null) {
            this.serverReporter.close();
        }

        if (sampler != null) {
            this.sampler.close();
        }
    }

    public String getTracerType() {
        return tracerType;
    }

    public Reporter getClientReporter() {
        return clientReporter;
    }

    public Reporter getServerReporter() {
        return serverReporter;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public Map<String, Object> getTracerTags() {
        return tracerTags;
    }

    @Override
    public String toString() {
        return "SzTracer{" + "tracerType='" + tracerType + '}';
    }

    protected void invokeReportListeners(SzTracerSpan tracerSpan) {
        List<SpanReportListener> listeners = SpanReportListenerHolder
                .getSpanReportListenersHolder();
        if (listeners != null && listeners.size() > 0) {
            for (SpanReportListener listener : listeners) {
                listener.onSpanReport(tracerSpan);
            }
        }
    }

    /**
     * SzTracerSpanBuilder is used to build Span inside Tracer
     */
    public class SzTracerSpanBuilder implements io.opentracing.Tracer.SpanBuilder {

        private String operationName;

        /**
         * Default initialization time
         */
        private long startTime = -1;

        /**
         * In 99% situations there is only one parent (childOf), so we do not want to allocate
         * a collection of references.
         */
        private List<SzTracerSpanReferenceRelationship> references = Collections.emptyList();

        private final Map<String, Object> tags = new HashMap<>();

        public SzTracerSpanBuilder(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public Tracer.SpanBuilder asChildOf(SpanContext parent) {
            return addReference(References.CHILD_OF, parent);
        }

        @Override
        public Tracer.SpanBuilder asChildOf(Span parentSpan) {
            if (parentSpan == null) {
                return this;
            }
            return addReference(References.CHILD_OF, parentSpan.context());
        }

        @Override
        public Tracer.SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            if (referencedContext == null) {
                return this;
            }
            if (!(referencedContext instanceof SzTracerSpanContext)) {
                return this;
            }
            if (!References.CHILD_OF.equals(referenceType)
                    && !References.FOLLOWS_FROM.equals(referenceType)) {
                return this;
            }
            if (references.isEmpty()) {
                // Optimization for 99% situations, when there is only one parent
                references = Collections.singletonList(new SzTracerSpanReferenceRelationship(
                        (SzTracerSpanContext) referencedContext, referenceType));
            } else {
                if (references.size() == 1) {
                    //To ensure order
                    references = new ArrayList<>(references);
                }
                references.add(new SzTracerSpanReferenceRelationship(
                        (SzTracerSpanContext) referencedContext, referenceType));
            }
            return this;
        }
//
//        // TODO: 2020/5/27
//        @Override
//        public SpanBuilder ignoreActiveSpan() {
//            return null;
//        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, boolean value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, Number value) {
            this.tags.put(key, value);
            return this;
        }

        // TODO: 2020/5/27
//        @Override
//        public <T> SpanBuilder withTag(Tag<T> tag, T t) {
//            return null;
//        }

        @Override
        public SpanBuilder withStartTimestamp(long microseconds) {
            this.startTime = microseconds;
            return this;
        }

        @Override
        public Span start() {
            SzTracerSpanContext szTracerSpanContext;

            if (this.references != null && this.references.size() > 0) {
                //Parent context exist
                szTracerSpanContext = this.createChildContext();
            } else {
                //Start with new root span context
                szTracerSpanContext = this.createRootSpanContext();
            }

            long begin = this.startTime > 0 ? this.startTime : System.currentTimeMillis();
            SzTracerSpan szTracerSpan = new SzTracerSpan(SzTracer.this, begin, this.references, this.operationName, szTracerSpanContext, this.tags);

            // calculate isSampled，but do not change parent's sampler behaviour
            boolean isSampled = calculateSampler(szTracerSpan);
            szTracerSpanContext.setSampled(isSampled);

            return szTracerSpan;
        }

        private boolean calculateSampler(SzTracerSpan SzTracerSpan) {
            boolean isSampled = false;
            if (this.references != null && this.references.size() > 0) {
                SzTracerSpanContext preferredReference = preferredReference();
                isSampled = preferredReference.isSampled();
            } else {
                if (sampler != null) {
                    SamplingStatus samplingStatus = sampler.sample(SzTracerSpan);
                    if (samplingStatus.isSampled()) {
                        isSampled = true;
                        //After the sampling occurs, the related attribute records
                        this.tags.putAll(samplingStatus.getTags());
                    }
                }
            }

            return isSampled;
        }

        private SzTracerSpanContext createRootSpanContext() {
            //generate traceId
            String traceId = TraceIdGenerator.generate();
            return new SzTracerSpanContext(traceId, ROOT_SPAN_ID, StringUtils.EMPTY_STRING);
        }

        private SzTracerSpanContext createChildContext() {
            SzTracerSpanContext preferredReference = preferredReference();

            SzTracerSpanContext szTracerSpanContext = new SzTracerSpanContext(
                    preferredReference.getTraceId(), preferredReference.nextChildContextId(),
                    preferredReference.getSpanId(), preferredReference.isSampled());
            szTracerSpanContext.addBizBaggage(this.createChildBaggage(true));
            szTracerSpanContext.addSysBaggage(this.createChildBaggage(false));
            return szTracerSpanContext;
        }

        private Map<String, String> createChildBaggage(boolean isBiz) {
            // optimization for 99% use cases, when there is only one parent
            if (references.size() == 1) {
                if (isBiz) {
                    return references.get(0).getSzTracerSpanContext().getBizBaggage();
                } else {
                    return references.get(0).getSzTracerSpanContext().getSysBaggage();
                }
            }
            Map<String, String> baggage = null;
            for (SzTracerSpanReferenceRelationship reference : references) {
                Map<String, String> referenceBaggage;
                if (isBiz) {
                    referenceBaggage = reference.getSzTracerSpanContext().getBizBaggage();
                } else {
                    referenceBaggage = reference.getSzTracerSpanContext().getSysBaggage();
                }
                if (referenceBaggage != null && referenceBaggage.size() > 0) {
                    if (baggage == null) {
                        baggage = new HashMap<>();
                    }
                    baggage.putAll(referenceBaggage);
                }
            }
            return baggage;
        }

        private SzTracerSpanContext preferredReference() {
            SzTracerSpanReferenceRelationship preferredReference = references.get(0);
            for (SzTracerSpanReferenceRelationship reference : references) {
                // childOf takes precedence as a preferred parent
                String referencedType = reference.getReferenceType();
                if (References.CHILD_OF.equals(referencedType)
                        && !References.CHILD_OF.equals(preferredReference.getReferenceType())) {
                    preferredReference = reference;
                    break;
                }
            }
            return preferredReference.getSzTracerSpanContext();
        }
    }

    public static final class Builder {

        private final String tracerType;

        private Reporter clientReporter;

        private Reporter serverReporter;

        private Map<String, Object> tracerTags = new HashMap<String, Object>();

        private Sampler sampler;

        public Builder(String tracerType) {
            AssertUtils.isTrue(StringUtils.isNotBlank(tracerType), "tracerType must be not empty");
            this.tracerType = tracerType;
        }

        public Builder withClientReporter(Reporter clientReporter) {
            this.clientReporter = clientReporter;
            return this;
        }

        public Builder withServerReporter(Reporter serverReporter) {
            this.serverReporter = serverReporter;
            return this;
        }

        public Builder withSampler(Sampler sampler) {
            this.sampler = sampler;
            return this;
        }

        public Builder withTag(String key, String value) {
            tracerTags.put(key, value);
            return this;
        }

        public Builder withTag(String key, Boolean value) {
            tracerTags.put(key, value);
            return this;
        }

        public Builder withTag(String key, Number value) {
            tracerTags.put(key, value);
            return this;
        }

        public Builder withTags(Map<String, ?> tags) {
            if (tags == null || tags.size() <= 0) {
                return this;
            }
            for (Map.Entry<String, ?> entry : tags.entrySet()) {
                String key = entry.getKey();
                if (StringUtils.isBlank(key)) {
                    continue;
                }
                Object value = entry.getValue();
                if (value == null) {
                    continue;
                }
                if (value instanceof String) {
                    this.withTag(key, (String) value);
                } else if (value instanceof Boolean) {
                    this.withTag(key, (Boolean) value);
                } else if (value instanceof Number) {
                    this.withTag(key, (Number) value);
                } else {
                    SelfDefineLog.error("Tracer tags unsupported type [" + value.getClass() + "]");
                }
            }
            return this;
        }

        public SzTracer build() {
            try {
                sampler = SamplerFactory.getSampler();
            } catch (Exception e) {
                SelfDefineLog.error("Failed to get tracer sampler strategy;");
            }
            return new SzTracer(this.tracerType, this.clientReporter, this.serverReporter,
                    this.sampler, this.tracerTags);
        }
    }
}
