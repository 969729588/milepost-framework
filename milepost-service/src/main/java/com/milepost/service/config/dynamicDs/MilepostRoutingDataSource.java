package com.milepost.service.config.dynamicDs;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 决定使用哪个数据源
 * @author Huarf
 * 2017年6月4日
 */
public class MilepostRoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected Object determineCurrentLookupKey() {
		return DataSourceContextHolder.getDataSource();
	}

}
