package com.milepost.core.banner;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.ResourceBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Created by Ruifu Hua on 2020/1/14.
 * 打印banner，禁止springboot的打印, prod打印到文件, dev打印到控制台。
 */
public class PrintBanner {
    private SpringApplication application;
    private ConfigurableEnvironment environment;

    private static Logger applicationLogger = null;
    private static Boolean printBanner = false;

    private static final String BANNER_LOCATION_PROPERTY = "spring.banner.location";
    private static final String DEFAULT_BANNER_LOCATION = "banner.txt";
    private static final String SPRING_PROFILES_ACTIVE_DEV = "dev";
    private static final String SPRING_PROFILES_ACTIVE_PROD = "prod";


    public PrintBanner(SpringApplication application, ConfigurableEnvironment environment) {
        this.application = application;
        this.environment = environment;
        applicationLogger = LoggerFactory.getLogger(application.getClass());
    }

    public void print() {
        if(!PrintBanner.printBanner){
            //标志，禁止打印多次
            PrintBanner.printBanner = true;

            //获取banner
            Banner banner = getBanner(environment);

            //打印banner
            printBanner(banner, environment);
        }
    }

    private Banner getBanner(ConfigurableEnvironment environment) {
        Banner banner = getTextBanner(environment);
        if(banner == null){
            banner = new SpringBootDefaultBanner();
        }
        return banner;
    }

    private void printBanner(Banner banner, ConfigurableEnvironment environment) {
        ByteArrayOutputStream baos = null;
        PrintStream printStream = null;
        try {
            String springProfilesActive = environment.getProperty("spring.profiles.active");
            baos = new ByteArrayOutputStream();
            printStream = new PrintStream(baos);
            banner.printBanner(environment, application.getClass(), printStream);
            String charset = environment.getProperty("spring.banner.charset", "UTF-8");
            if(SPRING_PROFILES_ACTIVE_PROD.equalsIgnoreCase(springProfilesActive)){
                applicationLogger.info(baos.toString(charset));
            }else if(SPRING_PROFILES_ACTIVE_DEV.equalsIgnoreCase(springProfilesActive)){
                System.out.println(baos.toString(charset));
            }
        }catch (Exception e){
            applicationLogger.error(e.getMessage(), e);
        }finally {
            IOUtils.closeQuietly(printStream);
            IOUtils.closeQuietly(baos);
        }
    }

    private Banner getTextBanner(Environment environment) {
        //获取自定义的banner，就是类路径下名称为banner.txt的文件，
        String location = environment.getProperty(BANNER_LOCATION_PROPERTY, DEFAULT_BANNER_LOCATION);
        ResourceLoader resourceLoader = new DefaultResourceLoader(ClassUtils.getDefaultClassLoader());
        Resource resource = resourceLoader.getResource(location);
        if (resource.exists()) {
            return new ResourceBanner(resource);
        }
        return null;
    }
}

class SpringBootDefaultBanner implements Banner {

    private final String[] BANNER = { "",
            "  .   ____          _            __ _ _",
            " /\\\\ / ___'_ __ _ _(_)_ __  __ _ \\ \\ \\ \\",
            "( ( )\\___ | '_ | '_| | '_ \\/ _` | \\ \\ \\ \\",
            " \\\\/  ___)| |_)| | | | | || (_| |  ) ) ) )",
            "  '  |____| .__|_| |_|_| |_\\__, | / / / /",
            " =========|_|==============|___/=/_/_/_/" };

    private static final String SPRING_BOOT = " :: Spring Boot :: ";

    private static final int STRAP_LINE_SIZE = 42;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass,
                            PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }
        String version = SpringBootVersion.getVersion();
        version = (version != null) ? " (v" + version + ")" : "";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAP_LINE_SIZE
                - (version.length() + SPRING_BOOT.length())) {
            padding.append(" ");
        }

        printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT,
                AnsiColor.DEFAULT, padding.toString(), AnsiStyle.FAINT, version));
        printStream.println();
    }

}