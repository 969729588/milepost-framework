package com.milepost.ui.config.auth;

import com.milepost.api.util.ClassPathResourceUtil;
import com.milepost.api.util.JsonUtil;
import com.milepost.api.vo.response.Response;
import com.milepost.api.vo.response.ResponseHelper;
import com.netflix.appinfo.InstanceInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/1/20.
 * 资源服务器配置
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    /**
     * 用于在EurekaClient端获取注册到EurekaServer上的所有服务实例，
     */
    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private Environment environment;

    /**
     * 认证错误页路径
     */
    public static final String AUTH_ERROR_PAGE_PATH ="static/_error/auth_error.html";

    /**
     * 认证系统登录地址占位符
     */
    public static final String AUTH_LOGIN_URL_PLACEHOLDER = "auth_login_url";

    /**
     * 错误页面中的异常信息占位符
     */
    public static final String EXCEPTION_MESSAGE_PLACEHOLDER = "exception_message";

    /**
     * 认证服务UI服务名称
     */
    public static final String AUTHENTICATION_UI_APP_NAME = "authentication-ui";

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
            //增加了SpringBoot Admin后，开放了所有的端点，用Oauth保护所有端点，为了不影响EurekaServer，忽略以下端点的保护
            .antMatchers("/milepost-actuator/info", /*"/milepost-actuator/env", "/milepost-actuator/beans",*/ "/milepost-actuator/health", /*"/milepost-actuator/configprops", */"/milepost-actuator/hystrix.stream",//监控
                    //"/hystrix.stream/**", //hystrix监控，不使用每个服务独立的监控页面，
                    "/**/*.css", "/**/*.js", "/**/*.jpg", "/**/*.png", "/**/*.gif", "/**/*.ico", "/**/*.woff", "/**/*.ttf")//静态资源
                .permitAll()
            .antMatchers("/**").authenticated();
    }


    /**
     * 拦截非法访问，
     * @param resources
     * @throws Exception
     */
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.authenticationEntryPoint((request, response, authException) -> {
            //只要遇到异常，响应状态码就是401，
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            String authExceptionMessage = authException.getMessage();
            if(StringUtils.isNotBlank(authExceptionMessage) && authExceptionMessage.contains("Full authentication is required to access this resource")){
                authExceptionMessage = "error=\"unauthorized\", error_description=\""+ authExceptionMessage +"\"";
            }else{
                authExceptionMessage = authException.getCause().toString();
            }

            authExceptionMessage = "{"+ authExceptionMessage +"}";

            //根据请求头判断返回html页面还是返回json数据，
            String accept = request.getHeader("accept");
            if (StringUtils.isBlank(accept) ||
                    accept.equalsIgnoreCase("*/*") ||
                    accept.contains("application/json")
                    ) {
                //返回json数据
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                Response<String> exceptionResponse = ResponseHelper.createAccessTokenExceptionResponse();
                exceptionResponse.setPayload(authExceptionMessage);

                String exceptionResponseJsonStr = JsonUtil.object2JsonStr(exceptionResponse);
                out.println(exceptionResponseJsonStr);
            } else {
                //返回html页面
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String errorContent = ClassPathResourceUtil.read(AUTH_ERROR_PAGE_PATH);

                //替换掉占位符
                //替换掉占位符
                Map<String, String> map = new HashMap<>();
                map.put(EXCEPTION_MESSAGE_PLACEHOLDER, authExceptionMessage);
                map.put(AUTH_LOGIN_URL_PLACEHOLDER, getLoginUrl());
                errorContent = ClassPathResourceUtil.replaceMap(errorContent, map);

                //写入到客户端
                out.println(errorContent);
            }
        });
    }

    /**
     * 获取当前租户下的认证服务登录地址，
     * @return
     */
    private String getLoginUrl() {
        String result = null;
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(AUTHENTICATION_UI_APP_NAME);
        if(serviceInstanceList==null || serviceInstanceList.isEmpty()){
            //EurekaServer中没有认证服务
            return "javascript:alert('EurekaServer中没有认证服务');";
        }

        String currTenant = environment.getProperty("multiple-tenant.tenant");
        for(ServiceInstance serviceInstance : serviceInstanceList){
            InstanceInfo instanceInfo = ((EurekaDiscoveryClient.EurekaServiceInstance) serviceInstance).getInstanceInfo();
            String tenant = instanceInfo.getMetadata().get("tenant");
            if(currTenant.equalsIgnoreCase(tenant)){
                //如果有多个实例，则只取第一个。
                result = instanceInfo.getHomePageUrl() + "/login";
                break;
            }
        }
        if(StringUtils.isBlank(result)){
            //当前租户下没有认证服务
            return "javascript:alert('当前租户下没有认证服务');";
        }else {
            return result;
        }
    }
}
