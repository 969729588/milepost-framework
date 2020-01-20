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
		"com.milepost.core.listener",//打印banner、
		"com.milepost.core.lock",//分布式锁、
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring",// ApplicationContextProvider
		"com.milepost.service.config.druid"//数据源
})
//Swagger，这里好像默认是全部扫描，之后需要指定一下扫描的包
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
