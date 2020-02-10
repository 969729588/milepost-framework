package com.milepost.api.util;

import org.springframework.beans.BeanUtils;

/**
 * 操作javabean的工具类
 * 基于org.springframework.beans.BeanUtils中的类
 *
 * Created by Ruifu Hua on 2019/7/25.
 */
public class JavaBeanUtil {

    /**
     * Copy the property values of the given source bean into the given target bean,
     * ignoring the given "ignoreProperties".
     * @param source
     * @param target
     * @param ignoreProperties
     */
    public static void copyBean(Object source, Object target, String... ignoreProperties){
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }
}
