package com.milepost.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/2/13.
 */
public class ReadAppYml {

    private static Logger logger = LoggerFactory.getLogger(ReadAppYml.class);

    private static HashMap appYmlMap = null;

    /**
     * 读取配置文件
     */
    static {
        InputStream inputStream = null;
        try {
            //读取application.yml中配置的属性，当向defaultProperties中put ${xxx}样子的值来引入application.yml中的内容时，
            // 就可以先从appYmlMap中获取到xxx的值，然后put进入defaultProperties中，然后才能使用${xxx}引用
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            ClassPathResource resource = new ClassPathResource("application.yml");
            inputStream = resource.getInputStream();
            //最顶层的元素
            ReadAppYml.appYmlMap = mapper.readValue(inputStream, HashMap.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * 获取从yml中读取到的map
     * @return
     */
    public static HashMap getAppYmlMap() {
        return ReadAppYml.appYmlMap;
    }

    /**
     * @param mapKey
     * @return
     */
    public static Map<String, Object> getMap(String mapKey) {
        String[] keyArray = mapKey.split("\\.");
        Map<String, Object> mapTemp = ReadAppYml.appYmlMap;
        for(int i=0; i<(keyArray.length); i++){
            if(mapTemp != null){
                mapTemp = (HashMap<String, Object>)mapTemp.get(keyArray[i]);
            }else{
                return null;
            }
        }
        return mapTemp;
    }

    /**
     * 如果没有配置则返回null
     * @param key
     * @return
     */
    public static String getValue(String key) {
        String[] keyArray = key.split("\\.");
        String mapKey = key.substring(0, (key.lastIndexOf(".")));
        Map<String, Object> map = getMap(mapKey);
        if(map != null){
            Object value = map.get(keyArray[keyArray.length-1]);
            if(value == null){
                return null;
            }
            //读取到那些在yml中引用其他属性的属性值
            String valurString = String.valueOf(value);
            if(valurString.startsWith("${") && valurString.endsWith("}")){
                return getValue(valurString);
            }else{
                return valurString;
            }
        }else{
            return null;
        }
    }
}
