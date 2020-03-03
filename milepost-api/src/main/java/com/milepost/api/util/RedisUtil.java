package com.milepost.api.util;

import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

/**
 * Created by Ruifu Hua on 2020/3/3.
 */
public class RedisUtil {
    /**
     * 关闭JedisCommands
     * @param jedisCommands
     */
    public static void closeJedisCommandsQuietly(JedisCommands jedisCommands) {
        if(jedisCommands!=null){
            if(jedisCommands instanceof Jedis){
                ((Jedis) jedisCommands).close();
            }else if(jedisCommands instanceof JedisCluster){
                //JedisCluster不需要关闭，从RedisTemplate中多次获取JedisCluster，都会得到同一个JedisCluster对象。
            }
        }
    }

    /**
     * 获取JedisCommands，支持单机、哨兵、集群
     * @param redisTemplate
     * @return
     */
    public static JedisCommands getJedisCommands(RedisTemplate redisTemplate){
        JedisCommands jedisCommands = (JedisCommands) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
        return jedisCommands;
    }

    /**
     * 获取Jedis，支持单机、哨兵
     * @param redisTemplate
     * @return
     */
    public static Jedis getJedis(RedisTemplate redisTemplate){
        JedisCommands jedisCommands = getJedisCommands(redisTemplate);
        return (Jedis) jedisCommands;
    }

    /**
     * 获取Jedis，支持集群
     * @param redisTemplate
     * @return
     */
    public static JedisCluster getJedisCluster(RedisTemplate redisTemplate){
        JedisCommands jedisCommands = getJedisCommands(redisTemplate);
        return (JedisCluster) jedisCommands;
    }
}
