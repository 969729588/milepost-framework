package com.milepost.auth.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
                //保留Oauth原始的机制
//                .exceptionHandling()
//                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
//            .and()
                .authorizeRequests()
                .antMatchers("/milepost-actuator/**").permitAll()
                .antMatchers("/**").authenticated()
            .and()
                .httpBasic();
    }

    @Autowired
    UserDetailsService userDetailsService;

    /**
     * 自定义的MD5加密方式，
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
//                .passwordEncoder(new BCryptPasswordEncoder());//此处控制对用户密码的加密方式，此处为BCryptPasswordEncoder，
                .passwordEncoder(passwordEncoder);//此处控制对用户密码的加密方式，此处为自定义的MD5加密方式

    }
}
