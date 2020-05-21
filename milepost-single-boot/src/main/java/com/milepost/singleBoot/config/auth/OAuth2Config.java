package com.milepost.singleBoot.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class OAuth2Config extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    /**
     * 生成jks文件时指定的alias，
     * jks文件在core项目中
     */
    public static String JKS_ALIAS = "milepost-alias";
    public static String JKS_FILE_NAME = "milepost.jks";
    public static String JKS_PASSWORD = "milepost";

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.inMemory()
//                .withClient("user-service")
//                .secret("123456")
//                .scopes("service")
//                .autoApprove(true)
//                .authorizedGrantTypes("implicit", "refresh_token", "password", "authorization_code")
//                .accessTokenValiditySeconds(12 * 300);//5min过期

//        INSERT INTO `oauth_client_details` VALUES ('user-service', null, '123456', 'service', 'implicit,refresh_token,password,authorization_code', null, null, '7200', '7200', '{\"a\":\"b\"}', 'true');
        clients.jdbc(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(tokenStore())
                .authenticationManager(authenticationManager)
                .tokenEnhancer(jwtTokenEnhancer())//这里不需要每次授权都查询数据库，所以不配置userServiceDetail，而配置tokenEnhancer(jwtTokenEnhancer())
                //设置refresh token是否重复使用,true:reuse;false:no reuse.
                .reuseRefreshTokens(false);//每次更新access token时，refresh token也生成新的
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
//                .allowFormAuthenticationForClients()
                .passwordEncoder(NoOpPasswordEncoder.getInstance());

        /**????????,应该不是这样的，
         * 必须设置allowFormAuthenticationForClients 否则没有办法用postman获取token
         * 也需要指定密码加密方式BCryptPasswordEncoder
         */
    }

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    /**
     * 这个是jwt的核心
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtTokenEnhancer());
    }

    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(JKS_FILE_NAME), JKS_PASSWORD.toCharArray());
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair(JKS_ALIAS));
        return converter;
    }
}
