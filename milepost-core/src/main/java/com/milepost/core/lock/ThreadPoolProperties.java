package com.milepost.core.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/3/11.
 * 线程池参数配置
 */
@Component
@ConfigurationProperties(prefix="thread-pool")
public class ThreadPoolProperties {

    /**
     * 核心(最小)线程数，默认值5
     */
    private int corePoolSize = 5;

    /**
     * 线程池最大线程数，默认值200
     */
    private int maxPoolSize = 200;

    /**
     * 线程队列最大线程数，默认值10
     */
    private int queueCapacity = 10;

    /**
     * 线程所允许的空闲时间，默认60s
     */
    private int keepAliveSeconds = 60;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    @Override
    public String toString() {
        return "ThreadPoolProperties{" +
                "corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", queueCapacity=" + queueCapacity +
                ", keepAliveSeconds=" + keepAliveSeconds +
                '}';
    }
}
