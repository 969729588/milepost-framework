package com.milepost.core.redisTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Created by Ruifu Hua on 2020/2/29.
 */
public class RedisTemplateConfig {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 修改RedisTemplate的序列化方式，<br>
     * StringRedisTemplate 默认使用使用StringRedisSerializer来序列化，<br>
     * RedisTemplate 默认使用JdkSerializationRedisSerializer来序列化，<br>
     * 这里将RedisTemplate的序列化方式修改为StringSerializer，这样在使用redis-cli调试时候很方便，<br>
     * 注意在使用redis-cli连接时候，要在最后加“--rwa”参数，如 redis-cli --raw<br>
     * @param applicationReadyEvent
     */
    @EventListener
    public void setSerializer(ApplicationReadyEvent applicationReadyEvent){
        /**
         * StringRedisTemplate 默认使用使用StringRedisSerializer来序列化
         * RedisTemplate 默认使用JdkSerializationRedisSerializer来序列化
         */
        RedisSerializer stringSerializer = redisTemplate.getStringSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
    }
}
