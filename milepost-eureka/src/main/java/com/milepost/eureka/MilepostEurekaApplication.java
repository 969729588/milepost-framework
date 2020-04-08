package com.milepost.eureka;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import com.milepost.core.sleuth.SleuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@EnableScheduling
@EnableEurekaServer
@SpringBootApplication
@ComponentScan(basePackages = {
		"com.milepost.core.spring",//ApplicationContextProvider
        //EurekaServer服务没有license，以提高性能
        //"com.milepost.core.lns"//license
		"com.milepost.core.sleuth"//链路跟踪
}, excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = SleuthFilter.class))//排除SleuthFilter，因为EurekaServer不区分租户
public class MilepostEurekaApplication{

	private static Logger logger = LoggerFactory.getLogger(MilepostEurekaApplication.class);

	public static void main(String[] args) {
//		SpringApplication.run(MilepostEurekaApplication.class, args);

		//EurekaServer需要的自定义属性
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.EUREKA.getValue());
		MilepostApplication.run(customProperties, MilepostEurekaApplication.class, args);
	}

	/**
	 * 为了测试的，没有实际作用
	 * EurekaServer端使用PeerAwareInstanceRegistry获取注册到注册中心的所有服务，获取的服务与Eureka Dashboard显示的一致。
	 * EurekaClient端使用org.springframework.cloud.client.discovery.DiscoveryClient获取注册到注册中心的所有服务。
	 * com.netflix.discovery.EurekaClient用来在客户端获取当前服务的InstanceInfo。
	 */
//	@Scheduled(initialDelay = 10000, fixedDelay = 5000)
//	public void printAllServiceInstance() {
//		//获取注册到注册中心的所有服务，获取的服务与Eureka Dashboard显示的一致。
//		PeerAwareInstanceRegistry registry = EurekaServerContextHolder.getInstance().getServerContext().getRegistry();
//		Applications applications = registry.getApplications();
//		List<Application> registeredApplications = applications.getRegisteredApplications();
//		if(registeredApplications.size() == 0){
//			logger.info("PeerAwareInstanceRegistry没有发现任何服务注册到注册中心。");
//		}
//		for(Application application : registeredApplications){
//			List<InstanceInfo> instanceInfoList = application.getInstances();
//			for(InstanceInfo instanceInfo : instanceInfoList){
//				String appName = instanceInfo.getAppName();
//				String instanceId = instanceInfo.getInstanceId();
//				logger.info("PeerAwareInstanceRegistry获取到的服务--服务名称:" + appName + "; 实例ID:" + instanceId);
//			}
//		}
//	}
}
