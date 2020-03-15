package com.milepost.api.vo.response;

/**
 * 返回状态码
 * @author Huarf
 * 2017年8月28日
 */
public class ReturnCode {
	public static final int SUCCESS = 0;
	public static final int EXCEPTION = -1;
	public static final int BUSINESS_ERROR = -2;
//	public static final int BIND_ERROR = -3;
//	public static final int NOT_FOUND = -4;
//	public static final int PARAM_ERROR = -5;
	public static final int FEIGN_ERROR = -6;
//	public static final int ACCESS_TOKEN_EXPIRED = -7;
	public static final int ACCESS_TOKEN_EXCEPTION = -8;
	public static final int FAIL = -9;
	public static final int NEVER_USED_CODE = -999999;
}
