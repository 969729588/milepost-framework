package com.milepost.core.lock;

import java.lang.annotation.*;

/**
 * Created by Ruifu Hua on 2019/12/23.<br>
 * 分布式调度锁，与@Scheduler一起使用，控制一个应用的多个实例的定时任务如何执行，<br>
 * 支持 只master执行 或者 所有slave执行 两种方式。<br>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SchedulerLock {
    SchedulerLockModel model() default SchedulerLockModel.master;
}
