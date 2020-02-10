package com.milepost.core.exception;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 发生异常时候，通过这个类定制返回的数据，包括页面上能获取到的数据和返回的json数据
 *
 * 整个程序的执行流程如下：
 * 1.异常点
 * 2.com.milepost.config.exception.GlobalExceptionHandler#handleException(java.lang.Exception, javax.servlet.http.HttpServletRequest) 的return之前
 * 3.com.milepost.config.exception.CustomErrorAttributes#getErrorAttributes(org.springframework.web.context.request.WebRequest, boolean)
 * 4.com.milepost.config.exception.GlobalExceptionHandler#handleException(java.lang.Exception, javax.servlet.http.HttpServletRequest) 的return
 */
@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
        //获取springboot默认的错误返回数据
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);

        //封装框架统一的response结构，即com.milepost.api.vo.response.Response
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("code", errorAttributes.get("status"));//springboot默认的响应状态码
        response.put("msg", errorAttributes.get("message"));//springboot默认的响应消息描述
        response.put("payload", errorAttributes);//springboot默认的所有响应数据
        return response;
    }

}
