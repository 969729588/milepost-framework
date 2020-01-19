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
		"com.milepost.core",//打印banner、分布式锁、多租户、ApplicationContextProvider
		"com.milepost.service.config.druid"//数据源
})
//Swagger
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
