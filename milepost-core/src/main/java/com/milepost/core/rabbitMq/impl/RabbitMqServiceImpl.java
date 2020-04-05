package com.milepost.core.rabbitMq.impl;

import com.milepost.core.rabbitMq.RabbitMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Created by Ruifu Hua on 2020/4/3.
 * RabbitMq服务实现类
 */
@Service
@ConditionalOnProperty("spring.rabbitmq.host")
public class RabbitMqServiceImpl implements RabbitMqService{

    private static Logger logger = LoggerFactory.getLogger(RabbitMqServiceImpl.class);

    @Autowired
    private Environment environment;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public RabbitMqServiceImpl() {
        logger.info("初始化RabbitMQ服务...");
    }

    /**
     * 向指定租户下的所有服务实例发消息
     * @param tenant
     * @param message
     */
    @Override
    public void send2AllInstancesOfTheTenant(String tenant, String message) {
        String exchange = "exchange."+ tenant +"_tenant";
        rabbitTemplate.convertAndSend(exchange, null, message);
    }

    /**
     * 向当前租户下的所有服务实例发消息
     * @param message
     */
    @Override
    public void send2AllInstancesOfCurrTenant(String message) {
        String tenant = environment.getProperty("multiple-tenant.tenant");
        String exchange = "exchange."+ tenant +"_tenant";
        rabbitTemplate.convertAndSend(exchange, null, message);
    }

    /**
     * 向当前租户下，指定服务下的所有实例发消息
     * @param serviceName
     * @param message
     */
    @Override
    public void send2AllInstancesOfTheService(String serviceName, String message) {
        String tenant = environment.getProperty("multiple-tenant.tenant");
        String exchange = "exchange."+ tenant +"."+ serviceName +"_service";
        rabbitTemplate.convertAndSend(exchange, null, message);
    }

    /**
     * 向当前租户下，当前服务下的所有实例发消息
     * @param message
     */
    @Override
    public void send2AllInstancesOfCurrService(String message) {
        String tenant = environment.getProperty("multiple-tenant.tenant");
        String serviceName = environment.getProperty("spring.application.name");
        String exchange = "exchange."+ tenant +"."+ serviceName +"_service";
        rabbitTemplate.convertAndSend(exchange, null, message);
    }

    /**
     * 向当前租户下的指定实例发消息
     * @param instanceId
     * @param message
     */
    @Override
    public void send2TheInstance(String instanceId, String message) {
        String tenant = environment.getProperty("multiple-tenant.tenant");
        String exchange = "exchange."+ tenant +"_instance";
        String routingKey = instanceId;
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    /**
     * 向当前租户下，指定服务下的某个实例发消息，如这个服务是集群的(有多个实例)，则只会有一个实例接收到消息。
     * @param serviceName
     * @param message
     */
    @Override
    public void send2OneInstancesOfTheService(String serviceName, String message) {
        String tenant = environment.getProperty("multiple-tenant.tenant");
        String exchange = "exchange."+ tenant +"."+ serviceName +"_randomInstance";
        String routingKey = "key.randomInstance";
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
