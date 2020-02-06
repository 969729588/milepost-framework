package com.milepost.ui.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

/**
 * 解析token
 */
@Configuration
public class JwtConfig {

    @Autowired
    JwtAccessTokenConverter jwtAccessTokenConverter;

    public static final String  PUBLIC_KEY = "public.key";

    public static final String PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----\r";
    public static final String PUBLIC_KEY_END = "\r-----END PUBLIC KEY-----";

    @Bean
    @Qualifier("tokenStore")
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    protected JwtAccessTokenConverter jwtTokenEnhancer() {
        JwtAccessTokenConverter converter =  new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        String publicKey ;
        try {
            publicKey = PUBLIC_KEY_BEGIN + new String(FileCopyUtils.copyToByteArray(resource.getInputStream())) + PUBLIC_KEY_END;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        converter.setVerifierKey(publicKey);
        return converter;
    }
}
