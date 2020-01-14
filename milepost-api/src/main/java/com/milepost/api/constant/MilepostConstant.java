package com.milepost.api.constant;

/**
 * Created by Ruifu Hua on 2019/12/21.
 * 框架所有的常量
 */
public class MilepostConstant {
    public static final String MILEPOST_VERSION = "1.0.0";
    public static final String APPLICATION_VERSION = "1.0.0.100";

    //应用类型，见com.milepost.api.enums.MilepostApplicationType
    public static final String MILEPOST_APPLICATION_TYPE_KEY ="eureka.instance.metadata-map.milepost-type";

    //下面两个要抽取到配置文件中
    //分布式同步锁重试获取所的间隔时间，单位ms，
//    public static final Long SYNCHRONIZED_LOCK_RETRY_INTERVAL = 1*1000L;//单位ms
//    //每一个实例能占有锁的最长时间，单位ms，
//    public static final int SYNCHRONIZED_LOCK_HOLD_DURATION = 45;//单位s

}
