package com.wayyue.tracer.core.samplers;

import java.util.HashMap;
import java.util.Map;

public class SamplingStatus {

    private boolean isSampled = false;

    /**
     * Allow tags to be placed at RootSpan
     */
    private Map<String, Object> tags = new HashMap<String, Object>();

    public boolean isSampled() {
        return isSampled;
    }

    public void setSampled(boolean sampled) {
        isSampled = sampled;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }
}
