package com.milepost.api.vo.response;

/**
 * 返回体，所有的ajax请求都会返回这个实体，
 * easyui框架的请求，可能会不需要返回这个实体，
 * @author Huarf
 * 2017年8月28日
 */
public class Response<T> {
	/**
	 * 返回状态码，见 com.milepost.system.constant.ReturnCode
	 */
	private int code;
	/**
	 * 返回消息
	 */
	private String msg = "";
	/**
	 * 返回的数据
	 */
	private T payload;

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getPayload() {
		return this.payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return "Response [code=" + code + ", msg=" + msg + ", payload=" + payload + "]";
	}
}