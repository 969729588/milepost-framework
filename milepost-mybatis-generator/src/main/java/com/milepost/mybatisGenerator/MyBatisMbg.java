package com.milepost.mybatisGenerator;

import com.milepost.api.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * @author Huarf
 * 生成mapper映射文件、mapper接口、实体类、查询条件实体类，
 * 使用方法：
 * 	1.将MybatisMbg.xml放到com.milepost.xxx.mbg下。
 * 	2.用运行main方法，按照中提示的进行操作。
 * 	3.当原来存在文件的时候，要给出提示并原备份。
 * 	4.每次生成，要覆盖原来的mbg配置文件。
 * 2017年8月22日
 *
 *
 * 对于student，输入以下：authenticationService.student
 *
 */
public class MyBatisMbg {
	
	public static void main(String[] args) throws Exception {

		//在Program arguments中配置一个参数，即当前项目的绝对跟路径
		//G:\学习资料\ideaWorkingSpace\springcloud-book-greenwich_milepost-1\authentication-parent\authentication-service
		//这个路径下面就是pom、src。
		if(args==null || args.length==0){
			System.out.println("[error]		请配置Program arguments。");
			return;
		}

		String projectRootPath = args[0];

		Scanner scanner = null;
		InputStream oldConfigInputStream = null;
		InputStream newConfigInputStream = null;
		XMLWriter xmlWriter = null;
		
		try {
			scanner = new Scanner(System.in);
			
			System.out.println("[info]		请输入模块包名，即com.milepost的下级包，支持多级包名，如user、workflow.user等，输入后按回车继续。");
			System.out.print("[input]		模块包名： >");
			String packName = scanner.nextLine().trim();
			
			//模块名称，一层包时候，modleName=packName，多层包时，modleName=最后一层的包名
			String modleName = (packName.contains(".")? packName.substring(packName.lastIndexOf(".")+1) : packName);
			
			//压缩备份旧文件
			//通过启动参数配置进来了
			//String projectRootPath = System.getProperty("user.dir");//D:\JavaSoftware\eclipse-mars-RWorkspace\workspace2\${projectName}
			String eclipseDir = "/src/main/java/com/milepost";
			File entityFile = new File(projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName) +"/entity");;
			
			if(!entityFile.isDirectory()){
				System.out.println("[info]		没找到包，您输入的模块包名可能有错误，已终止。");
				return;
			}
			File[] entityFileList = entityFile.listFiles();
			
			//只要entity中存在文件就备份整个模块包
			if(entityFileList!=null && entityFileList.length>0){
				System.out.print("[confirm]	已存在mbg生成的文件，是否对旧文件进行备份？（y/n）：y >");
				String backup = scanner.nextLine().trim();
				backup = "".equals(backup)? "y" : backup;
				if("y".equals(backup)){
					String folderPath = projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName);//整个模块包
					String zipFilePath = projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName) + "/mbg";
					ZipUtil.zip(folderPath, zipFilePath + "/" + modleName + "_"+ DateFormatUtils.format(new Date(), "yyyy-MM-dd_HH-mm-ss") +".zip");
				}
				//先删除原来mapper的文件，否则mbg会在原文件上追加内容
				String mapperFilePath = projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName)
						+"/dao/"+ modleName.substring(0, 1).toUpperCase() + modleName.substring(1) +"Mapper.xml";
				FileUtils.deleteQuietly(new File(mapperFilePath));
			}else{
				System.out.println("[info]		不存在mbg生成的文件，无需备份。");
			}
			
			//动态改变xml
			String entityTargetPackage = "com.milepost."+ packName +".entity";
			String daoTargetPackage = "com.milepost."+ packName +".dao";
			//oldConfigInputStream = Resources.getResourceAsStream("com/milepost/"+ (packName.contains(".")? packName.replace(".", "/") : packName) +"/mbg/MybatisMbg.xml");
			oldConfigInputStream = new FileInputStream(projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName) +"/mbg/MybatisMbg.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(oldConfigInputStream);
			Element rootElm = document.getRootElement();
			Element contextEle = rootElm.element("context");
			Element javaModelEle = contextEle.element("javaModelGenerator");
			javaModelEle.addAttribute("targetPackage", entityTargetPackage);
			Element sqlMapEle = contextEle.element("sqlMapGenerator");
			sqlMapEle.addAttribute("targetPackage", daoTargetPackage);
			Element tableEle = contextEle.element("table");
			System.out.println("[info]		注意table配置信息如下：");
			System.out.println("[info]		<table tableName=\""+ tableEle.attributeValue("tableName") +"\" domainObjectName=\""+ tableEle.attributeValue("domainObjectName") +"\">");
			System.out.print("[confirm]	请确认（y/n）：y >");
			String confirm = scanner.nextLine().trim();
			confirm = confirm.equals("")? "y" : confirm;//默认值y
			if("n".equals(confirm)){
				System.out.println("[info]		已终止。");
				return;
			}

			//将新xml写入本地磁盘
			OutputFormat format = new OutputFormat("	", true);// 设置缩进为4个空格，并且另起一行为true
	        xmlWriter = new XMLWriter(new FileOutputStream(projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName) +"/mbg/MybatisMbg.xml"), format);
	        xmlWriter.write(document);
	        xmlWriter.flush();
	        xmlWriter.close();
	        //读取新配置文件，生成代码
	        //newConfigInputStream = Resources.getResourceAsStream("com/milepost/"+ packName +"/mbg/MybatisMbg.xml");//这样不能读取到本次修改过的配置文件。
	        newConfigInputStream = new FileInputStream(projectRootPath + eclipseDir + "/" + (packName.contains(".")? packName.replace(".", "/") : packName) +"/mbg/MybatisMbg.xml");
	        
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
