package com.wayyue.tracer.boot.flexible.aop;

import com.wayyue.tracer.flexible.plugins.annotations.Tracer;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class TracerAnnotationClassPointcut extends DynamicMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> aClass, Object... objects) {
        return true;
    }

    @Override
    public ClassFilter getClassFilter() {
        return new ClassFilter() {
            @Override
            public boolean matches(Class<?> aClass) {
                return new AnnotationClassOrMethodFilter(Tracer.class).matches(aClass);
            }
        };
    }

    private final class AnnotationClassOrMethodFilter extends AnnotationClassFilter {

        private final AnnotationMethodsResolver methodsResolver;

        AnnotationClassOrMethodFilter(Class<? extends Annotation> annotationType) {
            super(annotationType, true);
            this.methodsResolver = new AnnotationMethodsResolver(annotationType);
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return super.matches(clazz) || this.methodsResolver.hasAnnotatedMethods(clazz);
        }

    }

    private static class AnnotationMethodsResolver {

        private final Class<? extends Annotation> annotationType;

        AnnotationMethodsResolver(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        boolean hasAnnotatedMethods(Class<?> clazz) {
            final AtomicBoolean found = new AtomicBoolean(false);
            ReflectionUtils.MethodCallback mc = new ReflectionUtils.MethodCallback() {
                @Override
                public void doWith(Method method) throws IllegalArgumentException {
                    if (found.get()) {
                        return;
                    }
                    Annotation annotation = AnnotationUtils.findAnnotation(method,
                        AnnotationMethodsResolver.this.annotationType);
                    if (annotation != null) {
                        found.set(true);
                    }
                }
            };
            ReflectionUtils.doWithMethods(clazz, mc);
            return found.get();
        }
    }
}
