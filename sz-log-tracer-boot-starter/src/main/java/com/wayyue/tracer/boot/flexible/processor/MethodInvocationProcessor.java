
package com.wayyue.tracer.boot.flexible.processor;

import com.wayyue.tracer.flexible.plugins.annotations.Tracer;
import org.aopalliance.intercept.MethodInvocation;

public interface MethodInvocationProcessor {

    /**
     * proxy method
     * @param invocation
     * @param tracerSpan
     * @return
     * @throws Throwable
     */
    Object process(MethodInvocation invocation, Tracer tracerSpan) throws Throwable;
}
