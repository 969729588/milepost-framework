package com.milepost.core.lock;

import java.lang.annotation.*;

/**
 * Created by Ruifu Hua on 2019/12/23.
 * 分布式调度锁，与@Scheduler一起使用，制一个应用的多个实例的定时任务如何执行，
 * 支持只有master执行或者所有slave执行两种方式。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SchedulerLock {
    LockModel model() default LockModel.master;
}
