package com.wayyue.tracer.boot.flexible.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;

public class TracerAnnotationClassAdvisor extends AbstractPointcutAdvisor {

    private Advice   advice;

    private Pointcut pointcut;

    public TracerAnnotationClassAdvisor(MethodInterceptor interceptor) {
        this.advice = interceptor;
        this.pointcut = new TracerAnnotationClassPointcut();
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

}
