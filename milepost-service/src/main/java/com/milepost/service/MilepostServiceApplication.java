package com.milepost.service;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

@ComponentScan(basePackages = {
		"com.milepost.service.config.auth",//认证、
		"com.milepost.service.config.openfeign",//feignClient拦截器
		"com.milepost.core.lock",//分布式锁、
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring",// ApplicationContextProvider
		"com.milepost.core.lns",//license
		"com.milepost.core.activeMq",//activeMq
		"com.milepost.core.rabbitMq",//rabbitMq
		"com.milepost.core.restTemplate",//restTemplate
		"com.milepost.core.redisTemplate",//redisTemplate
		"com.milepost.service.config.dynamicDs",//动态数据源
		"com.milepost.core.serviceDiscovery",//服务发现
		"com.milepost.core.sleuth"//链路跟踪
})
//Swagger，这里好像默认是全部扫描，之后需要指定一下扫描的包,
//访问：http://192.168.223.1:9991/authentication-service/swagger-ui.html
@EnableSwagger2Doc
public class MilepostServiceApplication {

	public MilepostServiceApplication() {
	}

	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
		//service需要的自定义属性
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.SERVICE.getValue());
		ConfigurableApplicationContext context = MilepostApplication.run(customProperties, primarySource, args);
		return context;
	}
}
