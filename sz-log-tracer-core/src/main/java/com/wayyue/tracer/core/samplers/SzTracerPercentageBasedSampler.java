package com.wayyue.tracer.core.samplers;


import com.wayyue.tracer.core.constants.SzTracerConstant;
import com.wayyue.tracer.core.span.SzTracerSpan;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SzTracerPercentageBasedSampler
 *
 */
public class SzTracerPercentageBasedSampler implements Sampler {

    public static final String TYPE = "PercentageBasedSampler";
    private final AtomicLong counter = new AtomicLong(0);
    private final BitSet sampleDecisions;
    private final SamplerProperties configuration;


    public SzTracerPercentageBasedSampler(SamplerProperties configuration) {
        int outOf100 = (int) (configuration.getPercentage());
        this.sampleDecisions = randomBitSet(100, outOf100, new Random());
        this.configuration = configuration;
    }

    @Override
    public SamplingStatus sample(SzTracerSpan szTracerSpan) {
        SamplingStatus samplingStatus = new SamplingStatus();
        Map<String, Object> tags = new HashMap<String, Object>();
        tags.put(SzTracerConstant.SAMPLER_TYPE_TAG_KEY, TYPE);
        tags.put(SzTracerConstant.SAMPLER_PARAM_TAG_KEY, configuration.getPercentage());
        tags = Collections.unmodifiableMap(tags);
        samplingStatus.setTags(tags);

        if (this.configuration.getPercentage() == 0) {
            samplingStatus.setSampled(false);
            return samplingStatus;
        } else if (this.configuration.getPercentage() == 100) {
            samplingStatus.setSampled(true);
            return samplingStatus;
        }
        boolean result = this.sampleDecisions.get((int) (this.counter.getAndIncrement() % 100));
        samplingStatus.setSampled(result);
        return samplingStatus;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void close() {
        //do nothing
    }

    /**
     * Reservoir sampling algorithm borrowed from Stack Overflow.
     * <p>
     * http://stackoverflow.com/questions/12817946/generate-a-random-bitset-with-n-1s
     * @param size
     * @param cardinality
     * @param rnd
     * @return BitSet
     */
    public static BitSet randomBitSet(int size, int cardinality, Random rnd) {
        BitSet result = new BitSet(size);
        int[] chosen = new int[cardinality];
        int i;
        for (i = 0; i < cardinality; ++i) {
            chosen[i] = i;
            result.set(i);
        }
        for (; i < size; ++i) {
            int j = rnd.nextInt(i + 1);
            if (j < cardinality) {
                result.clear(chosen[j]);
                result.set(i);
                chosen[j] = i;
            }
        }
        return result;
    }
}
