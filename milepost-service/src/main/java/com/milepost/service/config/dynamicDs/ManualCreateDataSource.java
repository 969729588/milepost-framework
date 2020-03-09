package com.milepost.service.config.dynamicDs;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.google.common.base.CaseFormat;
import org.apache.commons.beanutils.BeanUtils;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/2/13.
 * 创建动态数据源，即AbstractRoutingDataSource的子类。
 *
 * 读取Environment中的数据，手动创建主数据源和其他多数据源，并放入ioc容器中，
 * 涉及到将Environment中的数据绑定到Map中，数据解密、命名方式转换、
 *
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

    public static final String MAIN_DS = "mainDs";

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
     * 从Environment中读取数据，能识别命令行和环境变量中的配置
     * 涉及到将Environment中的数据绑定到Map中，数据解密等
     */
    private void initMultipleDs() {
        multipleDs = new HashMap<>();
        try {
            StringEncryptor stringEncryptor = this.beanFactory.getBean(StringEncryptor.class);
            //此处不能读取yml，因为springboot的配置项有多处来源，只能读取Environment，读取yml要慎用
            Environment environment = this.beanFactory.getBean(Environment.class);
            String druidKey = "spring.datasource.druid";

            //环境变量绑定到map
            Map<String, Object> envMap = new LinkedHashMap<>();
            final Bindable<? extends Map> bindable = Bindable.ofInstance(envMap);
            final Binder binder = Binder.get(environment);
            binder.bind(druidKey, bindable);

            //存储多数据源，是原始的，key和value都没经过处理的
            Map<String, Object> multipleDsOriginalEnvMaps = new LinkedHashMap<>();

            //map中，连词符形式的key转换为驼峰形式，map中String类型值解密
            Map<String, Object> mainDsEnvMap = new LinkedHashMap<>();
            for(Map.Entry<String, Object> entry : envMap.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();

                //除了多数据源的key-value之外，都要转换和解密
                if(!(value instanceof Map)){
                    String newKey = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);
                    //yml中只有字符串类型的配置项才支持加密，
                    Object newValue = null;
                    if(value instanceof String && ((String)value).startsWith("ENC(") && ((String)value).endsWith(")")){
                        //解密(配置文件中只有字符串类型的配置才支持加密)
                        String valueStr = (String)value;
                        valueStr = valueStr.substring(4, valueStr.length());
                        newValue = stringEncryptor.decrypt(valueStr);
                    }else{
                        //不解密，
                        newValue = value;
                    }
                    mainDsEnvMap.put(newKey, newValue);
                }else{
                    multipleDsOriginalEnvMaps.put(key, value);
                }
            }

            //创建主数据源
            DataSource mainDataSource = DruidDataSourceBuilder.create().build();
            BeanUtils.populate(mainDataSource, mainDsEnvMap);
            multipleDs.put(MAIN_DS, mainDataSource);

            //创建其他数据源
            for(Map.Entry<String, Object> entry : multipleDsOriginalEnvMaps.entrySet()){
                String multipleDsKey = entry.getKey();//多数据源的名称，一定不要将他改变
                Map<String, Object> multipleDsOriginalEnvMap = (Map<String, Object>)entry.getValue();
                //--------------------
                //map中，连词符形式的key转换为驼峰形式，map中String类型值解密
                Map<String, Object> multipleDsEnvMap = new LinkedHashMap<>();
                for(Map.Entry<String, Object> mEntry : multipleDsOriginalEnvMap.entrySet()){
                    String key = mEntry.getKey();
                    Object value = mEntry.getValue();

                    String newKey = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);
                    //yml中只有字符串类型的配置项才支持加密，
                    Object newValue = null;
                    if(value instanceof String && ((String)value).startsWith("ENC(") && ((String)value).endsWith(")")){
                        //解密(配置文件中只有字符串类型的配置才支持加密)
                        String valueStr = (String)value;
                        valueStr = valueStr.substring(4, valueStr.length());
                        newValue = stringEncryptor.decrypt(valueStr);
                    }else{
                        //不解密，
                        newValue = value;
                    }

                    multipleDsEnvMap.put(newKey, newValue);
                }
                //--------------------
                DataSource multipleDataSource = DruidDataSourceBuilder.create().build();
                BeanUtils.populate(multipleDataSource, mainDsEnvMap);//实现多数据源中未配置的属性继承主数据源
                BeanUtils.populate(multipleDataSource, multipleDsEnvMap);
                multipleDs.put(multipleDsKey, multipleDataSource);
            }
        }catch (Exception e){
            logger.error("创建数据源异常。", e);
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
        milepostRoutingDataSource.setDefaultTargetDataSource(multipleDs.get(MAIN_DS));
        return milepostRoutingDataSource;
    }
}
