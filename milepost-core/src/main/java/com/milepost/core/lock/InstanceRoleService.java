package com.milepost.core.lock;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

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
        Jedis jedis = null;
        try {
            jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
            InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
            //这里最好不要使用@Value读取配置文件，因为在不配置这个属性时，@Value注解报错，而这个方法返回UNKNOWN
            String appName = instanceInfo.getAppName();
            String instanceId = instanceInfo.getInstanceId();

            //从redis中获取当前实例的角色
            String redisInstanceId = jedis.get(appName);
            if(instanceId!=null && instanceId.equals(redisInstanceId)){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }

    }

    /**
     * 指定应用的指定实例是否是master，是则返回ture，否则返回false
     * @param appName 应用名称
     * @param instanceId 实例名称
     * @return
     */
    public Boolean isMaster(String appName, String instanceId){
        Jedis jedis = null;
        try {
            jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
            //从redis中获取当前实例的角色
            String redisInstanceId = jedis.get(appName);
            if(instanceId!=null && instanceId.equals(redisInstanceId)){
                return Boolean.TRUE;
            }else{
                return Boolean.FALSE;
            }
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }



}
