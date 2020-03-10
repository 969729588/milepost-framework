package com.milepost.ui.config.openfeign;


import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Ruifu Hua on 2020/1/20.
 * FeignClient拦截器，将token放入请求头中，不需要在每个FeignClient方法中都写一个从请求头中接收token的入参。
 */
@Component
public class FeignClientInterceptor implements RequestInterceptor {

    /**
     * token放在请求头.优先使用请求头中的token
     *
     * @param requestTemplate 请求参数
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String tokenHeader = request.getHeader("Authorization");
            String tokenParameter = request.getParameter("access_token");
            if(StringUtils.isNotBlank(tokenHeader)){
                requestTemplate.header("Authorization", new String[]{tokenHeader});
            }else if(StringUtils.isNotBlank(tokenParameter)){
                requestTemplate.header("Authorization", new String[]{"Bearer " + tokenParameter});
            }
        }
    }
}