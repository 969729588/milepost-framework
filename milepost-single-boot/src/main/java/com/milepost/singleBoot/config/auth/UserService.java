package com.milepost.singleBoot.config.auth;

import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Created by Ruifu Hua on 2020-05-21.
 * 认证相关，用来获取用户信息的，子模块必须提供这个接口的实现类并注入到SpringIOC容器中，
 */
public interface UserService extends UserDetailsService {

}
