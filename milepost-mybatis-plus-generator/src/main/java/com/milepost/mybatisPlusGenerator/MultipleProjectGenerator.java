package com.milepost.mybatisPlusGenerator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 多工程代码生成器<br>
 * 项目结构是多个工程的，即把entity单独放在api工程中供多个工程公用的结构，<br>
 * <br>
 * 使用方法：<br>
 * 1、配置Program arguments，格式如：   --author=zhangsan --driverName=com.mysql.cj.jdbc.Driver ......  <br>
 * 2、参数是一行字符串，每组参数以“--”开始，等号前是key，等号后是value，多组参数之间使用空格分隔<br>
 * 3、参数的key和value含义见 @{@link InputArgUtil}  <br>
 * 4、运行本类时候按照控制台的提示输入。<br>
 */
public class MultipleProjectGenerator {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

//        //作者
//        String author = "huarf";
//        //数据库连接信息
//        String driverName = "com.mysql.cj.jdbc.Driver";
//        String url = "jdbc:mysql://192.168.17.131:3306/milepost_auth?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&serverTimezone=GMT%2B8";
//        String username = "root";
//        String password = "admin123";
//        //数据库类型
//        String dbTypeStr = "mysql";//支持的类型见DbType中的枚举，mysql、oracle。
//        DbType dbType = DbType.getDbType(dbTypeStr);
//        //数据库表名前缀
//        String tablePrefix = "";
//
//        //entity所在的子工程名称，如 example-api
//        String entityProName = "example-api";
//        //sqlMap(除了entity)所在的子工程名称，如 example-service
//        String sqlMapProName = "example-service";
//        //entity所在的父包，如 com.milepost.exampleApi.entity，程序会在com.milepost.exampleApi.entity.${moduleName}下生成实体类
//        String entityPacName = "com.milepost.exampleApi.entity";
//        //sqlMap(除了entity)所在的父包，如 com.milepost.exampleService，程序会在com.milepost.exampleService.${moduleName}下生成dao、service>service.impl、controller
//        String sqlMapPacName = "com.milepost.exampleService";

        //-----------------------以上是相对稳定的变量，需要配置在Program arguments中，以下是相对变化的，需要控制台输入的

        //模块名称，不是代码生成器的模块，是milepost按照模块分包中的模块

        if(args==null || args.length==0){
            System.out.println("[error   ]		请配置Program arguments。");
            return;
        }

        Scanner scanner = null;

