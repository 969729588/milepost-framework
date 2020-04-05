package com.milepost.turbine;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.turbine.EnableTurbine;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

@EnableEurekaClient
@SpringBootApplication
@EnableTurbine
@EnableHystrixDashboard
@ComponentScan(basePackages = {
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring",// ApplicationContextProvider
		"com.milepost.core.sleuth"//链路跟踪
})
public class MilepostTurbineApplication {

	public static void main(String[] args) {
//		SpringApplication.run(MilepostTurbineApplication.class, args);

		//自定义属性
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.TURBINE.getValue());
		MilepostApplication.run(customProperties, MilepostTurbineApplication.class, args);

	}

}
