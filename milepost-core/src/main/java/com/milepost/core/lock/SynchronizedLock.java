package com.milepost.core.lock;

import java.lang.annotation.*;

/**
 * Created by Ruifu Hua on 2019/12/24.
 * 跨服务实例的synchronized，用在方法上，在一个应用的多个实例之间锁定这个方法，
 * 同一时刻只有一个服务的一个线程可进入该方法。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SynchronizedLock {

}
