package com.milepost.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.core.spring.ApplicationContextProvider;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import io.swagger.annotations.Authorization;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Ruifu Hua on 2019/12/30.
 */
public class MilepostApplication extends SpringApplication{

    private static Logger logger = LoggerFactory.getLogger(MilepostApplication.class);
    private Map<String, Object> customProperties;
    private Map<String, Object> argsMap;
    private static ConfigurableApplicationContext context;
    public static Boolean started;
    public static final String defaultPassword = "milepost";

    /**
     * 这里面不能注入ioc容器中的bean，因为还没初始化呢
     * @param primarySources
     * @param customProperties
     */
//    @Autowired(required = false)
//    private DiscoveryClient discoveryClient;

    public MilepostApplication(Class<?>[] primarySources, Map<String, Object> customProperties) {
        super(primarySources);
        this.customProperties = customProperties;
    }

    public MilepostApplication(){
        super(new Class[0]);
    }

    public static ConfigurableApplicationContext run(Map<String, Object> customProperties, Class<?> primarySource, String... args) {
        MilepostApplication milepostApplication = new MilepostApplication(new Class[]{primarySource}, customProperties);
        ConfigurableApplicationContext context = milepostApplication.run(args);
        return context;
    }

    /**
     * springboot加载配置项的顺序是  命令行参数   >   java系统属性(System.getProperties())    >   操作系统环境变量(System.getenv())   >   配置文件
     * 对于一些自己定义的属性，在逻辑处理过程中，要注意这个优先级。
     * @param args
     * @return
     */
    public ConfigurableApplicationContext run(String... args) {
        try {
            //启动之前
            this.runBefore();
            //解析命令行参数，主要用于将应用发布到k8s集群中，获取浮动ip。
            List<String> argsArray = this.resolverArgs(args);
            //加载默认属性，加载时把this.customProperties  加载进去，并调用SpringApplications.setDefaultProperties方法设置进去
            Map<String, Object> defaultProperties = this.loadDefaultProperties();

            //设置默认属性
            this.setDefaultProperties(defaultProperties);
            //启动
            context = super.run((String[])argsArray.toArray(new String[0]));

            //处理EurekaServer和非EurekaServer
            boolean isEurekaServer = this.isEurekaServer();
            if(isEurekaServer) {
                //

            } else {
                //
            }

            //启动之后
            this.runAfter();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
        }
        return context;
    }

    /**
     * 解析args(命令行参数)，主要用于将应用发布到k8s集群中，获取浮动ip。
     * 当应用被发布到k8s中时，具体被k8s调度到集群中的哪台机器上是不可确定的，
     * 所以这种情况下要求配置一个eureka.instance.ip-address.prefix命令行参数，其值如“192.168.55”，即去掉最后一个ip段，
     * 这个方法中根据eureka.instance.ip-address.prefix获取容器所在机器的ip地址，用这个ip地址来覆盖eureka.instance.ip-address
     * @param args
     * @return
     */
    private List<String> resolverArgs(String... args) throws Exception {
        List<String> argsList = Arrays.asList(args);
        logger.info("before:argsList -> " + String.valueOf(argsList));
        List<String> newArgsList = new LinkedList();
        newArgsList.addAll(argsList);
        this.argsMap = argsToMap(args);
        logger.info("before:argsMap -> " + String.valueOf(this.argsMap));

        Object eurekaInstanceIpAddressPrefix;
        //以下获取eurekaInstanceIpAddressPrefix参数值的顺序必须符合springboot加载配置项的顺序，
        // 即    命令行参数   >   java系统属性(System.getProperties())    >   操作系统环境变量(System.getenv())   >   配置文件
        if(this.argsMap.containsKey("eureka.instance.ip-address.prefix")) {
            //从命令行参数中取
            eurekaInstanceIpAddressPrefix = this.argsMap.get("eureka.instance.ip-address.prefix");
        } else {
            //从操作系统环境变量中取
            eurekaInstanceIpAddressPrefix = System.getenv("eureka.instance.ip-address.prefix");
        }

        //当获取到eurekaInstanceIpAddressPrefix后并且命令行参数中不包含eureka.instance.ip-address
        //则根据eurekaInstanceIpAddressPrefix获取k8s将本应用所调度到的机器的ip
        if(eurekaInstanceIpAddressPrefix != null && !this.argsMap.containsKey("eureka.instance.ip-address")) {
            String eurekaInstanceIpAddress = getEurekaInstanceIpAddress(eurekaInstanceIpAddressPrefix.toString());
            newArgsList.add("--eureka.instance.ip-address=" + eurekaInstanceIpAddress);
            logger.info("eureka.instance.ip-address 绑定 {}", eurekaInstanceIpAddress);
        }

        return newArgsList;
    }

