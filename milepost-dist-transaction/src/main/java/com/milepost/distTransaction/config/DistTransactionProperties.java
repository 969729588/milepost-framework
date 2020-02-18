package com.milepost.distTransaction.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/2/18.
 * 分布式事务属性
 */
@Component
@ConfigurationProperties(prefix="dist-transaction")
public class DistTransactionProperties {
    /**
     * 是否启用，默认false，不启用，
     * 对应seata.enabled
     */
    private boolean enabled = false;

    /**
     * 事务群组（可以每个应用独立取名，也可以使用相同的名字）,
     * 默认 default，
     * 对应seata.tx-service-group
     */
    private String txServiceGroup = "default";

    /**
     * 事务服务端注册到EurekaServer中的应用名称，
     * 默认 tx-server，
     * 对应seata.service.vgroup-mapping
     */
    private String txServerAppName = "tx-server";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTxServiceGroup() {
        return txServiceGroup;
    }

    public void setTxServiceGroup(String txServiceGroup) {
        this.txServiceGroup = txServiceGroup;
    }

    public String getTxServerAppName() {
        return txServerAppName;
    }

    public void setTxServerAppName(String txServerAppName) {
        this.txServerAppName = txServerAppName;
    }

    @Override
    public String toString() {
        return "DistTransactionProperties{" +
                "enabled=" + enabled +
                ", txServiceGroup='" + txServiceGroup + '\'' +
                ", txServerAppName='" + txServerAppName + '\'' +
                '}';
    }
}
