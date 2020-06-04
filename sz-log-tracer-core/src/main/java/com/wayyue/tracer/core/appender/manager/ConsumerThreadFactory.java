package com.wayyue.tracer.core.appender.manager;

import java.util.concurrent.ThreadFactory;

/**
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class ConsumerThreadFactory implements ThreadFactory {
    private String workName;

    /**
     * Getter method for property <tt>workName</tt>.
     *
     * @return property value of workName
     */
    public String getWorkName() {
        return workName;
    }

    /**
     * Setter method for property <tt>workName</tt>.
     *
     * @param workName value to be assigned to property workName
     */
    public void setWorkName(String workName) {
        this.workName = workName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread worker = new Thread(runnable, "Tracer-AsyncConsumer-Thread-" + workName);
        worker.setDaemon(true);
        return worker;
    }
}