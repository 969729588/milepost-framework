package com.milepost.core.lock;

import com.milepost.api.util.RedisUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;

/**
 * Created by Ruifu Hua on 2019/12/24.
 */
@Service
public class InstanceRoleService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EurekaClient eurekaClient;

    /**
     * 当前实例是否是master，是则返回ture，否则返回false
     *
     * @return 返回值见MilepostConstant
     */
    public Boolean isMaster(){
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = RedisUtil.getJedisCommands(redisTemplate);
            InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
            //这里最好不要使用@Value读取配置文件，因为在不配置这个属性时，@Value注解报错，而这个方法返回UNKNOWN
            String appName = instanceInfo.getAppName();
            String instanceId = instanceInfo.getInstanceId();

            //从redis中获取当前实例的角色
            String redisInstanceId = jedisCommands.get(appName);
            if(instanceId!=null && instanceId.equals(redisInstanceId)){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }finally {
            //关闭JedisCommands
            RedisUtil.closeJedisCommandsQuietly(jedisCommands);
        }
    }

    /**
     * 指定应用的指定实例是否是master，是则返回ture，否则返回false
     * @param appName 应用名称
     * @param instanceId 实例名称
     * @return
     */
    public Boolean isMaster(String appName, String instanceId){
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = RedisUtil.getJedisCommands(redisTemplate);
            //从redis中获取当前实例的角色
            String redisInstanceId = jedisCommands.get(appName);
            if(instanceId!=null && instanceId.equals(redisInstanceId)){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }finally {
            //关闭JedisCommands
            RedisUtil.closeJedisCommandsQuietly(jedisCommands);
        }
    }
}
