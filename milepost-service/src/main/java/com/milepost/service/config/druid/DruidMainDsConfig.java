package com.milepost.service.config.druid;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Created by Ruifu Hua on 2020/1/17.
 */
@Configuration
public class DruidMainDsConfig {

    /**
     * 配置主数据源
     * @return
     */
    @Bean
    @Primary //当有多个bean存在时，这个是主bean，优先使用这个
    @ConfigurationProperties("spring.datasource.druid")
    @ConditionalOnExpression("#{environment.getProperty('spring.datasource.druid.password') != null}")  //当存在数据源配置时才创建数据源，否则不创建
    //@ConditionalOnExpression("'${spring.datasource.druid.password}'!='null'")//这个表达式不好用了
    public DataSource dataSource(){
        return DruidDataSourceBuilder.create().build();
    }

}
