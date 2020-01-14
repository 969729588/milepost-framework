package com.milepost.core.lock;

import com.milepost.api.constant.MilepostConstant;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;

/**
 * Created by Ruifu Hua on 2019/12/24.
 */
@Aspect
@Component
public class SynchronizedLockAspect {
    private static Logger logger = LoggerFactory.getLogger(SynchronizedLockAspect.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private EurekaClient eurekaClient;

    @Autowired
    private SchedulerLockProperties schedulerLockProperties;

    @Pointcut("@annotation(com.milepost.core.lock.SynchronizedLock)")
    public void advice() {
    }



    /**
     * 环绕通知需要携带 ProceedingJoinPoint 类型的参数。
     * 环绕通知类似于动态代理的全过程：ProceedingJoinPoint 类型的参数可以决定是否执行目标方法。
     * 环绕通知必须有返回值，返回值即为目标方法的返回值
     *
     * 这个方法上的synchronized实现实例内的同步锁
     */
    @Around("advice()")
    public synchronized Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint) {
        Jedis jedis = null;
        Object result = null;

        //传入目标方法的参数
        Object[] args = null;
        //目标方法签名
        MethodSignature methodSignature = null;
        //目标方法签名长字符串，能够标识唯一一个方法，用这个字符串作为redis的key。
        String msLongString = null;
        //目标方法
        Method method = null;
        //目标方法名称
        String methodName = null;
        //目标方法上的注解
        SynchronizedLock synchronizedLock = null;

        //获取当前实例信息
        InstanceInfo instanceInfo = null;
        String instanceId = null;

        //返回1表示设置成功，返回0表示失败
        Long setnx = 0L;

        String flag = null;
        try {
            //前置通知
//            System.out.println("SynchronizedLockAspect.aroundMethod--1" + ", flag=" + flag);

            jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
            args = proceedingJoinPoint.getArgs();
            methodSignature = (MethodSignature)proceedingJoinPoint.getSignature();
            msLongString = methodSignature.toLongString();
            method = methodSignature.getMethod();
//            methodName = method.getName();
//            synchronizedLock = method.getAnnotation(SynchronizedLock.class);
            instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();
            instanceId = instanceInfo.getInstanceId();
            flag = (String) args[0];

            //尝试获取锁
            do{
                setnx = jedis.setnx(msLongString, instanceId);
                if(setnx == 0){
                    //如果获取锁失败，则等候MilepostConstant.SYNCHRONIZEDLOCK_RETRY_INTERVAL重试
                    logger.info(instanceId +"实例获取"+ msLongString +"锁失败，实例"+ jedis.get(msLongString) +"正占有锁，等待"+ schedulerLockProperties.getSynchronizedLockRetryIntervalInMilliseconds() +"ms后重试。");
                    Thread.sleep(schedulerLockProperties.getSynchronizedLockRetryIntervalInMilliseconds());
                }else{
                    //获取锁成功后，设置过期时间，即设置占有锁的最长时间，防止死锁
                    jedis.expire(msLongString, schedulerLockProperties.getSynchronizedLockHoldDurationInSeconds());
                    logger.info(instanceId +"实例获取"+ msLongString +"锁成功。");
                }
            }while(setnx == 0);

            //执行目标方法
//            System.out.println("SynchronizedLockAspect.aroundMethod--2" + ", flag=" + flag);
            result = proceedingJoinPoint.proceed();
//            System.out.println("SynchronizedLockAspect.aroundMethod--3" + ", flag=" + flag);

            //返回通知
            // ...

        } catch (Throwable e) {
            //异常通知
//            System.out.println("SynchronizedLockAspect.aroundMethod--4" + ", flag=" + flag);
            logger.error(e.getMessage(), e);
//            System.out.println("SynchronizedLockAspect.aroundMethod--5" + ", flag=" + flag);
        }finally {
            try {
                //后置通知，无论目标方法正常返回还是遇到异常，后置通知都是要执行的，而且是最后执行的
                //释放锁
//                System.out.println("SynchronizedLockAspect.aroundMethod--6" + ", flag=" + flag);
                Long del = jedis.del(msLongString);
                if(del == 1){
                    logger.info(instanceId +"实例释放"+ msLongString +"锁成功。");
                }else{
                    logger.info(instanceId +"实例释放"+ msLongString +"锁失败。");
                    if(jedis.exists(msLongString)){
                        logger.error(instanceId +"实例释放"+ msLongString +"锁失败，锁依然存在于redis中。");
                    }else{
                        logger.error(instanceId +"实例释放"+ msLongString +"锁失败，锁已经不在redis中，可能由于持有锁时间超过最大持有锁时间("+ schedulerLockProperties.getSynchronizedLockHoldDurationInSeconds() +"秒)，锁被自动释放。");
                    }
                }
//                System.out.println("SynchronizedLockAspect.aroundMethod--7" + ", flag=" + flag);
            }finally {
                if(jedis != null){
                    jedis.close();
                }
            }
        }
        //返回结果
//        System.out.println("SynchronizedLockAspect.aroundMethod--8" + ", flag=" + flag);
        return result;
    }
}
