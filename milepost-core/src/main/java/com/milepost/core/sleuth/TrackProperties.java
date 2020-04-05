package com.milepost.core.sleuth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/4/5.
 * 链路跟踪功能配置
 */
@Component
@ConfigurationProperties(prefix="track")
public class TrackProperties {

    /**
     * 链路跟踪开关，true打开，false关闭，默认true
     */
    private boolean enabled = true;

    /**
     * 链路跟踪采样率，0<=sampling<=1， 默认0.1。
     */
    private float sampling = 0.1f;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSampling() {
        return sampling;
    }

    public void setSampling(float sampling) {
        this.sampling = sampling;
    }

    @Override
    public String toString() {
        return "TrackProperties{" +
                "enabled=" + enabled +
                ", sampling=" + sampling +
                '}';
    }
}
