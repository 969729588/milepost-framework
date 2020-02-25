package com.milepost.service.config.auth;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Ruifu Hua on 2020/1/20.
 * 资源服务器配置
 */
@RestController
@Api(description = "文本加密")

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private StringEncryptor encryptor;

    /**
     * 配置哪些url可以不登录访问，哪些url必须登录之后才能访问
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/milepost-actuator/**",//监控
                    "/swagger-resources/**", "/v2/**", "/swagger-ui.html/**", "/webjars/**",//swagger
                    "/enc"//文本加密

            ).permitAll()
            .antMatchers("/**").authenticated();
    }

    @GetMapping(value = {"/enc"})
    @ApiOperation(value = "文本加密")
    public String enc(@ApiParam(value = "明文",required = true) @RequestParam("value") String value) {
        return "ENC(" + encryptor.encrypt(value) + ")";
    }
}
