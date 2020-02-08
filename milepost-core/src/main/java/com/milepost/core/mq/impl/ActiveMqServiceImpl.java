package com.milepost.core.mq.impl;

import com.milepost.core.mq.ActiveMqService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import javax.jms.Topic;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/2/8.
 * ActiveMq 服务实现类
 */
@Service
public class ActiveMqServiceImpl implements ActiveMqService{

    /**
     * 存放queue、topic对象
     */
    private static Map<String, Queue> queueMap = new HashMap<>();
    private static Map<String, Topic> topicMap = new HashMap<>();

    /**
     * 注入springboot自动配置的jmsTemplate
     */
    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendQueue(Queue queue, String message) {
        jmsTemplate.convertAndSend(queue, message);
    }

    @Override
    public void sendTopic(Topic topic, String message) {
        jmsTemplate.convertAndSend(topic, message);
    }

    @Override
    public String receiveQueue(Queue queue) {
        return (String) jmsTemplate.receiveAndConvert(queue);
    }

    @Override
    public String receiveTopic(Topic topic) {
        return (String) jmsTemplate.receiveAndConvert(topic);
    }

    @Override
    public Queue getQueue(String queueName) {
        if(!queueMap.containsKey(queueName)){
            ActiveMQQueue queue = new ActiveMQQueue(queueName);
            queueMap.put(queueName, queue);
        }
        return queueMap.get(queueName);
    }

    @Override
    public Topic getTopic(String topicName) {
        if(!topicMap.containsKey(topicName)){
            ActiveMQTopic topic = new ActiveMQTopic(topicName);
            topicMap.put(topicName, topic);
        }
        return topicMap.get(topicName);
    }
}
