package com.milepost.core.lock;

import brave.Span;
import brave.Tracer;
import com.milepost.api.enums.InstanceRole;
import com.milepost.api.util.RedisUtil;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisCommands;

/**
 * 应用启动之后，维护实例角色，目前只有两种（master/slave），<br>
 * 一个服务的多个实例中，只有一个是master，其他都是slave），<br>
 * 把一个服务的多个实例中的master的实例id（instanceId）放到redis中，key是服务名称（appName）。<br>
 * 应用启动成功之后，把自己的身份(master/slave)放到redis中，<br>
 * 之后通过保持心跳和抢占master的方式运行下去，<br>
 * 此功能是分布式调度锁的基础。<br>
 */
@Component
@ConditionalOnExpression("#{!'false'.equals(environment['scheduler-lock.enabled'])}")
public class InstanceRoleHandleScheduler {

    private static Logger logger = LoggerFactory.getLogger(InstanceRoleHandleScheduler.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private Tracer tracer;

    /**
     * 用于获取当前应用的appName和instanceId
     * appName:spring.application.name
     * instanceId:eureka.instance.instance-id
     */
    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private InstanceRoleService instanceRoleService;

    @Autowired
    private SchedulerLockProperties schedulerLockProperties;

    @Autowired
    private ThreadPoolProperties threadPoolProperties;

    /**
     * 是否已经成功初始化了角色
     */
    private Boolean roleInitSuccess = false;

    /**
     * 当前实例
     */
    private InstanceInfo instanceInfo = null;

    /**
     * 使用异步定时任务维护实例角色，<br>
     * 每${scheduler-lock.touch-heartbeat-interval-in-milliseconds}执行一次<br>
     * fixedDelay：从上一个任务完成到下一个任务开始的间隔，单位为ms，这能保证不会发生多个任务在时间上重叠的情况，
     *      但是由于本方法标注了@Async，是用线程池中的线程来执行异步执行此方法，所以每次定时任务相当于异步执行的，
     *      会发生任务重叠，但是但没什么影响。
     * @throws Exception
     */
    @Scheduled(fixedDelayString = "${scheduler-lock.touch-heartbeat-interval-in-milliseconds}")
    @Async
    public void run() throws Exception {

        //初始化instanceInfo
        if(this.instanceInfo == null){
            //获取appName和instanceId
            this.instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
        }

        //初始化角色，
        if(!this.roleInitSuccess){
            initRole();
        }

        //如果是当前是master，则保持自己的心跳，如果是slave，则尝试抢占master
        //这里必须传入RedisTemplate，方法内部的定时器中每次循环都要从redisTemplate中获取Jedis，并且使用后要关闭，
        //因为哨兵模式中，redisTemplate一直连接这master(受哨兵控制，是浮动的)，每获取一个Jedis都执行当时的master，
        //当master变化后，如果不重新获取Jedis，就会连不上。
        keepHeartbeatAndGrabMaster();
    }

    /**
     * 初始化角色，
     */
    private void initRole() {

        logger.info("分布式调度锁初始化...");

        //这里最好不要使用@Value读取配置文件，因为在不配置这个属性时，@Value注解报错，而这个方法返回UNKNOWN
        String currAppName = this.instanceInfo.getAppName();//注意，无论配置如何，这里都会获取到大写的，所以在DiscoveryClient中获取到小写的需要转换成大写的
        String currInstanceId = this.instanceInfo.getInstanceId();

        //因为RedisTemplate不支持setnx命令，所以要使用JedisCommands，
        //当配置redis单机和哨兵时，JedisCommands实际上是Jedis，
        //当配置redis集群时，JedisCommands实际上是JedisCluster，
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = RedisUtil.getJedisCommands(redisTemplate);
            //先获取redis中master的实例id，注意，这里的值必须在应用启动之初获取的，不能传入到timer中的循环中，因为master是浮动的，所以timer的循环中应该重新获取
            String masterInstanceId = jedisCommands.get(currAppName);

            Long setnx = jedisCommands.setnx(currAppName, currInstanceId);//返回1表示设置成功，
            if (setnx == 1) {
                //设置成功，设置过期时间
                jedisCommands.expire(currAppName, schedulerLockProperties.getHeartbeatExpirationDurationInSeconds());
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色初始化成功，实例角色=" + InstanceRole.MASTER.getValue() + "。");
            } else if (setnx == 0 && currInstanceId.equals(masterInstanceId)) {
                //设置失败，但是redis中的master的实例id == 当前应用的实例id，(快速重启master就会发生这种情况)，只更新过期时间
                jedisCommands.expire(currAppName, schedulerLockProperties.getHeartbeatExpirationDurationInSeconds());
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，这个实例在redis中已经被标记为" + InstanceRole.MASTER.getValue() + "，此处只更新过期时间。");
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色初始化成功，实例角色=" + InstanceRole.MASTER.getValue() + "。");
            } else {
                //设置失败，并且redis中的master的实例id != 当前应用的实例id，(已经有运行良好的master了)，
                logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，实例角色初始化成功，实例角色=" + InstanceRole.SLAVE.getValue() + "，" + InstanceRole.MASTER.getValue() + "的实例ID=" + masterInstanceId + "。");
            }

            this.roleInitSuccess = true;
        }finally {
            //关闭JedisCommands
            RedisUtil.closeJedisCommandsQuietly(jedisCommands);
        }
    }

    /**
     * 如果当前实例是master，则保持自己的心跳，间隔touchHeartbeatIntervalInSeconds(s)，超过heartbeatExpirationDurationInSeconds(s)心跳失效，所以touchHeartbeatIntervalInSeconds一定要 小于 heartbeatExpirationDurationInSeconds。
     * 如果当前实例是slave，则尝试抢占master，间隔touchHeartbeatIntervalInSeconds(s)，在任何一次尝试中如发现redis中不存在master，则立刻抢占，将自己设置为master。
     */
    private void keepHeartbeatAndGrabMaster() {
        //打印线程池信息
        ThreadPoolTaskExecutorConfig.printThreadPoolInfo();
        logger.info("维护实例角色，线程名=" + Thread.currentThread().getName() +"。");

        //这里最好不要使用@Value读取配置文件，因为在不配置这个属性时，@Value注解报错，而这个方法返回UNKNOWN
        String currAppName = this.instanceInfo.getAppName();//注意，无论配置如何，这里都会获取到大写的，所以在DiscoveryClient中获取到小写的需要转换成大写的
        String currInstanceId = this.instanceInfo.getInstanceId();
        String currTenant = this.instanceInfo.getMetadata().get("tenant");

        logger.info("服务名称=" + currAppName + "，实例ID=" + currInstanceId + "，开始维护心跳和抢占" + InstanceRole.MASTER.getValue() + "，" +
                "每" + schedulerLockProperties.getHeartbeatExpirationDurationInSeconds() + "秒更新一次心跳，抢占一次" + InstanceRole.MASTER.getValue() + "，心跳失效时间为" +
                schedulerLockProperties.getHeartbeatExpirationDurationInSeconds() + "秒。");

        //链路跟踪标签
        if(tracer != null){
            Span currentSpan = tracer.currentSpan();
            currentSpan.tag("instanceId", currInstanceId);
            currentSpan.tag("tenant", currTenant);
        }

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
}
