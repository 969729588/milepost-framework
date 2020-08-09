package com.milepost.mybatisPlusGenerator;

import sun.swing.plaf.synth.DefaultSynthStyle;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Ruifu Hua on 2020/8/9.
 * 输入参数工具类
 */
public class InputArgUtil {

    //以下是用户输入参数的key，用户必须按照下面的key传入输入参数，格式如下：
    //  --author=zhangsan --driverName=com.mysql.cj.jdbc.Driver ......

    //作者
    public static final String AUTHOR = "author";

    //数据库连接信息
    public static final String DRIVER_NAME = "driverName";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    //数据库类型，支持的类型见DbType中的枚举，常用的是 mysql、oracle。
    public static final String DB_TYPE_STR = "dbTypeStr";
    //数据库表名前缀
    public static final String TABLE_PREFIX = "tablePrefix";

    //单一项目的代码生成位置
    //代码所在的工程根目录，如 G:\\学习资料\\ideaWorkingSpace\\springcloud-book-greenwich_milepost-1\\example-parent\\example-single-boot
    public static final String PRO_ROOT_DIR = "proRootDir";
    //代码父包，会在这个包下生成 entity、dao、service>service.impl、controller
    public static final String PARENT_PACKAGE = "parentPackage";

    //多项目的代码生成位置
    //entity所在的子工程名称，如 example-api
    public static final String ENTITY_PRO_NAME = "entityProName";
    //sqlMap(除了entity)所在的子工程名称，如 example-service
    public static final String SQL_MAP_PRO_NAME = "sqlMapProName";
    //entity所在的父包，如 com.milepost.exampleApi.entity，程序会在com.milepost.exampleApi.entity.${moduleName}下生成实体类
    public static final String ENTITY_PAC_NAME = "entityPacName";
    //sqlMap(除了entity)所在的父包，如 com.milepost.exampleService，程序会在com.milepost.exampleService.${moduleName}下生成dao、service>service.impl、controller
    public static final String SQL_MAP_PAC_NAME = "sqlMapPacName";
    //模块名称，不是代码生成器的模块，是milepost按照模块分包中的模块
    public static final String MODULE_NAME = "moduleName";

    //文件覆盖，如果不覆盖则在原来文件的基础上增加代码
    public static final String FILE_OVERRIDE = "fileOverride";
    //针对哪些表生成代码，//多个表名之间用逗号分隔
    public static final String INCLUDES = "includes";


    /**
     * 解析用户输入参数到map中
     * @param inputArgArray
     * @return
     */
    public static Map<String, String> parseInputArgToMap(String[] inputArgArray){
        //存放输入参数，所有参数类型都是String，均没有默认值，不传就是null
        Map<String, String> inputArgMap = new LinkedHashMap<>();

        for(String inputArg : inputArgArray){
            inputArg = Pattern.compile("^--").matcher(inputArg).replaceAll("");
            String key = inputArg.substring(0, inputArg.indexOf("="));
            String value = inputArg.substring(inputArg.indexOf("=")+1);
            inputArgMap.put(key.trim(), value.trim().equals("''")? "" : value.trim());
        }

        return inputArgMap;
    }

    /**
     * 字符串是否为空，是否为空白
     * @param str
     * @return
     */
    public static boolean isBlank(String str){
        if(str==null || str.trim().equals("")){
            return true;
        }else{
            return false;
        }
    }

}
