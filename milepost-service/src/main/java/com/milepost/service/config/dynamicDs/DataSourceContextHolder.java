package com.milepost.service.config.dynamicDs;

/**
 * 获得和设置上下文环境 主要负责改变上下文数据源的名称
 * @author Huarf
 * 2017年6月4日
 */
public class DataSourceContextHolder {
	// 线程本地环境
	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

	// 设置默认数据源
	public static void setDefaultDataSource() {
		contextHolder.set(ManualCreateDataSource.DEFAULT_DS_KEY);
	}

	// 设置数据源
	public static void setDataSource(String dataSource) {
		contextHolder.set(dataSource);
	}

	// 获取数据源
	public static String getDataSource() {
		return contextHolder.get();
	}

	// 清除数据源
	public static void clearDataSource() {
		contextHolder.remove();
	}
}
