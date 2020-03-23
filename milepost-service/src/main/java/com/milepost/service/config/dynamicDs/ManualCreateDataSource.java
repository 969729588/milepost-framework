package com.milepost.service.config.dynamicDs;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

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
@Configuration
public class ManualCreateDataSource {

    private Logger logger = LoggerFactory.getLogger(ManualCreateDataSource.class);

    //主(默认)数据源key，
    public static final String DEFAULT_DS_KEY = "default";

    /**
     * 主数据源
     * @return
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties mainDataSourceProperties() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        return dataSourceProperties;
    }

    /**
     * 配置动态数据源，即AbstractRoutingDataSource的子类<br>
     * 需要标注成@Primary，<br>
     * 入参中的Environment中的参数已经解密过了，这太棒了，之前没解密是通过StringEncryptor解密的。<br>
     * @return
     */
    @Primary
    @Bean(name = "milepostRoutingDataSource")
    @ConditionalOnExpression("#{environment.getProperty('spring.datasource.password') != null}")  //当存在数据源配置时才创建数据源，否则不创建
    public DataSource milepostRoutingDataSource(Environment environment/*, StringEncryptor stringEncryptor*/) {
        MilepostRoutingDataSource milepostRoutingDataSource = new MilepostRoutingDataSource();

        Map<Object, Object> targetDataSources = new HashMap<>();

        //主数据源
        DataSourceProperties mainDataSourceProperties = mainDataSourceProperties();
        HikariDataSource mainDataSource = mainDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();

        //为主数据源绑定hikari连接池属性
        final Binder binder = Binder.get(environment);
        final Bindable<? extends HikariDataSource> mainDataSourceBindable = Bindable.ofInstance(mainDataSource);
        binder.bind("spring.datasource.hikari", mainDataSourceBindable);

        //保存主数据源
        targetDataSources.put(DEFAULT_DS_KEY, mainDataSource);

        //创建多数据源
        Map<Object, Object> multipleDataSourceMap = initMultipleDataSourceMap(environment);
        targetDataSources.putAll(multipleDataSourceMap);

        //保存多数据源
        milepostRoutingDataSource.setTargetDataSources(targetDataSources);
        milepostRoutingDataSource.setDefaultTargetDataSource(mainDataSource);

        return milepostRoutingDataSource;
    }

    /**
     * 初始化多数据源
     * @param environment
     * @return
     */
    private Map<Object,Object> initMultipleDataSourceMap(Environment environment) {
        Map<Object,Object> multipleDataSourceMap = new HashMap<>();
        try {
            String springDsKey = "spring.datasource";
            String hikariKey = "hikari";

            //环境变量绑定到map
            Map<String, Object> envMap = new LinkedHashMap<>();
            final Bindable<? extends Map> bindable = Bindable.ofInstance(envMap);
            final Binder binder = Binder.get(environment);
            binder.bind(springDsKey, bindable);

            //存储多数据源，是原始的，key和value都没经过处理的
            Map<String, Object> multipleDsOriginalEnvMaps = new LinkedHashMap<>();
            for(Map.Entry<String, Object> entry : envMap.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();

                //多数据源的属性，即spring.datasource 下 Map类型的并且key不是hikari的，
                if((value instanceof Map) && !key.equalsIgnoreCase(hikariKey)){
                    multipleDsOriginalEnvMaps.put(key, value);
                }
            }

            //创建多数据源
            for(Map.Entry<String, Object> entry : multipleDsOriginalEnvMaps.entrySet()){
                String multipleDsKey = entry.getKey();//多数据源的名称(key)，一定不要将他改变

                //为多数据源绑定连接属性
                DataSourceProperties multipleDataSourceProperties = new DataSourceProperties();
                final Bindable<? extends DataSourceProperties> multipleDataSourcePropertiesBindable = Bindable.ofInstance(multipleDataSourceProperties);
                binder.bind(springDsKey + "."+ multipleDsKey, multipleDataSourcePropertiesBindable);

                //为多数据源绑定hikari连接池属性
                HikariDataSource multipleDataSource = multipleDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
                final Bindable<? extends HikariDataSource> multipleDataSourceBindable = Bindable.ofInstance(multipleDataSource);
                binder.bind(springDsKey + "."+ multipleDsKey +".hikari", multipleDataSourceBindable);

                multipleDataSourceMap.put(multipleDsKey, multipleDataSource);
            }
        }catch (Exception e){
            logger.error("创建多数据源异常。", e);
        }
        return multipleDataSourceMap;
    }
}
