package com.milepost.service.config.dynamicDs;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.google.common.base.CaseFormat;
import com.milepost.api.util.ReadAppYml;
import org.apache.commons.beanutils.BeanUtils;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public static final String MAIN_SD = "mainDs";

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
     *
     * 从yml中读取配置，不能识别命令行参数
     */
    private void initMultipleDs() {
        multipleDs = new HashMap<>();
        try {
            StringEncryptor stringEncryptor = this.beanFactory.getBean(StringEncryptor.class);

            //创建主数据源
            String druidKey = "spring.datasource.druid";
            Map<String, Object> druidMap = ReadAppYml.getMap(druidKey);
            Map<String, Object> druidMapFormat = new LinkedHashMap<>();
            //将 key 的 小连词符 转 小驼峰， 解密value
            for(Map.Entry<String, Object> entry : druidMap.entrySet()){
                String key = entry.getKey();
                String keyFormat = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);

                Object value = entry.getValue();
                if(value instanceof String && ((String)value).startsWith("ENC(") && ((String)value).endsWith(")")){
                    //解密(配置文件中之后字符串类型的配置才支持加密)
                    String valueStr = (String)value;
                    valueStr = valueStr.substring(4, valueStr.length());
                    valueStr = stringEncryptor.decrypt(valueStr);
                    druidMapFormat.put(keyFormat, valueStr);
                }else{
                    //不解密，
                    druidMapFormat.put(keyFormat, value);
                }
            }

            //填充数据源对象
            DataSource mainDataSource = DruidDataSourceBuilder.create().build();
            BeanUtils.populate(mainDataSource, druidMapFormat);

            //保存主数据源
            multipleDs.put(MAIN_SD, mainDataSource);

            //创建其他数据源，没有配置的属性继承主数据源
            for(Map.Entry<String, Object> entry : druidMap.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof Map){
                    DataSource dataSource = DruidDataSourceBuilder.create().build();
                    BeanUtils.populate(dataSource, druidMapFormat);
                    BeanUtils.populate(dataSource, (Map)value);
                    multipleDs.put(key, dataSource);
                }
            }
        }catch (Exception e){
            logger.error("实例化多数据源异常。", e);
        }
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
        //这里的入参即会被传入上面mainDs()方法实例化的对象，不会实例化多个对象，
        //见com.milepost.authenticationExample.configurationAndComponent
        MilepostRoutingDataSource milepostRoutingDataSource = new MilepostRoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        //也可以通过这种方式获取mainDs。
        //DataSource mainDs = (DataSource)this.beanFactory.getBean("mainDs");
        targetDataSources.putAll(multipleDs);//其他数据源
        milepostRoutingDataSource.setTargetDataSources(targetDataSources);
        milepostRoutingDataSource.setDefaultTargetDataSource(multipleDs.get(MAIN_SD));
        return milepostRoutingDataSource;
    }
}
