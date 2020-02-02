package com.milepost.api.util;

import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/1/29.
 */
public class JsonUtilTest {
    @Test
    public void test1(){
        String jsonObjectStr = "{\"javaModelGenerator.targetProject\":\"xxx\",\"javaModelGenerator.targetPackage\":\"xxx\",\"sqlMapGenerator.targetProject\":\"xxx\",\"sqlMapGenerator.targetPackage\":{\"aa\":\"11\",\"bb\":\"22\"},\"cc\":[\"a\",\"b\",\"c\",{\"1\":\"a\",\"2\":\"b\"}]}";
        Map<String, Object> map = JsonUtil.jsonObject2Map(jsonObjectStr);
        System.out.println(map);
        System.out.println(map.get("javaModelGenerator.targetProject"));
        Map<String, Object> map1 = (Map<String, Object>)map.get("sqlMapGenerator.targetPackage");
        System.out.println(map1.get("aa"));
        List<Object> list = (List<Object>)map.get("cc");
        System.out.println(list.get(0));
        Map<String, Object> map2 = (Map<String, Object>)list.get(3);
        System.out.println(map2.get("1"));
    }

    @Test
    public void test2(){
        String jsonArrayStr = "[\"a\", \"b\", \"c\", {\n" +
                "\t\"1\": \"a\",\n" +
                "\t\"2\": \"b\"\n" +
                "}]";
        List<Object> list = JsonUtil.jsonArray2List(jsonArrayStr);
        System.out.println(list.get(0));
        Map<String, Object> map2 = (Map<String, Object>)list.get(3);
        System.out.println(map2.get("1"));
    }
}
