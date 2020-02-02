package com.milepost.mybatisGenerator;

import com.milepost.api.util.JsonUtil;
import com.milepost.api.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.*;
import java.util.*;

/**
 * 多项目的生成器，entity被抽取到api项目中的。<br>
 *
 * 生成mapper映射文件、实体类、查询条件实体类。<br>
 * 使用方法：<br>
 *  1.新建在service中mbg、dao、service、controller包，在api中新建entity包。<br>
 *  2.将MybatisMbg.xml放到com.milepost.**.mbg下。<br>
 *  3.配置MybatisMbg.xml中的jdbc、table。<br>
 *  4.配置Program arguments，为json字符串，包含以下4个k-v，注意必须是单引号，而不是双引号：<br>
		{<br>
		 	'projectRootPath': 'G:/学习资料/ideaWorkingSpace/springcloud-book-greenwich_milepost-1/authentication-parent',	//parent项目路径，这个路径下面有pom、src。<br>
 			'javaModelGenerator.targetProject': './authentication-api/src/main/java',	//entity(api)项目相对路径，<br>
			'javaModelGenerator.targetPackage': 'com.milepost.authenticationApi.entity',	//entity包名，<br>
			'sqlMapGenerator.targetProject': './authentication-service/src/main/java',	//dao(service)项目相对路径，<br>
			'sqlMapGenerator.targetPackage': 'com.milepost.authenticationService'	//dao包名，<br>
		}<br>
 * 	5.用运行main方法，按照中提示的进行操作。<br>
 * 	6.对于authentication-service工程的user，输入user。<br>
 * 	7.当原来存在文件的时候，给出提示并原备份。<br>
 * 	8.每次生成新的mbg配置文件，覆盖原来的mbg配置文件。<br>
 */
public class MyBatisMbgMultipleProject {
	
