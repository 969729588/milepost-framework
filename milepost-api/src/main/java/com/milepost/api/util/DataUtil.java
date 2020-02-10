package com.milepost.api.util;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class DataUtil {

	/**
	 * 传入一个JavaBean的集合，返回所有实例的指定字段的字符串，如何集合不可用的，则返回空字符串
	 * @param collection JavaBean 的集合
	 * @param fieldName 字段名，如userName、name等，默认是id
	 * @param delimiter 分隔符，默认是“,”
	 * @return
	 * @throws Exception
	 */
	public<E> String getFieldValues(Collection<E> collection, String fieldName, String delimiter) throws Exception {
    	String result = "";
    	
    	if(StringUtils.isBlank(fieldName))
    		fieldName = "id";
    	if(StringUtils.isBlank(delimiter))
    		delimiter = ",";
    	
		if(collection != null){
    		for(E e :collection){
    			if(e!=null){
    				Class<?> clazz = e.getClass();
    				Method method = clazz.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
    				Object fieldValue = method.invoke(e);
    				result = result + fieldValue.toString() + delimiter;
    			}
    		}
    		result = Pattern.compile(delimiter + "+$").matcher(result).replaceAll("");
    	}
		return result;
    }
	
	/**
	 * 传入一个JavaBean的集合，返回所有实例的指定字段的字符串，用“,”分割，如何集合不可用的，则返回空字符串
	 * @param collection JavaBean 的集合
	 * @param fieldName 字段名，如userName、name等，默认是id
	 * @return
	 * @throws Exception
	 */
	public<E> String getFieldValues(Collection<E> collection, String fieldName) throws Exception {
		return getFieldValues(collection, fieldName, ",");
    }
	
	/**
	 * 传入一个JavaBean的集合，返回所有实例的ids，如何集合不可用的，则返回空字符串
	 * @param collection
	 * @param delimiter 分隔符
	 * @return
	 * @throws Exception
	 */
	public<E> String getIds(Collection<E> collection, String delimiter) throws Exception {
		return getFieldValues(collection, "id", delimiter);
	}
	
	/**
	 * 传入一个JavaBean的集合，返回所有实例的ids，用“,”分割，如何集合不可用的，则返回空字符串
	 * @param collection
	 * @return
	 * @throws Exception
	 */
	public<E> String getIds(Collection<E> collection) throws Exception {
		return getFieldValues(collection, "id", ",");
	}
	
	/**
	 * 传入一个JavaBean的集合，返回所有实例的idList，如何集合不可用的，则返回空list
	 * @param collection
	 * @return
	 * @throws Exception
	 */
	public<E> List<String> getIdList(Collection<E> collection) throws Exception {
		if(collection == null){
			return new ArrayList<String>();
		}
		
		String ids = getFieldValues(collection, "id", ",");
		if(StringUtils.isNotBlank(ids)){
			return Arrays.asList(ids.split(","));
		}else {
			return new ArrayList<String>();
		}
	}
}