        try {

            scanner = new Scanner(System.in);

            System.out.println("[info    ]    控制台收录信息，输入后按回车继续。");
            System.out.println("");

            Map<String, String> inputArgToMap = InputArgUtil.parseInputArgToMap(args);

            System.out.println("[info    ]    Program arguments参数如下：");
            for(Map.Entry<String, String> entry : inputArgToMap.entrySet()){
                System.out.println("[info    ]        " + entry.getKey() + "=" + (entry.getValue().equals("")?"''":entry.getValue()));
            }
            System.out.print("[confirm ]    是否继续？（y/n）：y >");
            String isContinue = scanner.nextLine().trim();
            if(InputArgUtil.isBlank(isContinue) || isContinue.trim().equalsIgnoreCase("y")){
                //继续
                System.out.println("");
            }else{
                System.out.println("[info    ]    程序已终止。");
                return;
            }

            //-----------------------以上是相对稳定的变量，需要配置在Program arguments中，以下是相对变化的，需要控制台输入的

            scanner = new Scanner(System.in);

            System.out.println("[input   ]    输入模块名称，程序会在 ${entityProName} 工程的 ${entityPacName}.${模块名称} 包下生成 entity类，");
            System.out.print(  "              在 ${sqlMapProName} 工程的 ${sqlMapPacName}.${模块名称} 包下生成 dao、service、controller三个包： >");
            String moduleName = scanner.nextLine().trim();//student
            if(InputArgUtil.isBlank(moduleName)){
                System.out.println("[info    ]    模块名称不能为空，程序已终止。");
                return;
            }

            //文件覆盖，如果不覆盖则在原来文件的基础上增加代码
            System.out.print("[confirm ]    是否覆盖已存在得的文件？（y/n）：y >");
            String fileOverride = scanner.nextLine().trim();//com.milepost.exampleSingleBoot.userTest
            if(InputArgUtil.isBlank(fileOverride) || fileOverride.trim().equalsIgnoreCase("y")){
                fileOverride = "true";
            }else{
                fileOverride = "false";
            }

            //针对哪些表生成代码，mp的代码生成器支持多个表的，但是我写的代码不支持多个，
            System.out.print("[input   ]    输入数据库表名称： >");
            String includes = scanner.nextLine().trim();//student
            if(InputArgUtil.isBlank(includes)){
                System.out.println("[info    ]    数据库表名称不能为空，程序已终止。");
                return;
            }

            //控制台输入参数放到map中
            inputArgToMap.put(InputArgUtil.MODULE_NAME, moduleName);
            inputArgToMap.put(InputArgUtil.FILE_OVERRIDE, fileOverride);
            inputArgToMap.put(InputArgUtil.INCLUDES, includes);


            //1. 代码生成器
            AutoGenerator ag = new AutoGenerator();

            //2. 全局配置
            GlobalConfig config = new GlobalConfig();
            config.setActiveRecord(false) // 是否支持AR模式
                    .setAuthor(inputArgToMap.get(InputArgUtil.AUTHOR)) // 作者
//                .setOutputDir(OutputDir) // 生成路径
                    .setFileOverride(Boolean.valueOf(inputArgToMap.get(InputArgUtil.FILE_OVERRIDE)))  // 文件覆盖，如果不覆盖则在原来文件的基础上增加代码
                    .setIdType(IdType.ASSIGN_UUID) // 主键策略，如果字段类型是int，则即使指定了IdType.ASSIGN_UUID也没用，因为IdType.ASSIGN_UUID适用于varchar字段类型，
                    .setServiceName("%sService")  // 设置生成的service接口的名字的首字母是否为I，比如：IEmployeeService，%sService表示前面没有I
                    .setOpen(false)
                    .setBaseResultMap(true)    //sql映射文件中是否生成结果映射
                    .setBaseColumnList(true)
                    .setSwagger2(true);    //sql片段，数据表的列集合。

            //3. 数据源配置
            DataSourceConfig dsConfig = new DataSourceConfig();
            dsConfig.setDbType(DbType.getDbType(inputArgToMap.get(InputArgUtil.DB_TYPE_STR)) )  // 设置数据库类型
                    .setDriverName(inputArgToMap.get(InputArgUtil.DRIVER_NAME))
                    .setUrl(inputArgToMap.get(InputArgUtil.URL))
                    .setUsername(inputArgToMap.get(InputArgUtil.USERNAME))
                    .setPassword(inputArgToMap.get(InputArgUtil.PASSWORD));


            //4. 策略配置
            StrategyConfig stConfig = new StrategyConfig();
            stConfig.setCapitalMode(true) //开启全局大写命名
                    //3.3.2已经没有这个配置了，变成了xxx，2.3版本以后，字段名默认使用下环线命名方式。
                    //.setDbColumnUnderline(true)  // 指定表名 字段名是否使用下划线
                    .setNaming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略，下划线转驼峰命名
                    .setColumnNaming(NamingStrategy.underline_to_camel) //数据库表字段映射到实体的命名策略，如未指定按照 naming
                    .setTablePrefix(inputArgToMap.get(InputArgUtil.TABLE_PREFIX))    //表前缀
                    .setInclude(inputArgToMap.get(InputArgUtil.INCLUDES))    //针对哪些表生成代码
                    .setControllerMappingHyphenStyle(false)  // controller的映射路径驼峰转连字符
                    .setRestControllerStyle(true)   //rest风格的url
                    .setEntityBooleanColumnRemoveIsPrefix(true);    //去掉布尔值的is_前缀

            //5. 包名策略配置
            PackageConfig pkConfig = new PackageConfig();
            pkConfig.setModuleName(null);//默认值为""，需要设置为null，否则controller的路径映射中会多出来一个/

            //6. 整合
            ag.setGlobalConfig(config)
                    .setDataSource(dsConfig)
                    .setStrategy(stConfig)
                    .setPackageInfo(pkConfig)
                    .setTemplateEngine(new FreemarkerTemplateEngine());

            //从java属性中获取项目根目录，如果是maven多模块的结构，则获取的是父工程的根目录，
            //String projectPath = System.getProperty("user.dir");//G:\学习资料\ideaWorkingSpace\springcloud-book-greenwich_milepost-1\example-parent
            String projectPath = "G:\\学习资料\\ideaWorkingSpace\\springcloud-book-greenwich_milepost-1\\example-parent";//G:\学习资料\ideaWorkingSpace\springcloud-book-greenwich_milepost-1\example-parent
            String mavenPath = "\\src\\main\\java\\";

            //5. 自定义代码生成位置，以适应maven多项目情况
            customOutputDir(pkConfig, ag, projectPath, mavenPath,
                    inputArgToMap.get(InputArgUtil.ENTITY_PRO_NAME),
                    inputArgToMap.get(InputArgUtil.SQL_MAP_PRO_NAME),
                    inputArgToMap.get(InputArgUtil.ENTITY_PAC_NAME),
                    inputArgToMap.get(InputArgUtil.SQL_MAP_PAC_NAME),
                    inputArgToMap.get(InputArgUtil.MODULE_NAME));

            //7. 执行
            ag.execute();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(scanner != null){
                scanner.close();
            }
        }
    }

    /**
     * 自定义包路径，文件生成路径，这边配置更灵活
     * 虽然也可以使用InjectionConfig设置FileOutConfig的方式设置路径
     * 这里直接使用Map方式注入ConfigBuilder配置对象更加直观
     *
     {<br>
         'projectRootPath': 'G:/学习资料/ideaWorkingSpace/springcloud-book-greenwich_milepost-1/authentication-parent',	//parent项目路径，这个路径下面有pom、src。<br>
         'javaModelGenerator.targetProject': './authentication-api/src/main/java',	//entity(api)项目相对路径，<br>
         'javaModelGenerator.targetPackage': 'com.milepost.authenticationApi.entity',	//entity包名，<br>
         'sqlMapGenerator.targetProject': './authentication-service/src/main/java',	//dao(service)项目相对路径，<br>
         'sqlMapGenerator.targetPackage': 'com.milepost.authenticationService'	//dao包名，<br>
     }<br>
     *
     * @param pc
     * @param mpg
     * @param projectPath   从java属性中获取的项目根目录，如果是maven多模块的结构，则获取的是父工程的根目录，
     * @param mavenPath  mvn路径，即"\src\main\java\"
     * @param entityProName  entity所在的子工程名称，如 example-api
     * @param sqlMapProName  sqlMap(除了entity)所在的子工程名称，如 example-service
     * @param entityPacName  entity所在的父包，如 com.milepost.exampleApi.entity，程序会在com.milepost.exampleApi.entity.${moduleName}下生成实体类
     * @param sqlMapProName  sqlMap(除了entity)所在的父包，如 com.milepost.exampleService，程序会在com.milepost.exampleService.${moduleName}下生成dao、service>service.impl、controller
     * @param moduleName  模块名称，不是代码生成器的模块，是milepost按照模块分包中的模块
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static void customOutputDir(PackageConfig pc, AutoGenerator mpg,
                                       String projectPath, String mavenPath,
                                       String entityProName, String sqlMapProName,
                                       String entityPacName, String sqlMapPacName,
                                       String moduleName) throws NoSuchFieldException, IllegalAccessException {

        /**
         * packageInfo配置controller、service、serviceImpl、entity、mapper等文件的包路径
         * 这里包路径可以根据实际情况灵活配置
         */
        Map<String,String> packageInfo = new HashMap<>();
        packageInfo.put(ConstVal.CONTROLLER, sqlMapPacName + "." + moduleName + ".controller");
        packageInfo.put(ConstVal.SERVICE, sqlMapPacName + "." + moduleName + ".service");
        packageInfo.put(ConstVal.SERVICE_IMPL, sqlMapPacName + "." + moduleName + ".service.impl");
        packageInfo.put(ConstVal.ENTITY, entityPacName + "." + moduleName);
        packageInfo.put(ConstVal.MAPPER, sqlMapPacName + "." + moduleName + ".dao");

        /**
         * pathInfo配置controller、service、serviceImpl、entity、mapper、mapper.xml等文件的生成路径
         * srcPath也可以更具实际情况灵活配置
         * 后面部分的路径是和上面packageInfo包路径对应的源码文件夹路径
         * 这里你可以选择注释其中某些路径，可忽略生成该类型的文件，例如:注释掉下面pathInfo中Controller的路径，就不会生成Controller文件
         */
        Map pathInfo = new HashMap<>();
        pathInfo.put(ConstVal.CONTROLLER_PATH,      projectPath + "\\" + sqlMapProName + mavenPath + packageInfo.get(ConstVal.CONTROLLER).replaceAll("\\.", StringPool.BACK_SLASH + File.separator));
        pathInfo.put(ConstVal.SERVICE_PATH,         projectPath + "\\" + sqlMapProName + mavenPath + packageInfo.get(ConstVal.SERVICE).replaceAll("\\.", StringPool.BACK_SLASH + File.separator));
        pathInfo.put(ConstVal.SERVICE_IMPL_PATH,    projectPath + "\\" + sqlMapProName + mavenPath + packageInfo.get(ConstVal.SERVICE_IMPL).replaceAll("\\.", StringPool.BACK_SLASH + File.separator));
        pathInfo.put(ConstVal.ENTITY_PATH,          projectPath + "\\" + entityProName + mavenPath + packageInfo.get(ConstVal.ENTITY).replaceAll("\\.", StringPool.BACK_SLASH + File.separator));
        pathInfo.put(ConstVal.MAPPER_PATH,          projectPath + "\\" + sqlMapProName + mavenPath + packageInfo.get(ConstVal.MAPPER).replaceAll("\\.", StringPool.BACK_SLASH + File.separator));
        pathInfo.put(ConstVal.XML_PATH,             projectPath + "\\" + sqlMapProName + mavenPath + packageInfo.get(ConstVal.MAPPER).replaceAll("\\.", StringPool.BACK_SLASH + File.separator));
        pc.setPathInfo(pathInfo);

        /**
         * 创建configBuilder对象，传入必要的参数
         * 将以上的定义的包路径packageInfo配置到赋值到configBuilder对象的packageInfo属性上
         * 因为packageInfo是私有成员变量，也没有提交提供公共的方法，所以使用反射注入
         * 为啥要这么干，看源码去吧
         */
        ConfigBuilder configBuilder = new ConfigBuilder(mpg.getPackageInfo(), mpg.getDataSource(), mpg.getStrategy(), mpg.getTemplate(), mpg.getGlobalConfig());
        Field packageInfoField = configBuilder.getClass().getDeclaredField("packageInfo");
        packageInfoField.setAccessible(true);
        packageInfoField.set(configBuilder,packageInfo);

        /**
         * 设置配置对象
         */
        mpg.setConfig(configBuilder);
    }
}
