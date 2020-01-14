package com.milepost.ui;

import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.MilepostApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

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
