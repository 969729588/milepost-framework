package com.milepost.api.util;

import javax.servlet.http.HttpServletRequest;

/**
 * request 相关的工具类
 * @author HRF
 */
public class RequestUtil {
	
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if ((ip == null) || (ip.length() == 0) || ("unknown".equalsIgnoreCase(ip))) {
			ip = request.getRemoteAddr();
		}
		if (("0:0:0:0:0:0:0:1".equals(ip)) || ("::1".equals(ip))) {
			ip = "127.0.0.1";
		}
		return ip;
	}
	
	/**
	 * 返回以http/https开始的服务端根目录，不以“/”结尾
	 * @param request
	 * @return
	 */
	public static String getBasePath(HttpServletRequest request){
		String path = request.getContextPath();
		String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
		return basePath;
	}
}