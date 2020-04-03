package com.milepost.core.rabbitMq;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * Created by Ruifu Hua on 2020/4/3.
 * RabbitMq消息监听
 */
public interface RabbitMqListener {

    /**
     * 接收消息生产者  向EurekaServer中指定【租户】下的所有【服务】下的所有【实例】发消息。(send2AllInstancesOfCurrTenant)
     * 交换机类型：FANOUT
     * 交换机名称：exchange.${multiple-tenant.tenant}_tenant，每个租户消耗一个交换机。
     * 队列名称：queue.${eureka.instance.instance-id}_tenant，每个实例消耗一个队列。
     *
     * 对应
     * com.milepost.core.rabbitMq.RabbitMqService#send2AllInstancesOfTheTenant(java.lang.String, java.lang.String)
     * com.milepost.core.rabbitMq.RabbitMqService#send2AllInstancesOfCurrTenant(java.lang.String)
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.${eureka.instance.instance-id}_tenant", durable = "true"),
            exchange = @Exchange(value = "exchange.${multiple-tenant.tenant}_tenant", type = ExchangeTypes.FANOUT))
    )
    void receiveByTenant2AllInstances(String message);

    /**
     * 接收消息生产者  向EurekaServer中当前【租户】下的指定【服务】下的所有【实例】发消息。(send2AllInstancesOfTheService)
     * 交换机类型：FANOUT
     * 交换机名称：exchange.${multiple-tenant.tenant}.${spring.application.name}_service，每个租户下的每个服务消耗一个交换机。
     * 队列名称：queue.${eureka.instance.instance-id}_service，每个实例消耗一个队列。
     *
     * 对应
     * com.milepost.core.rabbitMq.RabbitMqService#send2AllInstancesOfTheService(java.lang.String, java.lang.String)
     * com.milepost.core.rabbitMq.RabbitMqService#send2AllInstancesOfCurrService(java.lang.String)
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.${eureka.instance.instance-id}_service", durable = "true"),
            exchange = @Exchange(value = "exchange.${multiple-tenant.tenant}.${spring.application.name}_service", type = ExchangeTypes.FANOUT))
    )
    void receiveByService2AllInstances(String message);

    /**
     * 接收消息生产者  向EurekaServer中当前【租户】下的指定【服务】下的指定【实例】发消息。(send2TheInstance)
     * 交换机类型：DIRECT
     * 交换机名称：exchange.${multiple-tenant.tenant}_instance，每个租户下的每个服务消耗一个交换机。
     * 队列名称：queue.${eureka.instance.instance-id}_instance，每个实例消耗一个队列。
     *
     * 对应
     * com.milepost.core.rabbitMq.RabbitMqService#send2TheInstance(java.lang.String, java.lang.String)
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.${eureka.instance.instance-id}_instance", durable = "true"),
            exchange = @Exchange(value = "exchange.${multiple-tenant.tenant}_instance", type = ExchangeTypes.DIRECT),
            key = "${eureka.instance.instance-id}")
    )
    void receiveByInstance(String message);

    /**
     * 接收消息生产者  向EurekaServer中当前【租户】下的指定【服务】下的某个(随机)【实例】发消息。(send2OneInstancesOfTheService)
     * 交换机类型：DIRECT
     * 交换机名称：exchange.${multiple-tenant.tenant}.${spring.application.name}_randomInstance，每个租户下的每个服务消耗一个交换机。
     * 队列名称：queue.${multiple-tenant.tenant}.${spring.application.name}_randomInstance，每个租户下的每个服务消耗一个队列。
     *
     * 对应
     * com.milepost.core.rabbitMq.RabbitMqService#send2OneInstancesOfTheService(java.lang.String, java.lang.String)
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "queue.${multiple-tenant.tenant}.${spring.application.name}_randomInstance", durable = "true"),
            exchange = @Exchange(value = "exchange.${multiple-tenant.tenant}.${spring.application.name}_randomInstance", type = ExchangeTypes.DIRECT),
            key = "key.randomInstance")
    )
    void receiveByService2OneInstances(String message);
}