	public static void main(String[] args) throws Exception {

		if(args==null || args.length==0){
			System.out.println("[error]		请配置Program arguments。");
			return;
		}

		String arg = args[0];
		arg = arg.replace("'", "\"");
		Map<String, Object> argMap = JsonUtil.jsonObject2Map(arg);

		//Project参数不为空，必须配置在Program arguments中
		String jmgTargetProject = (String) argMap.get("javaModelGenerator.targetProject");
		String smgTargetProject = (String) argMap.get("sqlMapGenerator.targetProject");

		//Package在Program arguments中配置一部分，用户输入一部分
		String jmgTargetPackage = (String) argMap.get("javaModelGenerator.targetPackage");
		String smgTargetPackage = (String) argMap.get("sqlMapGenerator.targetPackage");

		//projectRootPath参数不为空，必须配置在Program arguments中
		String projectRootPath = (String) argMap.get("projectRootPath");
		if(StringUtils.isBlank(projectRootPath)){
			System.out.println("[error]		Program arguments中必须包含projectRootPath。");
			return;
		}
		if(StringUtils.isBlank(jmgTargetProject)){
			System.out.println("[error]		Program arguments中必须包含javaModelGenerator.targetProject。");
			return;
		}
		if(StringUtils.isBlank(smgTargetProject)){
			System.out.println("[error]		Program arguments中必须包含sqlMapGenerator.targetProject。");
			return;
		}
		if(StringUtils.isBlank(jmgTargetPackage)){
			System.out.println("[error]		Program arguments中必须包含javaModelGenerator.targetPackage。");
			return;
		}
		if(StringUtils.isBlank(smgTargetPackage)){
			System.out.println("[error]		Program arguments中必须包含sqlMapGenerator.targetPackage。");
			return;
		}

		Scanner scanner = null;
		InputStream entityIs = null;
		OutputStream mbgOs = null;


		InputStream oldConfigInputStream = null;
		InputStream newConfigInputStream = null;
		XMLWriter xmlWriter = null;

		try {
			scanner = new Scanner(System.in);

			System.out.println("[info]		请输入模块包名，即com.milepost的下级包，支持多级包名，如user、workflow.user等，输入后按回车继续。");
			System.out.print("[input]		模块包名： >");
			String inputPackName = scanner.nextLine().trim();//	aa.bb
			String inputPackDir = inputPackName.replace(".", "/");//	aa/bb

			//模块名称，一层包时候，modleName=packName，多层包时，modleName=最后一层的包名
			String modleName = (inputPackName.contains(".")? inputPackName.substring(inputPackName.lastIndexOf(".")+1) : inputPackName);
			String nowTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd_HH-mm-ss");

			//获取bean、dao目录
			String entityDir = projectRootPath +
					jmgTargetProject.substring(1) + "/" +
					jmgTargetPackage.replace(".", "/") + "/" +
					inputPackDir;
			String newJmgTargetPackage = jmgTargetPackage + "." + inputPackName;

			String daoDir = projectRootPath +
					smgTargetProject.substring(1) + "/" +
					smgTargetPackage.replace(".", "/") + "/" +
					inputPackDir + "/dao";
			String newSmgTargetPackage = smgTargetPackage + "." + inputPackName + ".dao";

			String modleDir = projectRootPath +
					smgTargetProject.substring(1) + "/" +
					smgTargetPackage.replace(".", "/") + "/" +
					inputPackDir;


			String mbgDir = projectRootPath +
					smgTargetProject.substring(1) + "/" +
					smgTargetPackage.replace(".", "/") + "/" +
					inputPackDir + "/" + "mbg";

			File entityFile = new File(entityDir);
			if(!entityFile.isDirectory()){
				System.out.println("[info]		没找到entity包，您输入的模块包名可能有错误，已终止。");
				return;
			}
			File daoFile = new File(daoDir);
			if(!daoFile.isDirectory()){
				System.out.println("[info]		没找到dao包，您输入的模块包名可能有错误，已终止。");
				return;
			}

			//将entity剪切到dao中
			File[] entityFileList = entityFile.listFiles();
			if(entityFileList!=null && entityFileList.length>0){
				System.out.print("[confirm]	已存在mbg生成的文件，是否对旧文件进行备份？（y/n）：y >");
				String backup = scanner.nextLine().trim();
				backup = "".equals(backup)? "y" : backup;
				if("y".equals(backup)){
					ZipUtil.zipFolder(entityDir, mbgDir + "/entity.zip");
					for(File fileTemp : entityFileList){
						FileUtils.deleteQuietly(fileTemp);
					}
					//备份
					ZipUtil.zipFolder(modleDir, mbgDir + "/" + modleName + "_"+ nowTime +".zip");
					//先删除原来mapper的文件，否则mbg会在原文件上追加内容
					for(File fileTemp : daoFile.listFiles()){
						FileUtils.deleteQuietly(fileTemp);
					}
					FileUtils.deleteQuietly(new File(mbgDir + "/entity.zip"));
				}
			}else{
				System.out.println("[info]		不存在mbg生成的文件，无需备份。");
			}

			//动态改变xml
			oldConfigInputStream = new FileInputStream(mbgDir +"/MybatisMbg.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(oldConfigInputStream);
			Element rootElm = document.getRootElement();
			Element contextEle = rootElm.element("context");
			Element javaModelEle = contextEle.element("javaModelGenerator");
			javaModelEle.addAttribute("targetPackage", newJmgTargetPackage);
			javaModelEle.addAttribute("targetProject", jmgTargetProject);

			Element sqlMapEle = contextEle.element("sqlMapGenerator");
			sqlMapEle.addAttribute("targetPackage", newSmgTargetPackage);
			sqlMapEle.addAttribute("targetProject", smgTargetProject);

			Element tableEle = contextEle.element("table");
//			System.out.println("[info]		注意table配置信息如下：");
//			System.out.println("[info]		<table tableName=\""+ tableEle.attributeValue("tableName") +"\" domainObjectName=\""+ tableEle.attributeValue("domainObjectName") +"\">");
			System.out.println("[info]		注意如下配置：");
			System.out.println("[info]		数据库：" + tableEle.attributeValue("catalog") + "，" +
					"表名：" + tableEle.attributeValue("tableName") + "，" +
					"实体类：" + tableEle.attributeValue("domainObjectName"));

			System.out.print("[confirm]	请确认（y/n）：y >");
			String confirm = scanner.nextLine().trim();
			confirm = confirm.equals("")? "y" : confirm;//默认值y
			if("n".equals(confirm)){
				System.out.println("[info]		已终止。");
				return;
			}

			//将新xml写入本地磁盘
			OutputFormat format = new OutputFormat("	", true);// 设置缩进为4个空格，并且另起一行为true
	        xmlWriter = new XMLWriter(new FileOutputStream(mbgDir +"/MybatisMbg.xml"), format);
	        xmlWriter.write(document);
	        xmlWriter.flush();
	        xmlWriter.close();
	        //读取新配置文件，生成代码
	        newConfigInputStream = new FileInputStream(mbgDir +"/MybatisMbg.xml");

			List<String> warnings = new ArrayList<String>();
			boolean overwrite = true;
			ConfigurationParser cp = new ConfigurationParser(warnings);
			Configuration config = cp.parseConfiguration(newConfigInputStream);

			DefaultShellCallback callback = new DefaultShellCallback(overwrite);
			MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config,callback, warnings);
			myBatisGenerator.generate(null);
			printLog(warnings);

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			//关闭流
			IOUtils.closeQuietly(entityIs);
			IOUtils.closeQuietly(mbgOs);


			IOUtils.closeQuietly(scanner);
			IOUtils.closeQuietly(oldConfigInputStream);
			IOUtils.closeQuietly(newConfigInputStream);
			if(xmlWriter != null){
				try {
					xmlWriter.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 打印日志
	 * @param warnings
	 */
	private static void printLog(List<String> warnings) {
		System.out.println("[LOG] ---------------------------------------------");
		System.out.println("Warnings: " + warnings.size());
		for(String warning : warnings){
			System.out.println(warning);
		}
		System.out.println("");
	}

}
