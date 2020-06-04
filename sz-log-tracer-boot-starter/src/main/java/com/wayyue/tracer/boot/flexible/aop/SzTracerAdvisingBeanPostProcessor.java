package com.wayyue.tracer.boot.flexible.aop;


import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SzTracerAdvisingBeanPostProcessor extends AbstractAdvisingBeanPostProcessor implements BeanFactoryAware {

    private MethodInterceptor interceptor;

    public SzTracerAdvisingBeanPostProcessor(MethodInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        setBeforeExistingAdvisors(true);
        setExposeProxy(true);
        this.advisor = new TracerAnnotationClassAdvisor(this.interceptor);
    }
}
