package com.milepost.ui;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;

//父类上的注解会被子类继承
@ComponentScan(basePackages = {
		"com.milepost.ui.config.auth",//认证、
		"com.milepost.core.listener",//打印banner、
		"com.milepost.core.lock",//分布式锁、
		"com.milepost.core.multipleTenant",//多租户、
		"com.milepost.core.spring"// ApplicationContextProvider
})
public class MilepostUiApplication {

	public MilepostUiApplication() {
	}

	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
		//ui需要的自定义属性
		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY, MilepostApplicationType.UI.getValue());
		ConfigurableApplicationContext context = MilepostApplication.run(customProperties, primarySource, args);
		return context;
	}

}
