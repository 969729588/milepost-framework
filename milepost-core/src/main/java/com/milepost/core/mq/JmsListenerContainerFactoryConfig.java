package com.milepost.core.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import javax.jms.ConnectionFactory;

/**
 * Created by Ruifu Hua on 2020/2/8.
 * JmsListenerContainerFactory
 */
@Configuration
@ConditionalOnProperty("spring.activemq.broker-url")
public class JmsListenerContainerFactoryConfig {

    private static Logger logger = LoggerFactory.getLogger(JmsListenerContainerFactoryConfig.class);

    public JmsListenerContainerFactoryConfig() {
        logger.info("初始化消息总线服务JmsListenerContainerFactory...");
    }

    /**
     * Topic模式，
     * 在bean的方法上标
     * @ JmsListener(destination = "topic名称", containerFactory = "jmsListenerContainerFactoryTopic")
     * 注解即可监听topic，其中topic名称可以使用${xxx}来读取配置文件中的配置项
     * @param connectionFactory
     * @return
     */
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryTopic(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setPubSubDomain(true);//发布订阅模式
        bean.setConnectionFactory(connectionFactory);
        return bean;
    }

    /**
     * Queue模式，
     * 在bean的方法上标
     * @ JmsListener(destination = "queue名称", containerFactory = "jmsListenerContainerFactoryQueue")
     * 注解即可监听queue，其中queue名称可以使用${xxx}来读取配置文件中的配置项
     * @param connectionFactory
     * @return
     */
    @Bean
    public JmsListenerContainerFactory<?> jmsListenerContainerFactoryQueue(ConnectionFactory  connectionFactory) {
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setConnectionFactory(connectionFactory);
        return bean;
    }
}
