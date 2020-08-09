package com.milepost.mybatisPlusGenerator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Map;
import java.util.Scanner;

/**
 * 单工程代码生成器<br>
 * 项目结构是单一工程的，即没有把entity单独放在api工程中供多个工程公用的结构，<br>
 * <br>
 * 使用方法：<br>
 * 1、配置Program arguments，格式如：   --author=zhangsan --driverName=com.mysql.cj.jdbc.Driver ......  <br>
 * 2、参数是一行字符串，每组参数以“--”开始，等号前是key，等号后是value，多组参数之间使用空格分隔<br>
 * 3、参数的key和value含义见 @{@link InputArgUtil}  <br>
 * 4、运行本类时候按照控制台的提示输入。<br>
 */
public class SingleProjectGenerator {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
//        //作者
//        String author = "huarf";
//        //数据库连接信息
//        String driverName = "com.mysql.cj.jdbc.Driver";
//        String url = "jdbc:mysql://192.168.17.131:3306/example_single_boot?useUnicode=true&characterEncoding=utf8&characterSetResults=utf8&serverTimezone=GMT%2B8";
//        String username = "root";
//        String password = "admin123";
//        //数据库类型
//        String dbTypeStr = "mysql";//支持的类型见DbType中的枚举，mysql、oracle。
//        DbType dbType = DbType.getDbType(dbTypeStr);
//        //数据库表名前缀
//        String tablePrefix = "";
//        //代码生成路径
//        String proRootDir = "G:\\学习资料\\ideaWorkingSpace\\springcloud-book-greenwich_milepost-1\\example-parent\\example-single-boot";
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

            System.out.print("[input   ]    输入包名，程序会在你输入的包下生成 entity、dao、service、controller四个包： >");
            String parentPackage = scanner.nextLine().trim();//com.milepost.exampleSingleBoot.userTest
            if(InputArgUtil.isBlank(parentPackage)){
                System.out.println("[info    ]    包名不能为空，程序已终止。");
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
            String includes = scanner.nextLine().trim();//user
            if(InputArgUtil.isBlank(includes)){
                System.out.println("[info    ]    数据库表名称不能为空，程序已终止。");
                return;
            }

            //控制台输入参数放到map中
            inputArgToMap.put(InputArgUtil.PARENT_PACKAGE, parentPackage);
            inputArgToMap.put(InputArgUtil.FILE_OVERRIDE, fileOverride);
            inputArgToMap.put(InputArgUtil.INCLUDES, includes);

            //1. 代码生成器
            AutoGenerator ag = new AutoGenerator();

            //2. 全局配置
            String mavenPath = "\\src\\main\\java";
            String outputDir = inputArgToMap.get(InputArgUtil.PRO_ROOT_DIR) + mavenPath;
            GlobalConfig config = new GlobalConfig();
            config.setActiveRecord(false) // 是否支持AR模式
                    .setAuthor(inputArgToMap.get(InputArgUtil.AUTHOR)) // 作者
                    .setOutputDir(outputDir) // 生成路径
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
            pkConfig.setParent(inputArgToMap.get(InputArgUtil.PARENT_PACKAGE))
                    .setModuleName(null)//默认值为""，需要设置为null，否则controller的路径映射中会多出来一个/
                    .setMapper("dao")
                    .setService("service")
                    .setController("controller")
                    .setEntity("entity")
                    .setXml("dao");

            //6. 整合
            ag.setGlobalConfig(config)
                    .setDataSource(dsConfig)
                    .setStrategy(stConfig)
                    .setPackageInfo(pkConfig)
                    .setTemplateEngine(new FreemarkerTemplateEngine());

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

}
