package com.wayyue.tracer.core.samplers;


import com.wayyue.tracer.core.span.SzTracerSpan;

public interface Sampler {

    /**
     * @param szTracerSpan The operation name set on the span
     * @return whether or not the new trace should be sampled
     */
    SamplingStatus sample(SzTracerSpan szTracerSpan);

    /**
     * get sampler type
     * @return
     */
    String getType();

    /**
     * Release any resources used by the sampler.
     */
    void close();
}
