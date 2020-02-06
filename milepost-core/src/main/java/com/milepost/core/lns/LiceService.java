package com.milepost.core.lns;

/**
 * Created by Ruifu Hua on 2020/2/3.
 */

import java.lang.annotation.*;

/**
 * 标注此注解的方法，会检测lns是否过期
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LiceService {

}
