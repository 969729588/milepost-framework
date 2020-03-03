package com.milepost.core.lock;

import com.milepost.api.enums.InstanceRole;
import com.milepost.api.util.RedisUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCommands;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 实例角色
 * 为了实现集群部署一个应用的多个实例的身份，
 * 一个应用的多个实例中，只有一个master，
 * 应用启动成功之后，把自己的身份(master/slave)放到redis中，
 * 之后通过保持心跳和抢占master的方式运行下去
 */
@Component
public class InstanceRoleHandleRunner implements ApplicationRunner {

    private static Logger logger = LoggerFactory.getLogger(InstanceRoleHandleRunner.class);

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用于获取当前应用的appName和instance-id
     * appName:spring.application.name
     * instance-id:eureka.instance.instance-id
     */
    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private InstanceRoleService instanceRoleService;

    @Autowired
    private SchedulerLockProperties schedulerLockProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(schedulerLockProperties.isEnabled()){
            logger.info("分布式锁、分布式任务调度初始化...");
        }else{
            logger.info("分布式锁、分布式任务调度未开启...");
            return;
        }

        //因为RedisTemplate不支持setnx命令，所以要使用JedisCommands，
        //当配置redis单机和哨兵时，JedisCommands实际上是Jedis，
        //当配置redis集群时，JedisCommands实际上是JedisCluster，
        JedisCommands jedisCommands = null;

        try {
            jedisCommands = RedisUtil.getJedisCommands(redisTemplate);

            //获取appName和instanceId
            InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
            //这里最好不要使用@Value读取配置文件，因为在不配置这个属性时，@Value注解报错，而这个方法返回UNKNOWN
            String currAppName = instanceInfo.getAppName();//注意，无论配置如何，这里都会获取到大写的，所以在DiscoveryClient中获取到小写的需要转换成大写的
            String currInstanceId = instanceInfo.getInstanceId();

            //先获取redis中master的实例id，注意，这里的值是在应用启动之初获取的，不能用在timer中的循环中，因为master是浮动的，所以timer的循环中应该重新获取
            String redisInstanceId = jedisCommands.get(currAppName);

            Long setnx = jedisCommands.setnx(currAppName, currInstanceId);//返回1表示设置成功，
            if (setnx == 1) {
                //设置成功，设置过期时间
                jedisCommands.expire(currAppName, schedulerLockProperties.getHeartbeatExpirationDurationInSeconds());
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色初始化成功，实例角色=" + InstanceRole.MASTER.getValue() + "。");
            } else if (setnx == 0 && currInstanceId.equals(redisInstanceId)) {
                //设置失败，但是redis中的master的实例id == 当前应用的实例id，(快速重启master就会发生这种情况)，只更新过期时间
                jedisCommands.expire(currAppName, schedulerLockProperties.getHeartbeatExpirationDurationInSeconds());
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，这个实例在redis中已经被标记为" + InstanceRole.MASTER.getValue() + "，此处只更新过期时间。");
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色初始化成功，实例角色=" + InstanceRole.MASTER.getValue() + "。");
            } else {
                //设置失败，并且redis中的master的实例id != 当前应用的实例id，(已经有运行良好的master了)，
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色初始化成功，实例角色=" + InstanceRole.SLAVE.getValue() + "，" + InstanceRole.MASTER.getValue() + "的实例ID=" + redisInstanceId + "。");
            }

            //如果是当前是master，则保持自己的心跳，如果是slave，则尝试抢占master

            //这里必须传入RedisTemplate，方法内部的定时器中每次循环都要从redisTemplate中获取Jedis，并且使用后要关闭，
            //因为哨兵模式中，redisTemplate一直连接这master(受哨兵控制，是浮动的)，每获取一个Jedis都执行当时的master，
            //当master变化后，如果不重新获取Jedis，就会连不上。
            keepHeartbeatAndGrabMaster(currAppName, currInstanceId, redisTemplate);

        }finally {
            //关闭JedisCommands
            RedisUtil.closeJedisCommandsQuietly(jedisCommands);
        }
    }

    /**
     * 如果当前实例是master，则保持自己的心跳，间隔touchHeartbeatIntervalInSeconds(s)，超过heartbeatExpirationDurationInSeconds(s)心跳失效，所以touchHeartbeatIntervalInSeconds一定要 小于 heartbeatExpirationDurationInSeconds，这正好符合这两个参数的原本含义。
     * 如果当前实例是slave，则尝试抢占master，间隔touchHeartbeatIntervalInSeconds(s)，发现redis中不存在master时，立刻抢占，将自己设置为master
     *
     * @param currAppName    当前应用名称
     * @param currInstanceId 当前实例id
     */
    private void keepHeartbeatAndGrabMaster(String currAppName, String currInstanceId, RedisTemplate redisTemplate) {
        logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，开始维护心跳和抢占" + InstanceRole.MASTER.getValue() + "，" +
                "每" + schedulerLockProperties.getHeartbeatExpirationDurationInSeconds() + "秒更新一次心跳，抢占一次" + InstanceRole.MASTER.getValue() + "，心跳失效时间为" +
                schedulerLockProperties.getHeartbeatExpirationDurationInSeconds() + "秒。");

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            //run方法中如果发生异常，就会停止循环，所以使用try-catch
            @Override
            public void run() {

                JedisCommands jedisCommands = null;

                try {
                    jedisCommands = RedisUtil.getJedisCommands(redisTemplate);
                    //timer的循环中每次都要重新获取
                    String redisInstanceId = jedisCommands.get(currAppName);

                    if (instanceRoleService.isMaster()) {
                        //维护心跳
                        Long expire = jedisCommands.expire(currAppName, schedulerLockProperties.getHeartbeatExpirationDurationInSeconds());
                        if (expire == 1) {
                            logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色=" + InstanceRole.MASTER.getValue() + "，更新心跳成功。");
                        } else {
                            logger.error("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色=" + InstanceRole.MASTER.getValue() + "，更新心跳失败，可能会失去" + InstanceRole.MASTER.getValue() + "角色。");
                        }
                    } else {
                        //抢占master
                        Long setnx = jedisCommands.setnx(currAppName, currInstanceId);
                        if (setnx == 1) {
                            //抢占成功，设置过期时间
                            jedisCommands.expire(currAppName, schedulerLockProperties.getHeartbeatExpirationDurationInSeconds());
                            logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例抢占" + InstanceRole.MASTER.getValue() + "成功。");
                        } else if (setnx == 0) {
                            //抢占失败
                            logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例抢占" + InstanceRole.MASTER.getValue() + "失败，当前" + InstanceRole.MASTER.getValue() + "是" + redisInstanceId + "，运行状态良好。");
                        }
                    }
                }catch (Exception e){
                    //必须捕获异常，否则当发生异常后就不再循环了，比如连接redis超时就会发生异常
                    logger.error(e.getMessage(), e);
                }finally {
                    //关闭JedisCommands
                    RedisUtil.closeJedisCommandsQuietly(jedisCommands);
                }
            }
        }, 1000, schedulerLockProperties.getTouchHeartbeatIntervalInSeconds() * 1000);
    }
}
