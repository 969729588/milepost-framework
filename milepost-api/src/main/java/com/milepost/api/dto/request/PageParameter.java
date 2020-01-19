package com.milepost.api.dto.request;

import io.swagger.annotations.ApiModelProperty;

/**
 * 分页参数，从前端接收，传入PageHelper.startPage(Object params)方法中
 * @author HRF
 */
public class PageParameter {
	
	/**
	 * 页码，默认1
	 */
	@ApiModelProperty(value = "页码，非必须参数，默认值1。", required = false, dataType = "Long")
	private int pageNum = 1;
	
	/**
	 * 页大小，默认10
	 */
	@ApiModelProperty(value = "页大小，非必须参数，默认值10。", required = false, dataType = "Long")
	private int pageSize = 10;
	
	/**
	 * 分页合理化参数，默认值为true。当该参数设置为 true 时，pageNum<=0 时会查询第一页， pageNum>pages（超过总数时），会查询最后一页。
	 * 设置为false 时，直接根据参数进行查询。
	 */
	@ApiModelProperty(hidden = true)
	private boolean reasonable = true;
	
	/**
	 * 是否进行count查询，默认是，如果确实不需要查询记录总数，则置为false可提高性能
	 */
	@ApiModelProperty(hidden = true)
	private boolean countSql = true;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isReasonable() {
		return reasonable;
	}

	public void setReasonable(boolean reasonable) {
		this.reasonable = reasonable;
	}

	public boolean isCountSql() {
		return countSql;
	}

	public void setCountSql(boolean countSql) {
		this.countSql = countSql;
	}

	@Override
	public String toString() {
		return "PageParameter [pageNum=" + pageNum + ", pageSize=" + pageSize + ", reasonable=" + reasonable
				+ ", countSql=" + countSql + "]";
	}

	public PageParameter(int pageNum, int pageSize) {
		super();
		this.pageNum = pageNum;
		this.pageSize = pageSize;
	}

	public PageParameter() {
		super();
	}
}
