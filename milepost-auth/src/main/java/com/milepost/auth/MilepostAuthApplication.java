package com.milepost.auth;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

@EnableEurekaClient
@SpringBootApplication
@MapperScan("com.milepost.auth.*.dao")
@ComponentScan(basePackages = {
		"com.milepost.auth",//这里必须得指定一下默认扫描的包，而且最好放到第一行，否则无法扫描这个包
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring",// ApplicationContextProvider
		//jwt服务没有license，以提高性能
		//"com.milepost.core.lns",//license
        //"com.milepost.core.activeMq",//mq
		//"com.milepost.core.restTemplate",//restTemplate
		"com.milepost.service.config.dynamicDs",//动态数据源
		"com.milepost.core.sleuth"//链路跟踪
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
