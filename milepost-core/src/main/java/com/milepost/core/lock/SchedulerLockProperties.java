package com.milepost.core.lock;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/3/4.<br>
 * 分布式调度锁配置
 */
@Component
@ConfigurationProperties(prefix="scheduler-lock")
public class SchedulerLockProperties {

    /**
     * 是否启用，默认true，启用，
     * 如不启用则不能使用@SchedulerLock、InstanceRoleService。
     */
    private boolean enabled = true;
    /**
     * 更新心跳时间间隔，单位s，默认15s
     */
    private int touchHeartbeatIntervalInSeconds = 15;
    /**
     * 心跳过期时长，单位s，默认45s
     */
    private int heartbeatExpirationDurationInSeconds = 45;

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

    @Override
    public String toString() {
        return "SchedulerLockProperties{" +
                "enabled=" + enabled +
                ", touchHeartbeatIntervalInSeconds=" + touchHeartbeatIntervalInSeconds +
                ", heartbeatExpirationDurationInSeconds=" + heartbeatExpirationDurationInSeconds +
                '}';
    }
}
