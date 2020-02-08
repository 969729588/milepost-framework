package com.milepost.core.mq;

import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Created by Ruifu Hua on 2020/2/8.
 * ActiveMq 服务
 */
public interface ActiveMqService {
    /**
     * 发送queue消息
     * @param queue
     * @param message
     */
    void sendQueue(Queue queue, String message);

    /**
     * 发送topic消息
     * @param topic
     * @param message
     */
    void sendTopic(Topic topic, String message);

    /**
     * 收取queue消息，当没有消息时此方法会阻塞，直到有消息时才能收取到，每次收取一条消息
     * @param queue
     * @return
     */
    String receiveQueue(Queue queue);

    /**
     * 收取topic消息，当没有消息时此方法会阻塞，直到有消息时才能收取到，每次收取一条消息
     * @param topic
     * @return
     */
    String receiveTopic(Topic topic);

    /**
     * 获取queue，不需要每次发送/收取消息时都实例化一个queue，使用此方法可以避免实例化多个同名称的queue
     * @param queueName
     * @return
     */
    Queue getQueue(String queueName);

    /**
     * 获取topic，不需要每次发送/收取消息时都实例化一个topic，使用此方法可以避免实例化多个同名称的topic
     * @param topicName
     * @return
     */
    Topic getTopic(String topicName);
}
