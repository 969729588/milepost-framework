package com.milepost.singleBoot;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;


@ComponentScan(basePackages = {
		"com.milepost.singleBoot.config.auth",//认证、
		"com.milepost.core.spring",// ApplicationContextProvider
		"com.milepost.core.lns",//license
		"com.milepost.core.activeMq",//activeMq
		"com.milepost.core.restTemplate",//restTemplate
		"com.milepost.core.redisTemplate",//redisTemplate
		"com.milepost.singleBoot.config.dynamicDs"//动态数据源
})
//Swagger，这里好像默认是全部扫描，之后需要指定一下扫描的包,
//访问：http://192.168.186.5:9991/authentication-service/swagger-ui.html
@EnableSwagger2Doc
public class MilepostSingleBootApplication {

	public MilepostSingleBootApplication() {
	}

	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
		//单一SpringBoot应用需要的自定义属性
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.SINGLE_BOOT.getValue());
		ConfigurableApplicationContext context = MilepostApplication.runSingleBoot(customProperties, primarySource, args);
		return context;
	}

}
