package com.milepost.service;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

@ComponentScan(basePackages = {"com.milepost.core", "com.milepost.service.config.druid"})
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
