package com.milepost.api.util;

import org.apache.commons.lang.RandomStringUtils;

import java.util.UUID;

/**
 * Created by Ruifu Hua on 2020/1/16.
 */
public class DataUUIDUtil {

    /**
     * 生成指定长度的uuid
     * 通过org.apache.commons.lang.RandomStringUtils获取
     * @return
     */
    public static String randomUUID(int number){
        return RandomStringUtils.random(number,
                "abcdefghijklmnopqrstuvwxyz0123456789");
    }

    /**
     * 获得一个UUID
     * 通过jdk获取
     * @return String UUID
     */
    public static String randomUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

}
