package com.milepost.admin.config.auth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Ruifu Hua on 2020/1/29.
 * 调用认证服务（milepost-auth）
 */
@FeignClient(contextId = "authFc", name = "${info.app.auth-service.name}")
public interface AuthFc {

    /**
     * 获取token
     * @param authorization
     * @param grantType
     * @param username
     * @param password
     * @return
     */
    @PostMapping("${info.app.auth-service.prefix}/oauth/token")
    Jwt getToken(@RequestHeader(value = "Authorization") String authorization,
                 @RequestParam("grant_type") String grantType,
                 @RequestParam("username") String username,
                 @RequestParam("password") String password);

}
