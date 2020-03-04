package com.milepost.core.lock;

/**
 * Created by Ruifu Hua on 2019/12/23.
 * 分布式调度锁 运行模式，
 */
public enum SchedulerLockModel {
    slave,//slave
    master;//master

    SchedulerLockModel() {
    }
}
