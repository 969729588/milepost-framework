package com.milepost.api.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/3/15.
 */
public class ClassPathResourceUtil {
    /**
     * 读取类路径(resources)下的文件内容
     * @param path 文件路径，如application.yml、static/index.html
     * @return
     */
    public static String read(String path) throws IOException {
        InputStream inputStream = null;
        String content = "";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            inputStream = resource.getInputStream();
            content = IOUtils.toString(inputStream, "UTF-8");
        }catch (Exception e){
            throw e;
        }finally {
            IOUtils.closeQuietly(inputStream);
        }
        return content;
    }

    /**
     * 用map中的数据替换content中的占位符，例如：<br>
     *     map中有如下k-v对：
     *      key1=val1
     *      key2=val2
     *     content中有如下占位符：
     *      aa${key1}bb
     *      cc${key2}dd
     *     替换后将得到：
     *      aaval1bb
     *      ccval2dd
     * @param content
     * @param map
     * @return
     */
    public static String replaceMap(String content, Map<String, String> map){
        if(StringUtils.isBlank(content) || map==null){
            return content;
        }
        for(Map.Entry<String, String> entry : map.entrySet()){
            String key = entry.getKey();
            String placeholder = "${"+ key +"}";
            String value = entry.getValue();
            if(content.contains(placeholder)){
                content = content.replace(placeholder, value);
            }
        }
        return content;
    }
}
