package com.milepost.core.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/1/8.
 * 调度锁，配置文件加载set方法上才会在yml文件中显示提示
 */
@Component
@ConfigurationProperties(prefix="scheduled-lock")
public class SchedulerLockProperties {
    /**
     * 是否启用，默认true，启用，
     * 如不启用则不能使用@SynchronizedLock、@SchedulerLock、InstanceRoleService。
     */
    private boolean enabled = true;
    /**
     * 更新心跳时间间隔，单位s，默认15s
     */
    private int touchHeartbeatIntervalInSeconds = 15;
    /**
     * 心跳失效时长，单位s，默认45s
     */
    private int heartbeatExpirationDurationInSeconds = 45;
    /**
     * 分布式同步锁重试获取锁的间隔时间，单位ms，默认1000ms
     */
    private long synchronizedLockRetryIntervalInMilliseconds = 1000;
    /**
     * 每一个实例能占有锁的最长时间，单位s，默认45s
     */
    private int synchronizedLockHoldDurationInSeconds = 45;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTouchHeartbeatIntervalInSeconds() {
        return touchHeartbeatIntervalInSeconds;
    }

    public void setTouchHeartbeatIntervalInSeconds(int touchHeartbeatIntervalInSeconds) {
        this.touchHeartbeatIntervalInSeconds = touchHeartbeatIntervalInSeconds;
    }

    public int getHeartbeatExpirationDurationInSeconds() {
        return heartbeatExpirationDurationInSeconds;
    }

    public void setHeartbeatExpirationDurationInSeconds(int heartbeatExpirationDurationInSeconds) {
        this.heartbeatExpirationDurationInSeconds = heartbeatExpirationDurationInSeconds;
    }

    public long getSynchronizedLockRetryIntervalInMilliseconds() {
        return synchronizedLockRetryIntervalInMilliseconds;
    }

    public void setSynchronizedLockRetryIntervalInMilliseconds(long synchronizedLockRetryIntervalInMilliseconds) {
        this.synchronizedLockRetryIntervalInMilliseconds = synchronizedLockRetryIntervalInMilliseconds;
    }

    public int getSynchronizedLockHoldDurationInSeconds() {
        return synchronizedLockHoldDurationInSeconds;
    }

    public void setSynchronizedLockHoldDurationInSeconds(int synchronizedLockHoldDurationInSeconds) {
        this.synchronizedLockHoldDurationInSeconds = synchronizedLockHoldDurationInSeconds;
    }

    @Override
    public String toString() {
        return "SchedulerLockProperties{" +
                "enabled=" + enabled +
                ", touchHeartbeatIntervalInSeconds=" + touchHeartbeatIntervalInSeconds +
                ", heartbeatExpirationDurationInSeconds=" + heartbeatExpirationDurationInSeconds +
                ", synchronizedLockRetryIntervalInMilliseconds=" + synchronizedLockRetryIntervalInMilliseconds +
                ", synchronizedLockHoldDurationInSeconds=" + synchronizedLockHoldDurationInSeconds +
                '}';
    }
}
