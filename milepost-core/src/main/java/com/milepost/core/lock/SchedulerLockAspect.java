package com.milepost.core.lock;

import com.milepost.api.enums.InstanceRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by Ruifu Hua on 2019/12/23.<br>
 * 分布式调度锁 切面，拦截所有标注了@SchedulerLock注解的方法。<br>
 */
@Aspect
@Component
public class SchedulerLockAspect {

    private static Logger logger = LoggerFactory.getLogger(SchedulerLockAspect.class);

    @Autowired
    private InstanceRoleService instanceRoleService;

    @Autowired
    private SchedulerLockProperties schedulerLockProperties;


    @Pointcut("@annotation(com.milepost.core.lock.SchedulerLock)")
    public void advice() {
    }

    /**
     * 环绕通知需要携带 ProceedingJoinPoint 类型的参数。
     * 环绕通知类似于动态代理的全过程：ProceedingJoinPoint 类型的参数可以决定是否执行目标方法。
     * 环绕通知必须有返回值，返回值即为目标方法的返回值
     */
    @Around("advice()")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;

        if(!schedulerLockProperties.isEnabled()){
            //未开启
            logger.error("scheduler-lock.enabled=false，即关闭分布式调度锁功能，此时@SynchronizedLock注解无效，请关注。");
            try {
                result = proceedingJoinPoint.proceed();
            }catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            return result;
        }else{
            //开启

            //参数
            Object[] args = proceedingJoinPoint.getArgs();
            //方法签名
            MethodSignature methodSignature = (MethodSignature)proceedingJoinPoint.getSignature();
            //方法
            Method method = methodSignature.getMethod();
            //方法名
            String methodName = method.getName();
            //注解
            SchedulerLock schedulerLock = method.getAnnotation(SchedulerLock.class);

            try {

                //前置通知
                // ...
                switch(schedulerLock.model()){
                    case slave:
                        if(!instanceRoleService.isMaster()){
                            //执行方法
                            result = proceedingJoinPoint.proceed();
                        }else{
                            logger.info("调度锁模式="+ InstanceRole.SLAVE.getValue() +"，当前实例是" + InstanceRole.MASTER.getValue() +"，忽略本次调度。");
                        }
                        break;
                    case master:
                        if(instanceRoleService.isMaster()){
                            //执行方法
                            result = proceedingJoinPoint.proceed();
                        }else{
                            logger.info("调度锁模式="+ InstanceRole.MASTER.getValue() +"，当前实例是" + InstanceRole.SLAVE.getValue() +"，忽略本次调度。");
                        }
                        break;
                }

                //返回通知
                // ...

            } catch (Throwable e) {
                //异常通知
                logger.error(e.getMessage(), e);
            }finally {
                //后置通知
                // ...
            }
            //返回结果
            return result;
        }
    }
}