    /**
     * 根据ip前缀(192.168.55)获取真是ip
     * @param eurekaInstanceIpAddressPrefix
     * @return
     * @throws Exception
     */
    private static String getEurekaInstanceIpAddress(String eurekaInstanceIpAddressPrefix) throws Exception {
        logger.info("检测本机IP地址");
        Enumeration ips = NetworkInterface.getNetworkInterfaces();

        while(ips.hasMoreElements()) {
            NetworkInterface ifs = (NetworkInterface)ips.nextElement();
            Enumeration addresss = ifs.getInetAddresses();

            while(addresss.hasMoreElements()) {
                InetAddress address = (InetAddress)addresss.nextElement();
                String ip = address.getHostAddress();
                logger.info(ip);
                if(ip.startsWith(eurekaInstanceIpAddressPrefix)) {
                    return ip;
                }
            }
        }

        throw new Exception("eureka.instance.ip-address.prefix=" + eurekaInstanceIpAddressPrefix + "无法匹配本服务器IP");
    }

    /**
     * 数组形式的args转化成map形式
     * @param args
     * @return
     */
    private static Map<String, Object> argsToMap(String[] args) {
        LinkedHashMap<String, Object> map = new LinkedHashMap();
        String[] argsTemp = args;
        int argsLength = args.length;

        for(int i = 0; i < argsLength; ++i) {
            String arg = argsTemp[i];
            if(arg != null) {
                String key = arg.substring(0, arg.indexOf(61));//61是“=”的ascii码
                String value = arg.substring(arg.indexOf(61) + 1);
                map.put(key.replace("--", ""), value);
            }
        }

        return map;
    }

