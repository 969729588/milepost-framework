package com.milepost.admin;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

//开启AdminServer
@EnableAdminServer
//@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
		"com.milepost.admin",
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring",//ApplicationContextProvider
		"com.milepost.core.sleuth"//链路跟踪
})
public class MilepostAdminApplication {

	public static void main(String[] args) {
//		SpringApplication.run(MilepostAdminApplication.class, args);

		//自定义属性
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.ADMIN.getValue());
		MilepostApplication.run(customProperties, MilepostAdminApplication.class, args);
	}

//	@Bean
//	public InstanceExchangeFilterFunction auditLog() {
//		return (instance, request, next) -> {
//			System.out.println("-----");
//			return next.exchange(request);
//		};
//	}
}
