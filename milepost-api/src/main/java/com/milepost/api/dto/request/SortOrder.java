package com.milepost.api.dto.request;

import io.swagger.annotations.ApiModelProperty;

public class SortOrder {

	/**
	 * 排序字段名称
	 */
	@ApiModelProperty(value = "排序字段名称", required = false, dataType = "String")
	private String sort;
	
	/**
	 * 排序方式，ASC、DESC
	 */
	@ApiModelProperty(value = "排序方式，ASC、DESC", required = false, dataType = "String")
	private String order;
	
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getOrder() {
		//转成大写的，因为sql语句中一般用大写的
		return order==null? null:order.toUpperCase();
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public SortOrder(String sort, String order) {
		super();
		this.sort = sort;
		this.order = order;
	}
	public SortOrder() {
		super();
	}
	@Override
	public String toString() {
		return "SortOrder [sort=" + getSort() + ", order=" + getOrder() + "]";
	}
	
}
