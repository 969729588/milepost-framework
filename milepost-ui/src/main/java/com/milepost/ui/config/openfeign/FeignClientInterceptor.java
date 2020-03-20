package com.milepost.ui.config.openfeign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ruifu Hua on 2020/1/20.
 * FeignClient拦截器，将token放入请求头中，不需要在每个FeignClient方法中都写一个从请求头中接收token的入参。
 */
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final static String AUTHORIZATION = "Authorization";

    /**
     * token放在请求头.优先使用请求头中的token
     *
     * @param requestTemplate 请求参数
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //如果requestTemplate中有token，则不从HttpServletRequest中拿token，

        //requestTemplate中的token：FeignClient方法入参中的@RequestHeader(value = "Authorization") String authorization
        //HttpServletRequest中的token：前端请求传入的token，可以在请求头中，也可以在参数中

        //解决认证UI登录接口(com.milepost.authenticationUi.auth.controller.LoginController#login)中,
        //旧token过期后仍存在于前端缓存中，重新登录时获取到新token之后请求认证Service时，次此处无法识别FeignClient方法入参中的token的问题。
        if(requestTemplateHasToken(requestTemplate)){
           return;
        }

        //requestTemplate中没有token时，从HttpServletRequest中获取
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();

        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String tokenHeader = request.getHeader(AUTHORIZATION);
        String tokenParameter = request.getParameter("access_token");
        if(StringUtils.isNotBlank(tokenHeader)){
            requestTemplate.header(AUTHORIZATION, new String[]{tokenHeader});
        }else if(StringUtils.isNotBlank(tokenParameter)){
            requestTemplate.header(AUTHORIZATION, new String[]{"Bearer " + tokenParameter});
        }
    }

    /**
     * 如果requestTemplate中有token，则返回true，否则返回false
     * @param requestTemplate
     * @return
     */
    private boolean requestTemplateHasToken(RequestTemplate requestTemplate) {
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Set<String> headerNames = headers.keySet();
        if(headerNames.contains(AUTHORIZATION)){
            return true;
        }else{
            return false;
        }
    }
}