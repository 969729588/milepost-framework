package com.milepost.service.config.dynamicDs;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.milepost.api.util.ReadAppYml;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/2/13.
 * 根据配置文件创建主数据源，手动创建除主数据源之外的其他数据源并放入ioc容器中，
 * 创建动态数据源，即AbstractRoutingDataSource的子类。
 */
@Component
public class ManualCreateDataSource implements BeanDefinitionRegistryPostProcessor {

    private Logger logger = LoggerFactory.getLogger(ManualCreateDataSource.class);

    /**
     * 除了主数据源之外的其他数据源
     */
    private Map<Object, Object> multipleDs;

    /**
     * bean工厂，能从中获取bean，也能将自己new的bean对象放入其(ioc容器)中，
     */
    private ConfigurableListableBeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    }

    /**
     * spring框架调用，注入beanFactory
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        initMultipleDs();
        for(Map.Entry<Object, Object> entry : multipleDs.entrySet()){
            String key = (String)entry.getKey();
            Object value = entry.getValue();
            beanFactory.registerSingleton(key, value);
        }
    }

    /**
     * 初始化除主数据源之外的其他数据源
     */
    private void initMultipleDs() {
       multipleDs = new HashMap<>();
        try {
            String druidKey = "spring.datasource.druid";
            Map<String, Object> druidMap = ReadAppYml.getMap(druidKey);

            for(Map.Entry<String, Object> entry : druidMap.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof Map){
                    DataSource dataSource = DruidDataSourceBuilder.create().build();
                    BeanUtils.populate(dataSource, (Map)value);
                    multipleDs.put(key, dataSource);
                }
            }
        }catch (Exception e){
            logger.error("实例化多数据源异常。", e);
        }
    }

    /**
     * 配置主数据源，即默认数据源
     * @return
     */
    @Bean(name = "mainDs")//主数据源，即默认数据源
    //@Primary //当有多个bean存在时，这个是主bean，优先使用这个，增加动态数据源后，这个注解应该标注在创建milepostRoutingDataSource方法上
    @ConfigurationProperties("spring.datasource.druid")
    @ConditionalOnExpression("#{environment.getProperty('spring.datasource.druid.password') != null}")  //当存在数据源配置时才创建数据源，否则不创建
    //@ConditionalOnExpression("'${spring.datasource.druid.password}'!='null'")//这个表达式不好用了
    public DataSource mainDs(){
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 配置动态数据源，即AbstractRoutingDataSource的子类
     * 需要标注成@Primary
     * @return
     */
    @Primary
    @Bean(name = "milepostRoutingDataSource")
    @ConditionalOnExpression("#{environment.getProperty('spring.datasource.druid.password') != null}")  //当存在数据源配置时才创建数据源，否则不创建
    public DataSource milepostRoutingDataSource() {
        MilepostRoutingDataSource milepostRoutingDataSource = new MilepostRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource mainDs = (DataSource)this.beanFactory.getBean("mainDs");
        targetDataSources.put("mainDs", mainDs);//主数据源
        targetDataSources.putAll(multipleDs);//其他数据源
        milepostRoutingDataSource.setTargetDataSources(targetDataSources);
        milepostRoutingDataSource.setDefaultTargetDataSource(mainDs);
        return milepostRoutingDataSource;
    }
}