    /**
     * 加载默认属性，配置文件中没有配置的属性会采用这里的值。
     * 相当于对springcloud和springboot的默认值进行再一次的定制和改变，
     * 比如默认的server.port=8080，可以在这里改成9090。
     *
     * 相关参数含义
     * https://blogger.csdn.net/acmman/article/details/99670419
     *
     * @return
     */
    private Map<String,Object> loadDefaultProperties() throws IOException {

        //System.setProperty是获取java系统属性
        //在java代码中的System.setProperty("java.security.egd", "file:/dev/./urandom");
        //就等价于在java启动springboot的jar包是的“-Djava.security.egd=file:/dev/./urandom”参数

        //解决阿里云的tomcat启动慢的问题，SecureRandom在java各种组件中使用广泛，可以可靠的产生随机数。但在大量产生随机数的场景下，性能会较低。
        //这时可以使用"-Djava.security.egd=file:/dev/./urandom" 加快随机数产生过程。
        System.setProperty("java.security.egd", "file:/dev/./urandom");

        //java 启动springboot的jar包时指定lib目录，
        //java -jar -Dloader.path=lib  xxxApp.jar
        //java -jar -Dloader.path=lib,templates,static  xxxApp.jar
        System.setProperty("loader.path", "./plugins");

        //如果在java启动springboot的jar包时增加“-Dssl=true”参数，就可以让应用支持https请求，默认是不支持https
        Boolean supportSsl = Boolean.valueOf(System.getProperty("ssl", "false"));
        logger.info("supportSsl=" + supportSsl);

        //生成jwt和https相关文件，这几个文件都是事先放在当前jar包下的
        //#生成jks文件
        //#keytool -genkeypair -alias milepost-alias -validity 365 -keyalg RSA -dname "CN=花瑞富,OU=milepost公司,O=milepost公司,L=shenyan,S=liaoning,C=CH" -keypass milepost -keystore milepost.jks -storepass milepost
        //#获取jks文件中的公钥，将公钥部分保存在public.key文件中，这个文件就是其他服务用来解码jwt的公钥。
        //#keytool -list -rfc --keystore milepost.jks | openssl x509 -inform pem -pubkey
        //StreamFiles.InputStreamToFile(ResourceUtils.getURL("classpath:milepost.jks").openStream(), new File("milepost.jks"));
        FileCopyUtils.copy(ResourceUtils.getURL("classpath:milepost.jks").openStream(), new FileOutputStream(new File("milepost.jks")));
        //设置这两个之后，回去一个临时路径找milepost.jks，导致报错，
        //System.setProperty("javax.net.ssl.trustStore", "milepost.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword", defaultPassword);

        //StreamFiles.InputStreamToFile(ResourceUtils.getURL("classpath:public.key").openStream(), new File("public.key"));
        //StreamFiles.InputStreamToFile(ResourceUtils.getURL("classpath:license.dat").openStream(), new File("license.dat"));
        FileCopyUtils.copy(ResourceUtils.getURL("classpath:public.key").openStream(), new FileOutputStream(new File("public.key")));
        FileCopyUtils.copy(ResourceUtils.getURL("classpath:license.dat").openStream(), new FileOutputStream(new File("license.dat")));


        //读取application.yml中配置的属性，当向defaultProperties中put ${xxx}样子的值来引入application.yml中的内容时，
        // 就可以先从appYmlMap中获取到xxx的值，然后put进入defaultProperties中，然后才能使用${xxx}引用
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ClassPathResource resource = new ClassPathResource("application.yml");
        //最顶层的元素
        HashMap appYmlMap = mapper.readValue(resource.getInputStream(), HashMap.class);

        //获取当前应用的引用类型
        String applicationType = "";
        if(customProperties != null){
            applicationType = (String)(customProperties.get(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY));
        }

        //构造默认属性
        Map<String,Object> defaultProperties = new HashMap<>();

        //profiles
        String springProfilesActive = getStringByKeyFromAppYmlMap(appYmlMap, "spring.profiles.active");
        if(StringUtils.isNotBlank(springProfilesActive)){
            defaultProperties.put("spring.profiles.active", springProfilesActive);
        }else{
            defaultProperties.put("spring.profiles.active", "dev");
        }
        defaultProperties.put("spring.main.banner-mode", "off");//关闭，使用自己写的com.milepost.core.banner.PrintBanner打印banner

        //允许多个接口上的@FeignClient(“相同服务名”)
        defaultProperties.put("spring.main.allow-bean-definition-overriding", true);

        //xxs暂时不考虑，后续要加进来
//        defaultProperties.put("iplatform.xss.enabled", "true");
//        defaultProperties.put("iplatform.xss.words[0]", "alert\\s*\\(");
//        defaultProperties.put("iplatform.xss.words[1]", "eval\\s*\\(");
//        defaultProperties.put("iplatform.xss.words[2]", "javascript:");
//        defaultProperties.put("iplatform.xss.words[3]", "<script>");
//        defaultProperties.put("iplatform.xss.regex", "");
//        defaultProperties.put("iplatform.xss.policy", "break");

        //设置org.apache.activemq的日志级别为error，后续处理，感觉可以放在logback中配置
        //defaultProperties.put("logging.level.org.apache.activemq", "ERROR");

        //management监控相关，
        //监控相关端点的前缀，访问路径为https://localhost:port/${server.servlet.context-path}/milepost-actuator
        defaultProperties.put("management.endpoints.web.base-path", "/milepost-actuator");
        //开启除了shutdown之外的所有端点，具体有哪些见《深入浅出Spring Boot 2.x-标注版.pdf》394页
        defaultProperties.put("management.endpoints.web.exposure.include", "*");//在yml中要"*"，不能 *
        Set<String> exclude = new LinkedHashSet();
        exclude.add("shutdown");
        exclude.add("beans");
        defaultProperties.put("management.endpoints.web.exposure.exclude", exclude);
        //禁用以下组件的健康检查，每一个组件对应一个指示器，如redis的指示器是 RedisHealthIndicator
        defaultProperties.put("management.health.redis.enabled", "false");
        defaultProperties.put("management.health.mongo.enabled", "false");
        defaultProperties.put("management.health.elasticsearch.enabled", "false");
        defaultProperties.put("management.health.jms.enabled", "false");
        //这个已经过期了，A global security auto-configuration is now provided. Provide your own WebSecurityConfigurer bean instead.
        //defaultProperties.put("management.security.enabled", "false");

        //endpoints
        //defaultProperties.put("endpoints.restart.enabled", "false");
        //defaultProperties.put("endpoints.shutdown.enabled", "true");
        //变成了
        defaultProperties.put("management.endpoint.restart.enabled", "false");
        defaultProperties.put("management.endpoint.shutdown.enabled", "true");
        //没有这个属性了，Endpoint sensitive flag is no longer customizable as Spring Boot no longer provides a customizable security auto-configuration\n. Create or adapt your security configuration accordingly.
        //defaultProperties.put("endpoints.health.sensitive", "false");

        //redis，暂时不考虑，springboot2.x整合redis的配置变了，之前是spring.redis.pool.xxx
        //Maximum number of connections that can be allocated by the pool at a given time. Use a negative value for no limit.
        defaultProperties.put("spring.redis.jedis.pool.max-active", "5");
        /**
         * Maximum amount of time a connection allocation should block before throwing an
         * exception when the pool is exhausted. Use a negative value to block
         * indefinitely.
         */
        defaultProperties.put("spring.redis.jedis.pool.max-wait", "-1");
        /**
         * Target for the minimum number of idle connections to maintain in the pool. This
         * setting only has an effect if it is positive.
         */
        defaultProperties.put("spring.redis.jedis.pool.min-idle", "1");
        /**
         * Maximum number of "idle" connections in the pool. Use a negative value to
         * indicate an unlimited number of idle connections.
         */
        defaultProperties.put("spring.redis.jedis.pool.max-idle", "5");
        defaultProperties.put("spring.redis.timeout", "1000");

        //datasource，
        defaultProperties.put("spring.datasource.test-while-idle", "true");
        defaultProperties.put("spring.datasource.time-between-eviction-runs-millis", "300000");
        defaultProperties.put("spring.datasource.min-evictable-idle-time-millis", "30000");
        defaultProperties.put("spring.datasource.validation-query", "SELECT 1");
        defaultProperties.put("spring.datasource.max-active", "100");
        defaultProperties.put("spring.datasource.max-idle", "5");
        defaultProperties.put("spring.datasource.min-idle", "2");
        defaultProperties.put("spring.datasource.max-wait", "30000");
        defaultProperties.put("spring.datasource.initial-size", "5");

        //logapi开关
        //defaultProperties.put("iplatform.logapi.enabled", "true");

        //Whether to dispatch OPTIONS requests to the FrameworkServlet doService method.
        defaultProperties.put("spring.mvc.dispatch-options-request", "true");

        //favicon开关，默认是开启的，这里改成关闭
        defaultProperties.put("spring.mvc.favicon.enabled", "false");

        //tomcat
        //Maximum amount of worker threads. default=200,
        defaultProperties.put("server.tomcat.max-threads", "2000");
        //Maximum queue length for incoming connection requests when all possible request processing threads are in use.  default=100,
        defaultProperties.put("server.tomcat.accept-count", "200");
        //Minimum amount of worker threads. default=10,
        defaultProperties.put("server.tomcat.min-spare-threads", "10");
        //Character encoding to use to decode the URI. default=Charset.forName("UTF-8");
        defaultProperties.put("server.tomcat.uri-encoding", "UTF-8");
        //defaultProperties.put("server.tomcat.max-http-header-size", "65536");//这个过期了，单位是B，65536B=64KB
        //Maximum size of the HTTP message header. default=8KB
        defaultProperties.put("server.max-http-header-size", "64KB");//使用这个

        //没有这个属性了
        //defaultProperties.put("server.tomcat.disabled.methods", "OPTIONS,HEAD,TRACE");

        //feign，后续处理
//        defaultProperties.put("feign.hystrix.enabled", "true");

        //cache，spring的缓存感觉不太好用，太复杂，不如用mybatis的缓存。
        //defaultProperties.put("spring.cache.type", "guava");

        //thymeleaf，我不使用thymeleaf
        //defaultProperties.put("spring.thymeleaf.cache", "true");
        //defaultProperties.put("spring.thymeleaf.mode", "LEGACYHTML5");

        //Whether to enable the content Version Strategy. default=false
        defaultProperties.put("spring.resources.chain.strategy.content.enabled", "true");
        defaultProperties.put("spring.resources.chain.strategy.content.paths", "/**");

        //ribbon，后续处理
//        defaultProperties.put("ribbon.eureka.enabled", "true");
//        defaultProperties.put("ribbon.ConnectTimeout", "10000");
//        defaultProperties.put("ribbon.ReadTimeout", "60000");

        //https://www.jianshu.com/p/838f4d2b926a,
        //引入这个jasypt-spring-boot-starter依赖才有下面这个配置，
        //jasyt是一个加密框架，见https://github.com/ulisesbocchio/jasypt-spring-boot
        defaultProperties.put("jasypt.encryptor.password", defaultPassword);

        //Date format to use.
        defaultProperties.put("spring.mvc.date-format", "yyyy-MM-dd HH:mm:ss");

        //jackson
        defaultProperties.put("spring.jackson.time-zone", "GMT+8");
        defaultProperties.put("spring.jackson.date-format", "yyyy-MM-dd HH:mm:ss");

        //hystrix
//        defaultProperties.put("hystrix.command.default.fallback.enabled", "true");
//        defaultProperties.put("hystrix.command.default.fallback.isolation.semaphore.maxConcurrentRequests", "5000");
//        defaultProperties.put("hystrix.command.default.execution.timeout.enabled", "true");
//        defaultProperties.put("hystrix.command.default.execution.isolation.strategy", "SEMAPHORE");
//        defaultProperties.put("hystrix.command.default.execution.isolation.semaphore.maxConcurrentRequests", "5000");
//        defaultProperties.put("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", "180000");

        //h2是一个数据库，不使用h2数据库
        defaultProperties.put("spring.h2.console.enabled", "false");

        if(supportSsl.booleanValue()) {
            defaultProperties.put("server.ssl.enabled", "true");
            defaultProperties.put("server.ssl.key-store", "classpath:milepost.jks");
            defaultProperties.put("server.ssl.key-store-password", defaultPassword);
            defaultProperties.put("server.ssl.key-password", defaultPassword);
            defaultProperties.put("server.ssl.key-alias", "milepost-alias");
            defaultProperties.put("server.ssl.key-store-type", "jks");
        } else {
            defaultProperties.put("server.ssl.enabled", "false");
        }

        //Value to use for the Server response header (if empty, no header is sent).
        defaultProperties.put("server.server-header", "milepost-framework");

        //oauth2，后续处理
//        defaultProperties.put("security.oauth2.resource.userInfoUri", "AUTH-SERVICE");

        //eureka.instance相关页面地址，暂时不考虑ssl
        String protocol = "http";
        if(supportSsl.booleanValue()) {
            //http通信端口是否启用，默认为true
            defaultProperties.put("eureka.instance.non-secure-port-enabled", "false");
            //https通信端口是否启用
            defaultProperties.put("eureka.instance.secure-port-enabled", "true");
            //https通信端口
            defaultProperties.put("eureka.instance.secure-port", "${server.port}");
            protocol = "https";
        }

        String tenant = getStringByKeyFromAppYmlMap(appYmlMap, "multiple-tenant.tenant");
        tenant = (tenant==null? "default":tenant);//默认为default。
        //eureka，以下三个属性是必须配置的，eureka.instance.ip-address、spring.application.name、server.port
        String eurekaInstanceIpAddress = getStringByKeyFromAppYmlMap(appYmlMap, "eureka.instance.ip-address");
        String springApplicationName = getStringByKeyFromAppYmlMap(appYmlMap, "spring.application.name");
        String serverPort = getStringByKeyFromAppYmlMap(appYmlMap, "server.port");
        defaultProperties.put("eureka.instance.ip-address", eurekaInstanceIpAddress);
        defaultProperties.put("spring.application.name", springApplicationName);
        defaultProperties.put("server.port", (StringUtils.isBlank(serverPort)? "8080":serverPort));
        //这三个属性必须设置，否则下面的eureka.instance.instance-id在没有加载到springboot配置文件时是无法解析的
        //所以，以上这三个配置项是必须要配置的，即loadDefaultProperties方法中设置的属性，如果是${xxx}形式，就必须能取到值，
        defaultProperties.put("eureka.instance.instance-id", "${eureka.instance.ip-address}:"+ tenant +":${spring.application.name}:${server.port}");

        defaultProperties.put("eureka.instance.prefer-ip-address", "true");
        //既然eureka.instance.prefer-ip-address=true，就不需要hostname了
        //defaultProperties.put("eureka.instance.hostname", "${eureka.instance.ip-address}");

        String serverServletContextPath = getStringByKeyFromAppYmlMap(appYmlMap, "server.servlet.context-path");

        //在EurekaServer的启动类中将isEurekaServer属性设置进了java系统属性中，System.setProperty("isEurekaServer", "true");
        boolean isEurekaServer = this.isEurekaServer();
        if(isEurekaServer) {
            //defaultProperties.put("eureka.client.serviceUrl.defaultZone", "${discovery.server.address}");
            //EurekaServer两个false，注意，当集群部署时候要开启，否则EurekaServer控制台的基本信息中显示副本不可达，
            //当abc集群时，a向bc注册，b向ac注册，c向ab注册，本质上还是非自注册的。
            //此时也可以配置EurekaServer的续约过期时长、续约间隔时间等参数
            defaultProperties.put("eureka.client.register-with-eureka", "false");
            defaultProperties.put("eureka.client.fetch-registry", "false");
            //开启EurekaServer的自我保护机制
            defaultProperties.put("eureka.server.enable-self-preservation", "true");
            //EurekaServer每隔多长时间剔除一次服务，默认60 * 1000
            defaultProperties.put("eureka.server.eviction-interval-timer-in-ms", "5000");
            //响应缓存更新时间，默认30 * 1000
            defaultProperties.put("eureka.server.response-cache-update-interval-ms", "5000");
            //续约阈值百分比
            defaultProperties.put("eureka.server.renewal-percent-threshold",0.85);
            //EurekaServer期望EurekaClient多长时间续约一次
            defaultProperties.put("eureka.server.expected-client-renewal-interval-seconds",30);
            //EurekaServer页面上的Renews threshold表示续约阈值，即EurekaServer每分钟最少需要收到的续约次数，
            //Renews threshold = n * (60/expected-client-renewal-interval-seconds) * renewal-percent-threshold，
            //其中n为注册到EurekaServer的EurekaClient个数，
            //  单实例：EurekaServer是非自注册的，则n需要加1，
            //  集群(abc)：EurekaServer是自注册的，即a向bc注册，a不向a自己注册，a是bc的EurekaClient，此时n不需要加1。
            //EurekaServer页面上的Renews (last min)表示最后一分钟收到的续约次数。
            //当Renews (last min)<Renews threshold时，就会触发EurekaServer的自我保护机制。
            //当Renews (last min)>=Renews threshold时，EurekaServer自动退出自我保护机制。
            //如何计算不太重要，重要的是保证系统稳定之后，不要进入自我保护状态即可

            //更新控制台页面上续约阈值的时间间隔，
            defaultProperties.put("eureka.server.renewal-threshold-update-interval-ms",15*60*1000);
        } else {
            //defaultProperties.put("eureka.client.serviceUrl.defaultZone", "${discovery.server.address}");
            //Indicates how often(in seconds) to fetch the registry information from the eureka server. 默认是30，这里改成5
            defaultProperties.put("eureka.client.registry-fetch-interval-seconds", "10");
            /**
             * Indicates how often (in seconds) the eureka client needs to send heartbeats to
             * eureka server to indicate that it is still alive. If the heartbeats are not
             * received for the period specified in leaseExpirationDurationInSeconds, eureka
             * server will remove the instance from its view, there by disallowing traffic to this
             * instance. default=30
             * EurekaClient向EurekaServer的续约间隔时间，默认30，这里改成10
             */
            defaultProperties.put("eureka.instance.lease-renewal-interval-in-seconds", "4");//10
            /**
             * Indicates the time in seconds that the eureka server waits since it received the
             * last heartbeat before it can remove this instance from its view and there by
             * disallowing traffic to this instance.
             *
             * Setting this value too long could mean that the traffic could be routed to the
             * instance even though the instance is not alive. Setting this value too small could
             * mean, the instance may be taken out of traffic because of temporary network
             * glitches.This value to be set to atleast higher than the value specified in
             * leaseRenewalIntervalInSeconds. default=90
             *
             * EurekaClient向EurekaServer的续约过期时长，默认90，iplatform的这里没做改变，我把他改成15
             */
            defaultProperties.put("eureka.instance.lease-expiration-duration-in-seconds", "6");//15

            //healthcheck开关，默认值是true
            //https://www.jianshu.com/p/6dddcf873be2
            defaultProperties.put("eureka.client.healthcheck.enabled", "true");
        }

        String serverServletContextPathEmpty = (StringUtils.isBlank(serverServletContextPath)? "" : serverServletContextPath);

        defaultProperties.put("eureka.instance.home-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}" + serverServletContextPathEmpty);
        defaultProperties.put("eureka.instance.home-page-url-path", serverServletContextPathEmpty);
        defaultProperties.put("eureka.instance.health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPathEmpty +"${management.endpoints.web.base-path}/health");
        defaultProperties.put("eureka.instance.health-check-url-path",  serverServletContextPathEmpty +"${management.endpoints.web.base-path}/health");
        defaultProperties.put("eureka.instance.secure-health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPathEmpty +"${management.endpoints.web.base-path}/health");
        defaultProperties.put("eureka.instance.status-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPathEmpty +"${management.endpoints.web.base-path}/info");
        defaultProperties.put("eureka.instance.status-page-url-path",  serverServletContextPathEmpty +"${management.endpoints.web.base-path}/info");

        //eureka.instance.metadata-map
        String infoAppVersion = getStringByKeyFromAppYmlMap(appYmlMap, "info.app.version");
        infoAppVersion = StringUtils.isBlank(infoAppVersion)? MilepostConstant.APPLICATION_VERSION : infoAppVersion;//应用版本
        defaultProperties.put("eureka.instance.metadata-map.management.context-path", serverServletContextPathEmpty +"${management.endpoints.web.base-path}");

        //从配置文件中读取权重和跟踪采样率，设置默认值，
        // 默认值写在pojo的属性中了，但是这里无法注入，所以只能判断一下，写在pojo中的默认值当作编写yml时的提示，
        String weight = getStringByKeyFromAppYmlMap(appYmlMap, "multiple-tenant.weight");
        weight = (StringUtils.isNotBlank(weight)? weight : "1");//默认值为1
        String trackSampling = getStringByKeyFromAppYmlMap(appYmlMap, "multiple-tenant.track-sampling");
        trackSampling = (StringUtils.isNotBlank(trackSampling)? trackSampling : "0.1");//默认值为0.1
        defaultProperties.put("eureka.instance.metadata-map.weight", weight);//权重
        defaultProperties.put("eureka.instance.metadata-map.track-sampling", trackSampling);//应用的跟中采样率
        //如果配置文件中设置了租户、标签则读取出来设置到eureka.instance.metadata-map中，
        //String tenant = getStringByKeyFromAppYmlMap(appYmlMap, "multiple-tenant.tenant");//在前面读取，因为设置默认的实例id要用到租户
        String labelAnd = getStringByKeyFromAppYmlMap(appYmlMap, "multiple-tenant.label-and");
        String labelOr = getStringByKeyFromAppYmlMap(appYmlMap, "multiple-tenant.label-or");
        if(StringUtils.isNotBlank(tenant)) defaultProperties.put("eureka.instance.metadata-map.tenant", tenant);//租户
        if(StringUtils.isNotBlank(labelAnd)) defaultProperties.put("eureka.instance.metadata-map.label-and", labelAnd);//与标签
        if(StringUtils.isNotBlank(labelOr)) defaultProperties.put("eureka.instance.metadata-map.label-or", labelOr);//或标签

        //暂时不考虑用户域
        //defaultProperties.put("eureka.instance.metadata-map.user-domain", "DEFAULT");//用户域
        defaultProperties.put("eureka.instance.metadata-map.milepostversion", MilepostConstant.MILEPOST_VERSION);//框架版本
        defaultProperties.put("eureka.instance.metadata-map.version", infoAppVersion);
        defaultProperties.put("eureka.instance.metadata-map.name", "${info.app.name}");
        defaultProperties.put("eureka.instance.metadata-map.instance-id", "${eureka.instance.ip-address}:"+ tenant +":${spring.application.name}:${server.port}");

        //deploytype
        if(Paths.get("/.dockerenv", new String[0]).toFile().exists()) {
            defaultProperties.put("eureka.instance.metadata-map.deploytype", "docker");
        } else {
            defaultProperties.put("eureka.instance.metadata-map.deploytype", "host");
        }

        //springcloud集中配置，暂时不考虑，后续如果使用的时候在处理
//        defaultProperties.put("spring.cloud.config.enabled", "false");
        //配置中心
//        defaultProperties.put("eureka.instance.metadata-map.configprofile", "${spring.cloud.config.profile}");

        //uptime
        defaultProperties.put("eureka.instance.metadata-map.uptime", String.valueOf(System.currentTimeMillis()));

        //进程信息
        String processinfo = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        logger.info("processinfo {}", processinfo);
        defaultProperties.put("eureka.instance.metadata-map.pid", processinfo.split("@")[0]);
        defaultProperties.put("eureka.instance.metadata-map.processinfo", ManagementFactory.getRuntimeMXBean().getName());

        //logger.kafka
//        defaultProperties.put("logger.kafka.enabled", "false");
//        defaultProperties.put("logger.kafka.partitions", "20");
//        defaultProperties.put("logger.kafka.replication", "1");

        //sleuth
//        defaultProperties.put("spring.sleuth.enabled", "false");
//        defaultProperties.put("spring.zipkin.type", "activemq");
//        defaultProperties.put("spring.sleuth.mybatis.enabled", "true");
//        defaultProperties.put("spring.sleuth.event.http.time.overpct", "3");
//        defaultProperties.put("spring.sleuth.event.sql.time.overpct", "3");
//        defaultProperties.put("spring.sleuth.event.scheduling.time.overpct", "3");

        //service、auth需要flyway和mybatis

        if(MilepostApplicationType.SERVICE.getValue().equalsIgnoreCase(applicationType)
                || MilepostApplicationType.AUTH.getValue().equalsIgnoreCase(applicationType)){
            //flyway，数据库脚本名为“V1__xxx.sql”，当有表结构更新时，新增脚本，版本号增加即可，
            String springDatasourceDruidDbType = getStringByKeyFromAppYmlMap(appYmlMap, "spring.datasource.druid.db-type");
            if(StringUtils.isBlank(springDatasourceDruidDbType)){
                springDatasourceDruidDbType = "";
            }
            defaultProperties.put("spring.datasource.initialize", "false");//禁止spring使用data.sql来初始化。
            defaultProperties.put("spring.flyway.enabled", "true");//默认开启
            defaultProperties.put("spring.flyway.baseline-on-migrate", "true");//当迁移时发现目标schema非空，而且带有没有元数据的表时，是否自动执行基准迁移，默认false.
            //defaultProperties.put("spring.flyway.baseline-description", "init");//对执行迁移时基准版本的描述.
            defaultProperties.put("spring.flyway.baseline-version", "0");//执行基线时用来标记已有Schema的版本，默认值为1.
            defaultProperties.put("spring.flyway.locations", "classpath:db/" + springDatasourceDruidDbType);//sql脚本存放位置，按照数据库类型区分
            defaultProperties.put("spring.flyway.clean-on-validation-error", "true");//Whether to automatically call clean when a validation error occurs.
            defaultProperties.put("spring.flyway.clean-disabled", "true");//Whether to disable cleaning of the database.
            defaultProperties.put("spring.flyway.table", "flyway_md_" + springApplicationName);//flyway元数据表名称

            //mybatis
            defaultProperties.put("mybatis.config-location", "classpath:mybatis-config.xml");
            defaultProperties.put("mybatis.mapper-locations", "classpath:com/milepost/**/**/dao/*.xml");

            if(MilepostApplicationType.SERVICE.getValue().equalsIgnoreCase(applicationType)){
                defaultProperties.put("swagger.authorization.key-name", "Authorization");//swagger-ui调用接口时传入的token请求头名称，值为“Bearer {token}”

                defaultProperties.put("swagger.title", springApplicationName + " swagger");//swagger-ui调用接口时传入的token请求头名称，值为“Bearer {token}”
                defaultProperties.put("swagger.description", springApplicationName + " swagger");//swagger-ui调用接口时传入的token请求头名称，值为“Bearer {token}”
            }
        }else{
            defaultProperties.put("spring.flyway.enabled", "false");
        }

        //velocity与thymeleaf类似，是一个模版引擎，
        defaultProperties.put("spring.velocity.enabled", "false");

        //multipart
        //默认为true
        defaultProperties.put("spring.servlet.multipart.enabled", "true");
        //默认为0B
        defaultProperties.put("spring.servlet.multipart.file-size-threshold", "0B");
        //默认为1MB
        defaultProperties.put("spring.servlet.multipart.max-file-size", "100MB");
        //默认为10MB
        defaultProperties.put("spring.servlet.multipart.max-request-size", "1000MB");

        //sharding，没有这个属性了
        //defaultProperties.put("sharding.jdbc.enable", "false");
        //defaultProperties.put("sharding.jdbc.masterslave", "false");

        //把customProperties放进来
        if(this.customProperties != null) {
            Iterator it = this.customProperties.entrySet().iterator();

            while(it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                defaultProperties.put(key.toString(), value.toString());
            }
        }

        return defaultProperties;
    }

    /**
     * @param appYmlMap
     * @param key
     * @return
     */
    private Map<String, Object> getMapByKeyFromAppYmlMap(HashMap appYmlMap, String key) {
        String[] keyArray = key.split("\\.");
        Map<String, Object> mapTemp = appYmlMap;
        for(int i=0; i<(keyArray.length); i++){
            if(mapTemp != null){
                mapTemp = (HashMap<String, Object>)mapTemp.get(keyArray[i]);
            }else{
                return null;
            }
        }
        return mapTemp;
    }

    /**
     * 如果没有配置则返回null
     * @param appYmlMap
     * @param key
     * @return
     */
    private String getStringByKeyFromAppYmlMap(HashMap appYmlMap, String key) {
        String[] keyArray = key.split("\\.");
        String mapKey = key.substring(0, (key.lastIndexOf(".")));
        Map<String, Object> map = getMapByKeyFromAppYmlMap(appYmlMap, mapKey);
        if(map != null){
            Object value = map.get(keyArray[keyArray.length-1]);
            if(value == null){
                return null;
            }
            //读取到那些在yml中引用其他属性的属性值
            String valurString = String.valueOf(value);
            if(valurString.startsWith("${") && valurString.endsWith("}")){
                return getStringByKeyFromAppYmlMap(appYmlMap, valurString);
            }else{
                return valurString;
            }
        }else{
            return null;
        }
    }

    private void runBefore() {
        printEnv();
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                //这里不知道什么时候能进来，我没发现能进来的时候
//                if(MilepostApplication.this.discoveryClient != null) {
//                    try {
//                        MilepostApplication.logger.info("在注册中心注销本服务。");
//                        MilepostApplication.this.discoveryClient.shutdown();
//                        MilepostApplication.this.discoveryClient = null;
//                    } catch (Exception e) {
//                        MilepostApplication.logger.error("在注册中心注销服务错误。", e);
//                    }
//                }
//            }
//        });
    }

    private void runAfter() {
        try {
            this.showEurekaInfo();
            started = Boolean.TRUE;
            logger.info("服务启动完毕。");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 打印操作系统的环境变量
     */
    private static void printEnv() {
        Map<String, String> envs = System.getenv();
        if(envs.size() > 0) {
            logger.info("----------system environment----------");
            Iterator it = envs.keySet().iterator();

            while(it.hasNext()) {
                String name = (String)it.next();
                String value = (String)envs.get(name);
                logger.info("{}={}", name, value);
            }

            logger.info("----------system environment----------");
        }
    }

    /**
     * 打印EurekaServer相关信息，具体打印什么之后研究一下再定
     */
    private void showEurekaInfo() {
        String eurekaClientEnabled = context.getEnvironment().getProperty("eureka.client.enabled", "true");
        logger.info("eureka.client.enabled=" + eurekaClientEnabled);
        if(eurekaClientEnabled != null && "true".equalsIgnoreCase(eurekaClientEnabled)) {
            ApplicationInfoManager applicationInfoManager = ApplicationContextProvider.getContext().getBean(ApplicationInfoManager.class);
            InstanceInfo instanceInfo = applicationInfoManager.getInfo();
            ConfigurableEnvironment environment = context.getEnvironment();
            logger.info("eureka.instance.metadata-map.tenant=" + environment.getProperty("eureka.instance.metadata-map.tenant"));
            logger.info("eureka.instance.home-page-url=" + instanceInfo.getHomePageUrl());
            logger.info("eureka.instance.home-page-url-path=" + environment.getProperty("eureka.instance.home-page-url-path"));
            logger.info("eureka.instance.health-check-url=" + instanceInfo.getHealthCheckUrl());
            logger.info("eureka.instance.health-check-url-path=" + environment.getProperty("eureka.instance.health-check-url-path"));
            logger.info("eureka.instance.secure-health-check-url=" + instanceInfo.getSecureHealthCheckUrl());
            logger.info("eureka.instance.status-page-url=" + instanceInfo.getStatusPageUrl());
            logger.info("eureka.instance.status-page-url-path=" + environment.getProperty("eureka.instance.status-page-url-path"));
            logger.info("eureka.instance.metadata-map.management.context-path=" + environment.getProperty("eureka.instance.metadata-map.management.context-path"));
            logger.info("eureka.instance.metadata-map.instance-id=" + instanceInfo.getInstanceId());
            logger.info("eureka.instance.metadata-map.version=" + environment.getProperty("eureka.instance.metadata-map.version"));
            logger.info("eureka.instance.metadata-map.milepostversion=" + environment.getProperty("eureka.instance.metadata-map.milepostversion"));

            /*
            * defaultProperties.put("eureka.instance.home-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}" + serverServletContextPathEmpty);
            defaultProperties.put("eureka.instance.home-page-url-path", serverServletContextPathEmpty);
            defaultProperties.put("eureka.instance.health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPathEmpty +"${management.endpoints.web.base-path}/health");
            defaultProperties.put("eureka.instance.health-check-url-path",  serverServletContextPathEmpty +"${management.endpoints.web.base-path}/health");
            defaultProperties.put("eureka.instance.secure-health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPathEmpty +"${management.endpoints.web.base-path}/health");
            defaultProperties.put("eureka.instance.status-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPathEmpty +"${management.endpoints.web.base-path}/info");
            defaultProperties.put("eureka.instance.status-page-url-path",  serverServletContextPathEmpty +"${management.endpoints.web.base-path}/info");

            * */

        }
    }

    /**
     * 当前应用是否是EurekaServer，
     * @return 是则返回tyue
     */
    public boolean isEurekaServer() {
        String appType = (String)(this.customProperties.get(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY));
        return MilepostApplicationType.EUREKA.getValue().equals(appType);
    }
}




