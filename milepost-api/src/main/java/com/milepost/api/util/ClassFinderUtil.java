package com.milepost.api.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruifu Hua on 2020/2/26.
 */
public class ClassFinderUtil {

    public static List<Class> getSubClass(Class supClass, String[] packageNames) throws IOException, ClassNotFoundException {
        if(supClass == null) {
            throw new IllegalArgumentException("父类不能为空。");
        }
        if(ArrayUtils.isEmpty(packageNames)){
            throw new IllegalArgumentException("包名不能为空。");
        }

        List<Class> returnClassList = new ArrayList<>();

        for(String packageName : packageNames) {
            List<Class> allClass = findClass(packageName);

            for(Class clazz : allClass) {
                //判断clazz是否是supClass的子孙类
                if(supClass.isAssignableFrom(clazz) && !supClass.equals(clazz)) {
                    returnClassList.add(clazz);
                }
            }
        }

        return returnClassList;
    }

    public static List<Class> findClass(String packageName) throws IOException, ClassNotFoundException {
        if(StringUtils.isBlank(packageName)){
            throw new IllegalArgumentException("包名不能为空。");
        }

        List<Class> clazz = new ArrayList<>();
        String path = packageName.replace(".", "/");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] list = resolver.getResources("classpath*:" + path + "/**");

        String suffix = ".class";
        for(Resource res : list) {
            if(res.getFilename().endsWith(suffix)) {
                String fullPath = res.getURL().getFile();
                int idx = fullPath.lastIndexOf(path);
                String className = fullPath.substring(idx, fullPath.length() - suffix.length()).replace("/", ".");
                clazz.add(Class.forName(className));
            }
        }
        return clazz;
    }
}
