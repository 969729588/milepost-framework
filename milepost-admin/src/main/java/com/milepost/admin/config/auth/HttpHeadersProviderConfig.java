package com.milepost.admin.config.auth;

import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.api.util.EncryptionUtil;
import com.milepost.core.multipleTenant.MultipleTenantProperties;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ruifu Hua on 2020/3/30.
 * 从jwt服务获取access_token，SpringBoot Admin每次抓取监控数据的请求都带上access_token
 */
@Configuration
public class HttpHeadersProviderConfig {

    private Logger logger = LoggerFactory.getLogger(HttpHeadersProviderConfig.class);

    @Autowired
    private AuthFc authFc;

    @Autowired
    private MultipleTenantProperties multipleTenantProperties;

    private Jwt jwt;

    @Order(0)
    @Bean("bearerTokenHttpHeaderProvider")
    public HttpHeadersProvider bearerTokenHttpHeaderProvider(){
        return (instance) -> {
            HttpHeaders headers = new HttpHeaders();
            try {
                Registration registration = instance.getRegistration();
                Map<String, String> metadata = registration.getMetadata();
                String applicationType = metadata.get("milepost-type");
                if(MilepostApplicationType.ADMIN.getValue().equalsIgnoreCase(applicationType)){
                    //SBA Server使用Basic Auth
                    String username = instance.getRegistration().getMetadata().get("sba_server.user");
                    String password = instance.getRegistration().getMetadata().get("sba_server.password");
                    String basicAuthorization = "Basic " + EncryptionUtil.encodeWithBase64(username + ":" + password);//Basic Auth，即 client_id + ":" + client_secret 的base64编码
                    //放入请求头中
                    headers.set(HttpHeaders.AUTHORIZATION, basicAuthorization);
                }else{
                    //获取token
                    String accessToken = requestOrReturnLocalAccessToken(instance);
                    //放入请求头中
                    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                }

            } catch (UnsupportedEncodingException e) {
                logger.error("获取access_token异常。");
                logger.error(e.getMessage(), e);
            }

            return headers;
        };
    }

    /**
     * 从JWT中获取token或者返回本地token，检查token是否过期，是则从JWT中获取，否则使用本地缓存的。
     * @param instance
     * @return
     * @throws UnsupportedEncodingException
     */
    private String requestOrReturnLocalAccessToken(Instance instance) throws UnsupportedEncodingException {
        //当前时间
        long now = Instant.now().toEpochMilli();

        //首先判断本地是否存在可用的token
        if(jwt != null){
            //将在这个时间之后过期
            long expiresAt = jwt.getExpires_at();

            //预留时长10分钟
            long offSet = TimeUnit.MINUTES.toMillis(10);
            if((now+offSet) >= expiresAt){
                //过期，重新获取，并给全局对象赋值
                Jwt newJwt = requestAccessToken(instance);
                //确定expiresAt
                newJwt.setExpires_at(TimeUnit.SECONDS.toMillis(newJwt.getExpires_in()) + now);
                this.jwt = newJwt;
            }else {
                //没过期，使用本地的
            }
        }else{
            //本地没有Jwt，获取，并给全局对象赋值
            Jwt newJwt = requestAccessToken(instance);
            //确定expiresAt
            newJwt.setExpires_at(TimeUnit.SECONDS.toMillis(newJwt.getExpires_in()) + now);
            this.jwt = newJwt;
        }

        return this.jwt.getAccess_token();
    }

    private Jwt requestAccessToken(Instance instance) throws UnsupportedEncodingException {
        //获取SBA Clinet的实例元数据中的用户名和密码
        //此用户已经写在jwt服务的slq脚本中了
        String username = instance.getRegistration().getMetadata().get("sba_server.user");
        String password = instance.getRegistration().getMetadata().get("sba_server.password");

        String tenant = multipleTenantProperties.getTenant();
        String clientId = "client_id_" + tenant;
        String clientSecret = "client_secret_" + tenant;
        String basicAuthorization = "Basic " + EncryptionUtil.encodeWithBase64(clientId + ":" + clientSecret);//Basic Auth，即 client_id + ":" + client_secret 的base64编码
        String grantType = "password";//授权方式
        //这里可以使用OpenFeign的feign.auth.BasicAuthRequestInterceptor实现，那样会代码会更多，但可以借鉴其如何实现拦截器
        Jwt jwt = authFc.getToken(basicAuthorization, grantType, username, password);
        return jwt;
    }
}
