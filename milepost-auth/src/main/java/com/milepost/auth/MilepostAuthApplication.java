package com.milepost.auth;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

@EnableEurekaClient
@SpringBootApplication
@ComponentScan(basePackages = {
		"com.milepost.auth",//这里必须得指定一下默认扫描的包，而且最好放到第一行，否则无法扫描这个包
		"com.milepost.core.listener",//打印banner、
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring",// ApplicationContextProvider
		"com.milepost.service.config.druid",//数据源
		"com.milepost.core.mq"//mq
})
public class MilepostAuthApplication {

	public static void main(String[] args) {
		//SpringApplication.run(MilepostAuthApplication.class, args);

		//auth需要的自定义属性，这里没有像MilepostService那样单独写一个类，因为这个是不对程序员开放的。
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.AUTH.getValue());
		MilepostApplication.run(customProperties, MilepostAuthApplication.class, args);
	}

}
