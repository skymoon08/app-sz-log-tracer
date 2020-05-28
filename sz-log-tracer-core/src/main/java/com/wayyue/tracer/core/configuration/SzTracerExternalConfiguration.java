package com.wayyue.tracer.core.configuration;

/**
 * The interface of the external configuration class, which is implemented by external code
 */
public interface SzTracerExternalConfiguration {

    /**
     * get value by input key
     * @param key
     * @return
     */
    String getValue(String key);

    /**
     * Whether the specified key exists
     * @param key
     * @return
     */
    boolean contains(String key);

}