package com.milepost.service.config.openfeign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ruifu Hua on 2020/1/20.
 * FeignClient拦截器，将token放入请求头中，不需要在每个FeignClient方法中都写一个从请求头中接收token的入参。<br>
 *
 * 实现ServletRequestListener接口的原因：
 * 由于使用了Hystrix，资源隔离级别为默认的线程池隔离级别，导致执行apply(RequestTemplate requestTemplate)方法
 * 的线程已经不是从前端请求过来的tomcat线程了，进而导致下面两行无法获取到数据
 * RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
 * HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
 * 即所以无法从requestTemplate中获取前端传入的token。
 * 所以使用ServletRequestListener来暴露request，进而获取其中的token。
 * SpringCloud官方文档也有说明：https://cloud.spring.io/spring-cloud-openfeign/2.1.x/single/spring-cloud-openfeign.html
 * [If you need to use ThreadLocal bound variables in your RequestInterceptor`s you will need to
 * either set the thread isolation strategy for Hystrix to `SEMAPHORE or disable Hystrix in Feign.]
 *
     # To disable Hystrix in Feign
     feign:
        hystrix:
            enabled: false

     # To set thread isolation to SEMAPHORE
     hystrix:
        command:
            default:
                execution:
                    isolation:
                        strategy: SEMAPHORE

 */
@Component
public class BearerTokenRequestInterceptor implements ServletRequestListener, RequestInterceptor {

    private final static String AUTHORIZATION = "Authorization";

    private final static String ACCESS_TOKEN = "access_token";

    //Spring的Bean是单例的，多个请求可以同时进入这个类中的方法，所以这里要使用ThreadLocal变量，否则会发生线程安全问题，<br>
    //但是ThreadLocal只能在同一个线程中共享数据，所以要使用InheritableThreadLocal，这个能在父子线程中共享数据，
    private ThreadLocal<String> AUTHORIZATION_VALUE_FROM_HTTP_SERVLET_REQUEST = new InheritableThreadLocal<>();

    /**
     * 获取HttpServletRequest中的token，放到本地线程变量中
     *
     * @param requestEvent
     */
    @Override
    public void requestInitialized(ServletRequestEvent requestEvent) {
        if (!(requestEvent.getServletRequest() instanceof HttpServletRequest)) {
            throw new IllegalArgumentException(
                    "Request is not an HttpServletRequest: " + requestEvent.getServletRequest());
        }
        HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
        String authorizationValueFromHttpServletRequest = getAuthorizationValueFromHttpServletRequest(request);

        //System.out.println(Thread.currentThread().getName() + "，requestInitialized得到的token" + formatToken(authorizationValueFromHttpServletRequest));

        this.AUTHORIZATION_VALUE_FROM_HTTP_SERVLET_REQUEST.set(authorizationValueFromHttpServletRequest);
    }

    /**
     * 截取后10位，方便调试时候观察比较，
     *
     * @param authorizationValueFromHttpServletRequest
     * @return
     */
    private String formatToken(String authorizationValueFromHttpServletRequest) {
        String temp = null;
        if (StringUtils.isNotBlank(authorizationValueFromHttpServletRequest)) {
            temp = authorizationValueFromHttpServletRequest.substring(authorizationValueFromHttpServletRequest.length() - 10);
        } else {
            temp = authorizationValueFromHttpServletRequest;
        }
        return temp;
    }

    /**
     * 获取HttpServletRequest中的 Authorization 值(以“Bearer ”开头的)，<br>
     * 优先使用请求头中的，其次使用参数中的，如果都没有则返回null。
     *
     * @param request
     * @return
     */
    private String getAuthorizationValueFromHttpServletRequest(HttpServletRequest request) {

        String authorizationHeaderValue = request.getHeader(AUTHORIZATION);
        if (StringUtils.isNotBlank(authorizationHeaderValue)) {
            return authorizationHeaderValue;
        }

        String tokenParameter = request.getParameter(ACCESS_TOKEN);
        if (StringUtils.isNotBlank(tokenParameter)) {
            return "Bearer " + tokenParameter;
        }

        //如果都没有则返回null
        return null;
    }

    /**
     * 移除本地线程变量中的数据
     *
     * @param requestEvent
     */
    @Override
    public void requestDestroyed(ServletRequestEvent requestEvent) {
        this.AUTHORIZATION_VALUE_FROM_HTTP_SERVLET_REQUEST.remove();
    }

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
        if (requestTemplateHasToken(requestTemplate)) {
            return;
        }
        String authorizationValueFromHttpServletRequest = this.AUTHORIZATION_VALUE_FROM_HTTP_SERVLET_REQUEST.get();

        //System.out.println(Thread.currentThread().getName() + "，apply得到的token" + formatToken(authorizationValueFromHttpServletRequest));

        if (StringUtils.isNotBlank(authorizationValueFromHttpServletRequest)) {
            requestTemplate.header(AUTHORIZATION, new String[]{authorizationValueFromHttpServletRequest});
        }
    }

    /**
     * 如果requestTemplate中有token，则返回true，否则返回false
     *
     * @param requestTemplate
     * @return
     */
    private boolean requestTemplateHasToken(RequestTemplate requestTemplate) {
        Map<String, Collection<String>> headers = requestTemplate.headers();
        Set<String> headerNames = headers.keySet();
        if (headerNames.contains(AUTHORIZATION)) {
            return true;
        } else {
            return false;
        }
    }
}