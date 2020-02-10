package com.milepost.core.restTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Created by Ruifu Hua on 2020/2/9.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * springboot自动注入了RestTemplateBuilder，见WebClientAutoConfiguration.RestTemplateConfiguration#restTemplateBuilder
     */
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Bean
    public RestTemplate restTemplate() {
        restTemplateBuilder.setConnectTimeout(Duration.ofMillis(10000));
        restTemplateBuilder.setReadTimeout(Duration.ofMillis(10000));
        return restTemplateBuilder.build();
    }
}
