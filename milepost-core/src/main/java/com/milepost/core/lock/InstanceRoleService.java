package com.milepost.core.lock;

import com.milepost.api.util.RedisUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;

/**
 * Created by Ruifu Hua on 2019/12/24.<br>
 *
 * 实例角色服务，获取一个实例的角色是master还是slave。<br>
 * {@link InstanceRoleHandleRunner} 中已经把一个服务的多个实例中的master的实例id（instanceId）放到了redis中，key是服务名称（appName）。
 */
@Service
public class InstanceRoleService {

    private static Logger logger = LoggerFactory.getLogger(InstanceRoleService.class);

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用于获取当前应用的appName和instanceId
     * appName:spring.application.name
     * instanceId:eureka.instance.instance-id
     */
    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private SchedulerLockProperties schedulerLockProperties;

    /**
     * 当前实例是否是master，是则返回ture，否则返回false
     *
     * @return 返回值见MilepostConstant
     */
    public Boolean isMaster(){
        InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
        //这里最好不要使用@Value读取配置文件，因为在不配置这个属性时，@Value注解报错，而这个方法返回UNKNOWN
        String appName = instanceInfo.getAppName();
        String instanceId = instanceInfo.getInstanceId();
        return isMaster(appName, instanceId);
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

            if(!schedulerLockProperties.isEnabled()){
                //未开启
                logger.error("scheduler-lock.enabled=false，即关闭分布式调度锁功能，此时InstanceRoleService注解无效，所有实例都会被认为是slave，请关注。");
            }

            jedisCommands = RedisUtil.getJedisCommands(redisTemplate);
            //从redis中获取当前实例的角色
            String masterInstanceId = jedisCommands.get(appName);
            if(instanceId!=null && instanceId.equals(masterInstanceId)){
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
