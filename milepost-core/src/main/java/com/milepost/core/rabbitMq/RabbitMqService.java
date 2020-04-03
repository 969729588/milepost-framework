package com.milepost.core.rabbitMq;

/**
 * Created by Ruifu Hua on 2020/4/3.
 */
public interface RabbitMqService {
    /**
     * 向指定租户下的所有服务实例发消息
     * @param tenant
     * @param message
     */
    void send2AllInstancesOfTheTenant(String tenant, String message);

    /**
     * 向当前租户下的所有服务实例发消息
     * @param message
     */
    void send2AllInstancesOfCurrTenant(String message);

    /**
     * 向当前租户下，指定服务下的所有实例发消息
     * @param serviceName
     * @param message
     */
    void send2AllInstancesOfTheService(String serviceName, String message);

    /**
     * 向当前租户下，当前服务下的所有实例发消息
     * @param message
     */
    void send2AllInstancesOfCurrService(String message);

    /**
     * 向当前租户下的指定实例发消息
     * @param instanceId
     * @param message
     */
    void send2TheInstance(String instanceId, String message);

    /**
     * 向当前租户下，指定服务下的某个实例发消息，如这个服务是集群的(有多个实例)，则只会有一个实例接收到消息。
     * @param serviceName
     * @param message
     */
    void send2OneInstancesOfTheService(String serviceName, String message);
}
