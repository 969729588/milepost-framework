package com.milepost.api.util;

import org.apache.commons.lang.StringUtils;

/**
 * Created by Ruifu Hua on 2020/2/9.
 * 命名方式格式化工具类
 */
public class NamingFormatUtil {

    /**
     * 将下划线风格替换为驼峰风格
     * @param src
     * @return
     */
    public static String underline2Camelhump(String src) {

        if(StringUtils.isBlank(src)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        boolean nextUpperCase = false;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '_') {
                if (sb.length() > 0) {
                    nextUpperCase = true;
                }
            } else {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将驼峰风格替换为下划线风格
     * @param src
     * @param upperCase 是否为大写，true大写，否则小写
     * @return
     */
    private static String camelhump2Underline(String src, Boolean upperCase) {

        if(StringUtils.isBlank(src)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c>='A' && c<='Z') {
                if(upperCase){
                    sb.append("_" + Character.toUpperCase(c));
                }else{
                    sb.append("_" + Character.toLowerCase(c));
                }
            } else {
                if(upperCase){
                    sb.append(Character.toUpperCase(c));
                }else{
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 将驼峰风格替换为大写的划线风格
     * @param src
     * @return
     */
    public static String camelhump2UpperCaseUnderline(String src) {
        return camelhump2Underline(src, true);
    }

    /**
     * 将驼峰风格替换为小写的划线风格
     * @param src
     * @return
     */
    public static String camelhump2LowerCaseUnderline(String src) {
        StringUtils.isNotBlank("");
        return camelhump2Underline(src, false);
    }
}
