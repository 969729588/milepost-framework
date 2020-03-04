package com.milepost.core.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/3/4.<br>
 * 分布式同步锁配置
 */
@Component
@ConfigurationProperties(prefix="synchronized-lock")
public class SynchronizedLockProperties {
    /**
     * 是否启用，默认true，启用，
     * 如不启用则不能使用@SynchronizedLock。
     */
    private boolean enabled = true;

    /**
     * 分布式同步锁重试获取锁的间隔时间，单位ms，默认1000ms
     */
    private long retryIntervalInMilliseconds = 1000;
    /**
     * 每一个实例能占有锁的最长时间，单位s，默认45s
     */
    private int holdDurationInSeconds = 45;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getRetryIntervalInMilliseconds() {
        return retryIntervalInMilliseconds;
    }

    public void setRetryIntervalInMilliseconds(long retryIntervalInMilliseconds) {
        this.retryIntervalInMilliseconds = retryIntervalInMilliseconds;
    }

    public int getHoldDurationInSeconds() {
        return holdDurationInSeconds;
    }

    public void setHoldDurationInSeconds(int holdDurationInSeconds) {
        this.holdDurationInSeconds = holdDurationInSeconds;
    }

    @Override
    public String toString() {
        return "SynchronizedLockProperties{" +
                "enabled=" + enabled +
                ", retryIntervalInMilliseconds=" + retryIntervalInMilliseconds +
                ", holdDurationInSeconds=" + holdDurationInSeconds +
                '}';
    }
}
