package com.milepost.core.multipleTenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2020/1/13.
 * 多租户相关配置
 */
@Component
@ConfigurationProperties(prefix="multiple-tenant")
public class MultipleTenantProperties {
    /**
     * 租户，默认为 default，不区分大小写，不支持逗号分割。<br>
     * 如果当前服务未设置租户，则他可以选择设置或未设置租户的所有服务，<br>
     * 如果当前服务设置了租户，则只选择与他设置了相同租户的服务。
     */
    private String tenant = "default";
    /**
     * 权重，0和正整数，默认1，<br>
     * 负载均衡计算方法：<br>
     * 一个服务被选中的概率是这个服务的权重/所有服务权重之和，<br>
     * 权重为0的服务永远不会被选中，<br>
     */
    private Integer weight = 1;
    /**
     * 与标签，<br>
     * 格式：aa,bb,cc，多个标签支持逗号分割。<br>
     * 过滤算法：两个服务实例的标签完全相等才选中服务实例，即标签集合中元素个数相等，元素相等，不区分顺序。<br>
     * 如果当前服务同时设置了或标签和与标签，则以或标签为准，忽略与标签，因为或标签过滤结果为空时，与标签一定为空。<br>
     * 如果当前服务未设置或标签，也未设置与标签，则他可以选择设置或未设置标签的所有服务。<br>
     */
    private String labelAnd;
    /**
     * 或标签，<br>
     * 格式：aa,bb,cc，多个标签支持逗号分割。<br>
     * 过滤算法：交集不为空，就选中服务实例<br>
     */
    private String labelOr;

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getLabelAnd() {
        return labelAnd;
    }

    public void setLabelAnd(String labelAnd) {
        this.labelAnd = labelAnd;
    }

    public String getLabelOr() {
        return labelOr;
    }

    public void setLabelOr(String labelOr) {
        this.labelOr = labelOr;
    }


    @Override
    public String toString() {
        return "MultipleTenantProperties{" +
                "tenant='" + tenant + '\'' +
                ", weight=" + weight +
                ", labelAnd='" + labelAnd + '\'' +
                ", labelOr='" + labelOr + '\'' +
                '}';
    }
}
