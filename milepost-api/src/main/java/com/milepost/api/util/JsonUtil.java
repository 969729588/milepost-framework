package com.milepost.api.util;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * json转换
 */
public class JsonUtil {
	
	public static Map<String, Object> jsonObject2Map(String jsonObjectStr){
		Map<String, Object> map = JSONObject.parseObject(jsonObjectStr, Map.class);
		return map;
	}

	public static List<Object> jsonArray2List(String jsonArrayStr){
		List<Object> list = JSONObject.parseObject(jsonArrayStr, List.class);
		return list;
	}


}
