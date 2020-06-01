package com.wayyue.tracer.boot.flexible.processor;

import com.wayyue.tracer.core.appender.sefllog.SelfDefineLog;
import com.wayyue.tracer.flexible.plugins.annotations.Tracer;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class SzTracerIntroductionInterceptor implements IntroductionInterceptor {

    private final MethodInvocationProcessor szMethodInvocationProcessor;

    public SzTracerIntroductionInterceptor(MethodInvocationProcessor szMethodInvocationProcessor) {
        this.szMethodInvocationProcessor = szMethodInvocationProcessor;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (method == null) {
            return invocation.proceed();
        }
        Method mostSpecificMethod = AopUtils.getMostSpecificMethod(method, invocation.getThis()
            .getClass());

        Tracer tracerSpan = findAnnotation(mostSpecificMethod, Tracer.class);
        if (tracerSpan == null) {
            return invocation.proceed();
        }
        return szMethodInvocationProcessor.process(invocation, tracerSpan);
    }

    @Override
    public boolean implementsInterface(Class<?> aClass) {
        return true;
    }

    private <T extends Annotation> T findAnnotation(Method method, Class<T> clazz) {
        T annotation = AnnotationUtils.findAnnotation(method, clazz);
        if (annotation == null) {
            try {
                annotation = AnnotationUtils.findAnnotation(
                    method.getDeclaringClass().getMethod(method.getName(),
                        method.getParameterTypes()), clazz);
            } catch (NoSuchMethodException | SecurityException ex) {
                SelfDefineLog.warn("Exception occurred while tyring to find the annotation");
            }
        }
        return annotation;
    }
}
