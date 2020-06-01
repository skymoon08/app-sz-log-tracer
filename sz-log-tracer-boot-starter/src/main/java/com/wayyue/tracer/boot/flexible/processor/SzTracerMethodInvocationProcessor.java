
package com.wayyue.tracer.boot.flexible.processor;

import com.wayyue.tracer.core.span.CommonSpanTags;
import com.wayyue.tracer.core.span.SzTracerSpan;
import com.wayyue.tracer.core.utils.StringUtils;
import com.wayyue.tracer.flexible.plugins.FlexibleTracer;
import com.wayyue.tracer.flexible.plugins.annotations.Tracer;
import org.aopalliance.intercept.MethodInvocation;


public class SzTracerMethodInvocationProcessor implements MethodInvocationProcessor {

    private io.opentracing.Tracer tracer;

    public SzTracerMethodInvocationProcessor(io.opentracing.Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Object process(MethodInvocation invocation, Tracer tracerSpan) throws Throwable {
        return proceedProxyMethodWithTracerAnnotation(invocation, tracerSpan);
    }

    private Object proceedProxyMethodWithTracerAnnotation(MethodInvocation invocation, Tracer tracerSpan) throws Throwable {
        if (tracer instanceof FlexibleTracer) {
            try {
                String operationName = tracerSpan.operateName();
                if (StringUtils.isBlank(operationName)) {
                    operationName = invocation.getMethod().getName();
                }
                SzTracerSpan szTracerSpan = ((FlexibleTracer) tracer)
                    .beforeInvoke(operationName);
                szTracerSpan.setTag(CommonSpanTags.METHOD, invocation.getMethod().getName());
                if (invocation.getArguments() != null && invocation.getArguments().length != 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (Object obj : invocation.getArguments()) {
                        stringBuilder.append(obj.getClass().getName()).append(";");
                    }
                    szTracerSpan.setTag("param.types", stringBuilder.toString().substring(0, stringBuilder.length() - 1));
                }
                return invocation.proceed();
            } catch (Throwable t) {
                ((FlexibleTracer) tracer).afterInvoke(t.getMessage());
                throw t;
            } finally {
                ((FlexibleTracer) tracer).afterInvoke();
            }
        } else {
            return invocation.proceed();
        }
    }

}
