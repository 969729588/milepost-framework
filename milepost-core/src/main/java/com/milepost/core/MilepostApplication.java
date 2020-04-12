package com.milepost.core;

import com.google.common.base.CaseFormat;
import com.milepost.api.constant.MilepostConstant;
import com.milepost.api.enums.MilepostApplicationType;
import com.milepost.api.util.ReadAppYml;
import com.milepost.core.spring.ApplicationContextProvider;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by Ruifu Hua on 2019/12/30.
 */
public class MilepostApplication extends SpringApplication{

    private static Logger logger = LoggerFactory.getLogger(MilepostApplication.class);
    private Map<String, Object> customProperties;
    private Map<String, String> argsMap;
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
        //SpringBoot不能识别java系统属性和操作系统环境变量中的
        // eureka.client.service-url.defaultZone=abc、eureka_client_serviceUrl={defaultZone: http://192.168.1.105:8761/eureka/}
        //所以，如果命令行参数中没有此配置项，则将其放到命令行参数中，
        String[] newArgs = handleEcsDefaultZone(args);

        MilepostApplication milepostApplication = new MilepostApplication(new Class[]{primarySource}, customProperties);
        ConfigurableApplicationContext context = milepostApplication.run(newArgs);
        return context;
    }

    /**
     * springboot加载配置项的优先级是  命令行参数   >   java系统属性(System.getProperties())    >   操作系统环境变量(System.getenv())   >   配置文件
     * 对于一些自己定义的属性，在逻辑处理过程中，要注意这个优先级。
     * 框架必须要完美支持操作系统环境变量，因为在k8s中，容器创建时候可以指定环境变量，这是很重要的。
     *
     * @param args
     * @return
     */
    public ConfigurableApplicationContext run(String... args) {
        try {
            //启动之前
            this.runBefore();
            //解析命令行参数，主要用于将应用发布到k8s集群中，获取浮动ip。暂时不用关注
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
     * SpringBoot不能识别java系统属性和操作系统环境变量中的
     * eureka.client.serviceUrl.defaultZone=abc、eureka_client_serviceUrl={defaultZone: http://192.168.1.105:8761/eureka/}
     * 所以，如果操作系统环境变量中有此配置项，并且命令行参数中没有此配置项，则将其放到命令行参数中，
     *
     * 注意，key中的defaultZone必须驼峰式，因为他是一个map，
     * @param args
     * @return
     */
    private static String[] handleEcsDefaultZone(String[] args) {
        List<String> argList = new ArrayList<>();
        for(String arg : args){
            argList.add(arg);
        }

        String envKey = "eureka_client_serviceUrl_defaultZone";
        String argsKey = "eureka.client.service-url.defaultZone";

        boolean argsContained = false;
        for(String arg : argList){
            if(arg.startsWith("--" + argsKey)){
                argsContained = true;
                break;
            }
        }

        String envValue = System.getenv(envKey);
        //envValue = "http://192.168.223.136:8761/eureka/";
        if(StringUtils.isNotBlank(envValue) && !argsContained){
            argList.add("--" + argsKey + "=" + envValue);
        }

        String[] newArgs = new String[argList.size()];
        argList.toArray(newArgs);

        return newArgs;
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
        //logger.info("before:argsList -> " + String.valueOf(argsList));
        List<String> newArgsList = new LinkedList<>();
        newArgsList.addAll(argsList);
        this.argsMap = argsToMap(args);
        //logger.info("before:argsMap -> " + String.valueOf(this.argsMap));

        Object eurekaInstanceIpAddressPrefix;
        //以下获取eurekaInstanceIpAddressPrefix参数值的顺序必须符合springboot加载配置项的优先级，
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
    private static Map<String, String> argsToMap(String[] args) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
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

        String springProfilesActiveDev = "dev";
        String springProfilesActiveTest = "test";
        String springProfilesActiveProd = "prod";

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
        //logger.info("supportSsl=" + supportSsl);

        //生成jwt和https相关文件，这几个文件都是事先放在当前jar包下的
        //1、生成jks文件
        //#keytool -genkeypair -alias milepost-alias -validity 365 -keyalg RSA -dname "CN=花瑞富,OU=milepost公司,O=milepost公司,L=shenyan,S=liaoning,C=CH" -keypass milepost -keystore milepost.jks -storepass milepost

        //2、获取jks文件中的公钥(PUBLIC KEY)和证书(CERTIFICATE，证书文件以“.cer”结尾)，将公钥部分保存在public.key文件中，这个文件就是其他服务用来解码jwt的公钥。
        //#keytool -list -rfc --keystore milepost.jks | openssl x509 -inform pem -pubkey

        //3、获取获取jks文件中的私钥(PUBLIC KEY)和证书(CERTIFICATE，证书文件以“.cer”结尾)，将私钥部分保存在private.key文件中，这个文件就是用来给license.dat文件签名的，使用上面的公钥验签。
        //keytool -v -importkeystore -srckeystore milepost.jks -srcstoretype jks -srcstorepass milepost -destkeystore milepost.pfx -deststoretype pkcs12 -deststorepass milepost -destkeypass milepost
        //openssl pkcs12 -in milepost.pfx -nodes

        //StreamFiles.InputStreamToFile(ResourceUtils.getURL("classpath:milepost.jks").openStream(), new File("milepost.jks"));
        FileCopyUtils.copy(ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + "milepost.jks").openStream(), new FileOutputStream(new File("milepost.jks")));
        //设置这两个之后，回去一个临时路径找milepost.jks，导致报错，
        //System.setProperty("javax.net.ssl.trustStore", "milepost.jks");
        //System.setProperty("javax.net.ssl.trustStorePassword", defaultPassword);

        //StreamFiles.InputStreamToFile(ResourceUtils.getURL("classpath:public.key").openStream(), new File("public.key"));
        //StreamFiles.InputStreamToFile(ResourceUtils.getURL("classpath:license.dat").openStream(), new File("license.dat"));
        FileCopyUtils.copy(ResourceUtils.getURL("classpath:public.key").openStream(), new FileOutputStream(new File("public.key")));
        FileCopyUtils.copy(ResourceUtils.getURL("classpath:license.dat").openStream(), new FileOutputStream(new File("license.dat")));

        //认证UI 登录SBA Server 的用户信息，
        String loginSbaServerUser = "admin";
        String loginSbaServerPassword = "XSRF-TOKEN=random";

        //获取当前应用的应用类型
        String applicationType = "";
        if(customProperties != null){
            applicationType = (String)(customProperties.get(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY));
        }

        //构造默认属性
        Map<String,Object> defaultProperties = new HashMap<>();

        //profiles
        String springProfilesActive = getConfigProByPriority("spring.profiles.active", springProfilesActiveDev);
        defaultProperties.put("spring.profiles.active", springProfilesActive);//这里需要设置一下，logback使用这个

        defaultProperties.put("spring.main.banner-mode", "off");//关闭，使用自己写的com.milepost.core.banner.PrintBanner打印banner

        //允许多个接口上的@FeignClient(“相同服务名”)，通过配置@FeignClient的contextId解决，不要把这个配置从true改成false.
        //defaultProperties.put("spring.main.allow-bean-definition-overriding", true);

        //这里的默认属性也支持加密。
        //defaultProperties.put("info.app.description", "ENC(Cni63Asy/ryIyTshDZ6fdLtuiZB8ZRYTyxpiMXEUrvk=)");

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
        defaultProperties.put("spring.activemq.user", "admin");
        defaultProperties.put("spring.activemq.password", "admin");
        defaultProperties.put("spring.activemq.pool.enabled", true);//使用连接池
        defaultProperties.put("spring.activemq.pool.max-connections", 10);

        //management监控相关，
        //监控相关端点的前缀，访问路径为https://localhost:port/${server.servlet.context-path}/milepost-actuator
        defaultProperties.put("management.endpoints.web.base-path", "/milepost-actuator");
        //原本是只开启以下端点，具体有哪些见《深入浅出Spring Boot 2.x-标注版.pdf》394页
        //增加了SpringBoot Admin后，开放了所有的端点，用Oauth保护所有端点，
        //Set<String> include = new LinkedHashSet<>();
        //include.add("info");
        //include.add("env");
        //include.add("beans");//显示Spring loC 容器关于Bean 的信息
        //include.add("health");
        //include.add("configprops");//显示当前项目的属性配置信息（通过@ConfigurationProperties 配置）
        //////include.add("loggers");//显示并修改应用程序中记录器的配置
        //include.add("hystrix.stream");//hystrix监控
        defaultProperties.put("management.endpoints.web.exposure.include", "*");//如开启全部，则在yml中要写"*"，不能 *
        defaultProperties.put("management.endpoint.health.show-details", "ALWAYS");

        //禁用以下组件的健康检查，每一个组件对应一个指示器，如redis的指示器是 RedisHealthIndicator
        defaultProperties.put("management.health.redis.enabled", "false");
        defaultProperties.put("management.health.mongo.enabled", "false");
        defaultProperties.put("management.health.elasticsearch.enabled", "false");
        defaultProperties.put("management.health.jms.enabled", "false");

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
//        defaultProperties.put("spring.datasource.test-while-idle", "true");
//        defaultProperties.put("spring.datasource.time-between-eviction-runs-millis", "300000");
//        defaultProperties.put("spring.datasource.min-evictable-idle-time-millis", "30000");
//        defaultProperties.put("spring.datasource.validation-query", "SELECT 1");
//        defaultProperties.put("spring.datasource.max-active", "100");
//        defaultProperties.put("spring.datasource.max-idle", "5");
//        defaultProperties.put("spring.datasource.min-idle", "2");
//        defaultProperties.put("spring.datasource.max-wait", "30000");
//        defaultProperties.put("spring.datasource.initial-size", "5");

        //logapi开关
        //defaultProperties.put("milepost.logapi.enabled", "true");

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
        defaultProperties.put("jasypt.encryptor.algorithm", "PBEWithMD5AndDES");

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
        //defaultProperties.put("security.oauth2.resource.userInfoUri", "AUTH-SERVICE");

        String tenant = this.getConfigProByPriority("multiple-tenant.tenant", "default");//默认为default。
        //eureka，以下三个属性是必须配置的，eureka.instance.ip-address、spring.application.name、server.port
        String eurekaInstanceIpAddress = this.getConfigProByPriority("eureka.instance.ip-address", null);
        String springApplicationName = this.getConfigProByPriority("spring.application.name", null);
        String serverPort = this.getConfigProByPriority("server.port", "8080");
        defaultProperties.put("eureka.instance.ip-address", eurekaInstanceIpAddress);
        defaultProperties.put("spring.application.name", springApplicationName);
        defaultProperties.put("server.port", serverPort);
        //这三个属性必须设置，否则下面的eureka.instance.instance-id在没有加载到springboot配置文件时是无法解析的
        //所以，以上这三个配置项是必须要配置的，即loadDefaultProperties方法中设置的属性，如果是${xxx}形式，就必须能取到值，

        defaultProperties.put("eureka.instance.instance-id", "${eureka.instance.ip-address}:"+ tenant +":${spring.application.name}:${server.port}");

        defaultProperties.put("eureka.instance.prefer-ip-address", "true");
        //既然eureka.instance.prefer-ip-address=true，就不需要hostname了
        //defaultProperties.put("eureka.instance.hostname", "${eureka.instance.ip-address}");

        String serverServletContextPath = this.getConfigProByPriority("server.servlet.context-path", "");

        //在EurekaServer的启动类中将isEurekaServer属性设置进了java系统属性中，System.setProperty("isEurekaServer", "true");
        if(MilepostApplicationType.EUREKA.getValue().equals(applicationType)){
            //EurekaServer
            //defaultProperties.put("eureka.client.service-url.defaultZone", "${discovery.server.address}");
            //EurekaServer两个false，注意，当集群部署时候要开启，否则EurekaServer控制台的基本信息中显示副本不可达，
            //当abc集群时，a向bc注册，b向ac注册，c向ab注册，所以EurekaServer本质上还是非自注册的。
            //此时也可以配置EurekaServer的续约过期时长、续约间隔时间等参数
            defaultProperties.put("eureka.client.register-with-eureka", "false");
            defaultProperties.put("eureka.client.fetch-registry", "false");
            //开启EurekaServer的自我保护机制
            defaultProperties.put("eureka.server.enable-self-preservation", "true");
            //EurekaServer每隔多长时间剔除一次服务，默认60 * 1000
            //defaultProperties.put("eureka.server.eviction-interval-timer-in-ms", "5000");
            //响应缓存更新时间，默认30 * 1000
            defaultProperties.put("eureka.server.response-cache-update-interval-ms", "5000");
            //是否使用只读的缓存服务清单，默认true
            //defaultProperties.put("eureka.server.use-read-only-response-cache", false);

            //续约阈值百分比
            //defaultProperties.put("eureka.server.renewal-percent-threshold",0.85);
            //EurekaServer期望EurekaClient多长时间续约一次
            //defaultProperties.put("eureka.server.expected-client-renewal-interval-seconds",30);
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
            //defaultProperties.put("eureka.server.renewal-threshold-update-interval-ms",15*60*1000);
        }else{
            //除了EurekaServer

            //defaultProperties.put("eureka.client.service-url.defaultZone", "${discovery.server.address}");
            //Indicates how often(in seconds) to fetch the registry information from the eureka server. 默认是30，这里改成5，
            //这里最好与EurekaServer端的eureka.server.response-cache-update-interval-ms一致
            defaultProperties.put("eureka.client.registry-fetch-interval-seconds", "5");
            /**
             * Indicates how often (in seconds) the eureka client needs to send heartbeats to
             * eureka server to indicate that it is still alive. If the heartbeats are not
             * received for the period specified in leaseExpirationDurationInSeconds, eureka
             * server will remove the instance from its view, there by disallowing traffic to this
             * instance. default=30
             * EurekaClient向EurekaServer的续约间隔时间，默认30，
             */
            //defaultProperties.put("eureka.instance.lease-renewal-interval-in-seconds", "4");//10
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
             * EurekaClient向EurekaServer的续约过期时长，默认90，
             */
            //defaultProperties.put("eureka.instance.lease-expiration-duration-in-seconds", "6");//15

            //healthcheck开关，默认值是true
            //https://www.jianshu.com/p/6dddcf873be2
            //https://blog.csdn.net/chengqiuming/article/details/81052322
            //defaultProperties.put("eureka.client.healthcheck.enabled", "true");

            //配置SpringBoot Admin client端相关的默认属性，只要注册到EurekaServer上的服务都需要配置
            configSpringBootAdminClient(defaultProperties, springProfilesActive, springApplicationName);

            //UI和Service
            if(MilepostApplicationType.UI.getValue().equalsIgnoreCase(applicationType) ||
                    MilepostApplicationType.SERVICE.getValue().equalsIgnoreCase(applicationType)){
                //配置feign
                configFeign(defaultProperties);
                //配置ribbon
                configRibbon(defaultProperties);
                //配置hystrix
                configHystrix(defaultProperties);
            }

            //Turbine
            if(MilepostApplicationType.TURBINE.getValue().equalsIgnoreCase(applicationType)){
                defaultProperties.put("turbine.app-config", "_all_");//监控本租户下的所有服务实例
                defaultProperties.put("turbine.combine-host-port", true);
                defaultProperties.put("turbine.cluster-name-expression", "new String(\"default\")");
            }

            //Admin，SpringBoot Admin Server
            if(MilepostApplicationType.ADMIN.getValue().equalsIgnoreCase(applicationType)){
                //设置SpringBoot Admin Server服务调用的JWT
                defaultProperties.put("info.app.auth-service.name", "milepost-auth");
                defaultProperties.put("info.app.auth-service.prefix", "/milepost-auth");

                //设置SBA Server端的用户名和密码，即保护SBA Server的security的用户名和密码，是一个写死的值，
                // 这个值也配置在认证UI的元数据中，在认证UI页面点击SBA Server时传入这个用户信息，
                defaultProperties.put("spring.security.user.name", loginSbaServerUser);
                defaultProperties.put("spring.security.user.password", loginSbaServerPassword);

                //禁止打印一个很长的warn
                defaultProperties.put("logging.level.de.codecentric.boot.admin.server.services.EndpointDetectionTrigger", "error");
                //禁止打印一个没影响的error
                defaultProperties.put("logging.level.org.apache.catalina.connector.CoyoteAdapter", "off");

                //发送邮件提醒通知，发件人必须是邮箱服务器的所属人，
                String springMailUsername = getConfigProByPriority("spring.mail.username", null);
                if(StringUtils.isNotBlank(springMailUsername)){
                    String fromPersonName = "Milepost Spring Boot Admin";//发件人名称
                    defaultProperties.put("spring.boot.admin.notify.mail.from", fromPersonName + " " + "<"+ springMailUsername +">");
                }

                //以下是给SBA Client端的默认配置，也需要配置在SBA Server端，原因同上
                defaultProperties.put("eureka.instance.metadata-map.startup", "${random.int}");

                //SBA Server其实也是一个SBA Client，因为SBA Server本身也被SBA Server监控，
                //但是SBA Server与Service和UI类服务不同，他没有使用Oauth保护，只使用spring-boot-starter-security保护，
                //在SBA Server请求自己本身的监控数据时候，采用Basic Auth，才能沟通过，所以这里也需要与Service和UI类服务类似的方式设置
                defaultProperties.put("eureka.instance.metadata-map.sba_server.user", loginSbaServerUser);
                defaultProperties.put("eureka.instance.metadata-map.sba_server.password", loginSbaServerPassword);

                //实时浏览日志，只有测试环境和生产环境才有日志文件
                if(!springProfilesActive.equalsIgnoreCase("dev")){
                    defaultProperties.put("logging.file", "./logs/"+ springApplicationName +".log");
                    //带颜色的日志没生效
                    //"%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
                    //defaultProperties.put("logging.pattern.file", "[${eureka.instance.instance-id}] %yellow(%d{yyyy-MM-dd HH:mm:ss.SSS}) - [%highlight(%-5level)] [%green(%-50logger{50}): %blue(%-4line)] - %msg%n");
                    //defaultProperties.put("logging.pattern.file", "[${eureka.instance.instance-id}] %clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){yellow} - [%clr(%-5level){highlight}] [%clr(%-50logger{50}){green}: %clr(%-4line){blue}] - %msg%n");
                }


                //暂时用上但是比较有用的
                //修改页面标题
                //spring.boot.admin.ui.title=Milepost-Admin
                //Headers not to be forwarded when making requests to clients。默认值[Cookie, Set-Cookie, Authorization]
                //spring.boot.admin.instance-proxy.ignored-headers=[Cookie, Set-Cookie, Authorization]
                //spring.boot.admin.discovery.ignored-services=example-ui111
            }
        }

        //eureka.instance相关页面地址
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

        defaultProperties.put("eureka.instance.home-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}" + serverServletContextPath);
        defaultProperties.put("eureka.instance.home-page-url-path", serverServletContextPath);
        defaultProperties.put("eureka.instance.health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPath +"${management.endpoints.web.base-path}/health");
        defaultProperties.put("eureka.instance.health-check-url-path",  serverServletContextPath +"${management.endpoints.web.base-path}/health");
        defaultProperties.put("eureka.instance.secure-health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPath +"${management.endpoints.web.base-path}/health");
        defaultProperties.put("eureka.instance.status-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPath +"${management.endpoints.web.base-path}/info");
        defaultProperties.put("eureka.instance.status-page-url-path",  serverServletContextPath +"${management.endpoints.web.base-path}/info");

        //链路跟踪
        String trackEnabled = getConfigProByPriority("track.enabled", "true");//默认开启
        String trackSampling = getConfigProByPriority("track.sampling", "0.1");//默认0.1
        if(Boolean.valueOf(trackEnabled)){
            //开启sleuth,zipkin，必须有RabbitMQ的支持，即必须配置RabbitMQ
            defaultProperties.put("spring.sleuth.web.client.enabled", true);
            defaultProperties.put("spring.sleuth.sampler.probability", Float.valueOf(trackSampling));
            defaultProperties.put("spring.zipkin.sender.type", "rabbit");//强制使用RabbitMQ
        }else{
            //关闭sleuth,zipkin
            defaultProperties.put("sleuth.enabled", false);
            defaultProperties.put("zipkin.enabled", false);
        }

        //eureka.instance.metadata-map
        String infoAppVersion = ReadAppYml.getValue("info.app.version");
        infoAppVersion = StringUtils.isBlank(infoAppVersion)? MilepostConstant.APPLICATION_VERSION : infoAppVersion;//应用版本
        defaultProperties.put("eureka.instance.metadata-map.management.context-path", serverServletContextPath +"${management.endpoints.web.base-path}");
        //共SpringBoot、HystrixTurbine等各种监控系统使用的
        defaultProperties.put("eureka.instance.metadata-map.context-path", serverServletContextPath);

        //从配置文件中读取权重和跟踪采样率，设置默认值，
        // 默认值写在pojo的属性中了，但是这里无法注入，所以只能判断一下，写在pojo中的默认值当作编写yml时的提示，
        String weight = getConfigProByPriority("multiple-tenant.weight", "1");//默认值为1
        //String t1rackS2ampling = getConfigProByPriority("multiple-tenant.trac1k-sa2mpling", "0.1");//默认值为0.1
        defaultProperties.put("eureka.instance.metadata-map.weight", weight);//权重
        defaultProperties.put("eureka.instance.metadata-map.track-sampling", trackSampling);//应用的跟中采样率
        //如果配置文件中设置了租户、标签则读取出来设置到eureka.instance.metadata-map中，
        String labelAnd = getConfigProByPriority("multiple-tenant.label-and", null);
        String labelOr = getConfigProByPriority("multiple-tenant.label-or", null);
        if(StringUtils.isNotBlank(tenant)) defaultProperties.put("eureka.instance.metadata-map.tenant", tenant);//租户
        if(StringUtils.isNotBlank(labelAnd)) defaultProperties.put("eureka.instance.metadata-map.label-and", labelAnd);//与标签
        if(StringUtils.isNotBlank(labelOr)) defaultProperties.put("eureka.instance.metadata-map.label-or", labelOr);//或标签

        //暂时不考虑用户域
        //defaultProperties.put("eureka.instance.metadata-map.user-domain", "DEFAULT");//用户域
        defaultProperties.put("eureka.instance.metadata-map.milepostversion", MilepostConstant.MILEPOST_VERSION);//框架版本
        defaultProperties.put("eureka.instance.metadata-map.version", infoAppVersion);
        defaultProperties.put("eureka.instance.metadata-map.name", "${info.app.name}");
        defaultProperties.put("eureka.instance.metadata-map.description", "${info.app.description}");
        defaultProperties.put("eureka.instance.metadata-map.instance-id", "${eureka.instance.ip-address}:"+ tenant +":${spring.application.name}:${server.port}");

        //维护实例角色
        defaultProperties.put("scheduler-lock.touch-heartbeat-interval-in-milliseconds", 15 * 1000);

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
        //logger.info("processinfo {}", processinfo);
        defaultProperties.put("eureka.instance.metadata-map.pid", processinfo.split("@")[0]);
        defaultProperties.put("eureka.instance.metadata-map.processinfo", ManagementFactory.getRuntimeMXBean().getName());

        //logger.kafka
//        defaultProperties.put("logger.kafka.enabled", "false");
//        defaultProperties.put("logger.kafka.partitions", "20");
//        defaultProperties.put("logger.kafka.replication", "1");

        //service、auth需要flyway和mybatis
        if(MilepostApplicationType.SERVICE.getValue().equalsIgnoreCase(applicationType)
                || MilepostApplicationType.AUTH.getValue().equalsIgnoreCase(applicationType)){
            //JWT和Service
            //flyway，使用这个属性
            String springDatasourcePlatform = getConfigProByPriority("spring.datasource.platform", "mysql");
            defaultProperties.put("spring.datasource.initialize", "false");//禁止spring使用data.sql来初始化。
            defaultProperties.put("spring.flyway.enabled", "true");//默认开启
            defaultProperties.put("spring.flyway.baseline-on-migrate", "true");//当迁移时发现目标schema非空，而且带有没有元数据的表时，是否自动执行基准迁移，默认false.
            //defaultProperties.put("spring.flyway.baseline-description", "init");//对执行迁移时基准版本的描述.
            defaultProperties.put("spring.flyway.baseline-version", "0");//执行基线时用来标记已有Schema的版本，默认值为1.
            defaultProperties.put("spring.flyway.locations", "classpath:db/" + springDatasourcePlatform);//sql脚本存放位置，按照数据库类型区分
            defaultProperties.put("spring.flyway.clean-on-validation-error", "true");//Whether to automatically call clean when a validation error occurs.
            defaultProperties.put("spring.flyway.clean-disabled", "true");//Whether to disable cleaning of the database.
            defaultProperties.put("spring.flyway.table", "flyway_md_" + springApplicationName);//flyway元数据表名称

            //mybatis
            defaultProperties.put("mybatis.config-location", "classpath:mybatis-config.xml");
            defaultProperties.put("mybatis.mapper-locations", "classpath:com/milepost/**/**/dao/*.xml");

            if(MilepostApplicationType.SERVICE.getValue().equalsIgnoreCase(applicationType)){
                //Service类服务的swagger
                if(springProfilesActiveDev.equalsIgnoreCase(springProfilesActive)){
                    //开发(dev)环境中，开启swagger，
                    defaultProperties.put("swagger.enabled", true);
                }else{
                    //生产(prod)环境和测试(test)环境中，关闭swagger，
                    defaultProperties.put("swagger.enabled", false);
                }

                //无论默认值是开启还是关闭，这里都必须要设置默认值，否则当开发者在配置文件中只配置开关时候，没有下面的配置是不行的。
                defaultProperties.put("swagger.authorization.key-name", "Authorization");//swagger-ui调用接口时传入的token请求头名称，值为“Bearer {token}”
                defaultProperties.put("swagger.title", springApplicationName + " swagger");//swagger-ui调用接口时传入的token请求头名称，值为“Bearer {token}”
                defaultProperties.put("swagger.description", springApplicationName + " swagger");//swagger-ui调用接口时传入的token请求头名称，值为“Bearer {token}”

                //分布式事务，tx-client
                //milepost框架为了实现多租户而重写了一个负载均衡策略，并标注了@Primary，所以即时这里开启了，
                //也不会使用LCN的负载均衡(com.codingapi.txlcn.tracing.http.ribbon.TxlcnZoneAvoidanceRule)，
                //以后有时间可以把这个加入进来，目前采取的措施是在代码中避免这种情况的发生。
                defaultProperties.put("tx-lcn.ribbon.loadbalancer.dtx.enabled", false);
                //分布式事务框架存储的业务切面信息。采用的是h2数据库。绝对路径。该参数默认的值为{user.dir}/.txlcn/{application.name}-{application.port}
                //若在同一个服务器上的同一个目录下启动两个实例，则他们会使用同一个h2数据文件，所以增加端口加以区分。
                defaultProperties.put("tx-lcn.aspect.log.file-path", "./tmp/tx-lcn-aspect-log/h2_"+ serverPort +"_log");
            }
        }else{
            //除了Service类服务和JWT服务，都不使用flyway
            defaultProperties.put("spring.flyway.enabled", "false");
        }

        if(MilepostApplicationType.UI.getValue().equalsIgnoreCase(applicationType)){
            //设置UI类服务调用的JWT
            defaultProperties.put("info.app.auth-service.name", "milepost-auth");
            defaultProperties.put("info.app.auth-service.prefix", "/milepost-auth");

            //设置SBA Server端的用户名和密码，即保护SBA Server的security的用户名和密码，是一个写死的值，
            // 这个值也配置在认证UI的元数据中，在认证UI页面点击SBA Server时传入这个用户信息，
            defaultProperties.put("eureka.instance.metadata-map.login_sba_server.user", loginSbaServerUser);
            defaultProperties.put("eureka.instance.metadata-map.login_sba_server.password", loginSbaServerPassword);
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
     * 配置SpringBoot Admin client端相关的默认属性，只要注册到EurekaServer上的服务都需要配置
     * @param defaultProperties
     */
    private void configSpringBootAdminClient(Map<String, Object> defaultProperties, String springProfilesActive, String springApplicationName) {
        //https://codecentric.github.io/spring-boot-admin/2.1.0/#_what_is_spring_boot_admin
        //https://codecentric.github.io/spring-boot-admin/2.1.0/#spring-cloud-discovery-support
        //needed to trigger info and endpoint update after restart
        defaultProperties.put("eureka.instance.metadata-map.startup", "${random.int}");

        //SBA Client放入实例元数据中的用户名和密码，SBA Server抓取到实例元数据并获取到下面的两个配置，
        // 从JWT请求token，然后携带token去SBA Client抓取监控数据，这个用户数据已经写在jwt服务的slq脚本中了
        defaultProperties.put("eureka.instance.metadata-map.sba_server.user", "SBA_Server");
        defaultProperties.put("eureka.instance.metadata-map.sba_server.password", "123456");

        //实时浏览日志，只有测试环境和生产环境才有日志文件
        if(!springProfilesActive.equalsIgnoreCase("dev")){
            defaultProperties.put("logging.file", "./logs/"+ springApplicationName +".log");
            //带颜色的日志没生效
            //"%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID}){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx"
            //defaultProperties.put("logging.pattern.file", "[${eureka.instance.instance-id}] %yellow(%d{yyyy-MM-dd HH:mm:ss.SSS}) - [%highlight(%-5level)] [%green(%-50logger{50}): %blue(%-4line)] - %msg%n");
            //defaultProperties.put("logging.pattern.file", "[${eureka.instance.instance-id}] %clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){yellow} - [%clr(%-5level){highlight}] [%clr(%-50logger{50}){green}: %clr(%-4line){blue}] - %msg%n");
        }
    }

    /**
     * 配置hystrix，配置执行线程超时时间，大于ribbon超时时间
     * @param defaultProperties
     */
    private void configHystrix(Map<String, Object> defaultProperties) {
        defaultProperties.put("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", TimeUnit.SECONDS.toMillis(10));
        //为了方便调试，能看出效果
        //滚动时间窗口内，失败请求数超过此数则断路器开启，默认20
        //defaultProperties.put("hystrix.command.default.circuitBreaker.requestVolumeThreshold", 2);
        //断路器开启后，超过此时间后尝试一次，如请求正常了，则关闭断路器，默认5000
        //defaultProperties.put("hystrix.command.default.circuitBreaker.sleepWindowInMilliseconds", TimeUnit.SECONDS.toMillis(10));
    }

    /**
     * 配置ribbon，关闭重试，配置超时
     * @param defaultProperties
     */
    private void configRibbon(Map<String, Object> defaultProperties) {
        //在同一个服务提供者中重试的次数，不包括第一次请求
        defaultProperties.put("ribbon.MaxAutoRetries", 0);
        //切换其他服务提供者重试的次数，不包括第一次请求
        defaultProperties.put("ribbon.MaxAutoRetriesNextServer", 0);
        //是否所有类型的请求都进行重试操作
        defaultProperties.put("ribbon.OkToRetryOnAllOperations", false);
        //连接超时时间，单位ms，默认1000ms
        defaultProperties.put("ribbon.ConnectTimeout", TimeUnit.SECONDS.toMillis(8));
        //读取超时时间，单位ms，默认1000ms
        defaultProperties.put("ribbon.ReadTimeout", TimeUnit.SECONDS.toMillis(8));
    }

    /**
     * 配置feign，开启hystrix
     * @param defaultProperties
     */
    private void configFeign(Map<String, Object> defaultProperties) {
        //开启hystrix后才可以使用@FeignClient注解的fallback属性和fallbackFactory属性
        defaultProperties.put("feign.hystrix.enabled", true);
    }

    /**
     * 按照SpringBoot读取配置属性的优先级来读取指定属性名的属性值<br>
     * springboot加载配置项的优先级是  命令行参数   >   java系统属性(System.getProperties())    >   操作系统环境变量(System.getenv())   >   配置文件
     *
     * @param key 属性名
     * @param defaultVal 默认值
     * @return
     */
    private String getConfigProByPriority(String key, String defaultVal) {
        String envKey = key.replace(".", "_");
        envKey = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, envKey);
        if(this.argsMap.containsKey(key)){
            return this.argsMap.get(key);
        }else if(System.getenv().containsKey(envKey)){
            return System.getenv().get(envKey);
        }else {
            String value = ReadAppYml.getValue(key);
            if(StringUtils.isBlank(value)){
                return defaultVal;
            }else {
                return value;
            }
        }
    }


    private void runBefore() {
        //printEnv();
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
        Runtime.getRuntime().addShutdownHook(new Thread() {
            //使用SpringBoot的shutdown端点和 kill -15 pid 关闭应用时，如果成功的正常关闭了，就会进入这里，
            public void run() {
                logger.info("------服务关闭成功------");
            }
        });
    }

    private void runAfter() {
        try {
            //打印命令行参数
            this.printArgs();
            //打印Java系统属性
            printJavaProperty();
            //打印操作系统环境变量
            printEnv();
            //打印进程id
            String processinfo = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            logger.info("processinfo {}", processinfo);

            //打印是否支持ssl
            Boolean supportSsl = Boolean.valueOf(System.getProperty("ssl", "false"));
            logger.info("supportSsl=" + supportSsl);

            this.showEurekaInfo();
            started = Boolean.TRUE;
            logger.info("服务启动完毕。");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 打印操作系统的环境变量，<br>
     * Linux环境变量名称中不能包含 .- 等字符，比如server.servlet.context-path=abc等价于server_servlet_contextPath=abc
     */
    private static void printEnv() {
        Map<String, String> envs = System.getenv();
        logger.info("----------操作系统环境变量----------");
        if(envs.size() > 0) {

            Iterator it = envs.keySet().iterator();

            while(it.hasNext()) {
                String name = (String)it.next();
                String value = (String)envs.get(name);
                logger.info("{}={}", name, value);
            }
        }
        logger.info("----------操作系统环境变量----------");
    }

    /**
     * 打印java属性，即-D参数，也叫做VM options。<br>
     *
     * nohup java -Xmx256m -Xms256m \
     *   -Dssl=true -Daa=11 \
     *   -jar ${JAR_NAME} \
     *   --spring.profiles.active=test \
     *   --server.port=8761 \
     *   --eureka.instance.ip-address=192.168.223.129 \
     *   >/dev/null 2>&1 &
     *
     * --参数是命行参数，也叫Program arguments，进入启动类的入参中。
     *
     */
    private static void printJavaProperty(){
        Properties properties = System.getProperties();
        logger.info("----------Java系统属性----------");
        if(properties.size() > 0){

            Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<Object, Object> entry = it.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                logger.info("{}={}", key, value);
            }
        }
        logger.info("----------Java系统属性----------");
    }

    /**
     * 打印命令行参数
     * --参数是命行参数，也叫Program arguments，进入启动类的入参中。
     */
    private void printArgs(){
        logger.info("----------命令行参数----------");
        if(this.argsMap.size() > 0) {

            Iterator it = this.argsMap.keySet().iterator();

            while(it.hasNext()) {
                String name = (String)it.next();
                String value = (String)this.argsMap.get(name);
                logger.info("{}={}", name, value);
            }
        }
        logger.info("----------命令行参数----------");
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
            * defaultProperties.put("eureka.instance.home-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}" + serverServletContextPath);
            defaultProperties.put("eureka.instance.home-page-url-path", serverServletContextPath);
            defaultProperties.put("eureka.instance.health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPath +"${management.endpoints.web.base-path}/health");
            defaultProperties.put("eureka.instance.health-check-url-path",  serverServletContextPath +"${management.endpoints.web.base-path}/health");
            defaultProperties.put("eureka.instance.secure-health-check-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPath +"${management.endpoints.web.base-path}/health");
            defaultProperties.put("eureka.instance.status-page-url", protocol + "://${eureka.instance.ip-address}:${server.port}"+ serverServletContextPath +"${management.endpoints.web.base-path}/info");
            defaultProperties.put("eureka.instance.status-page-url-path",  serverServletContextPath +"${management.endpoints.web.base-path}/info");

            * */

        }
    }

    /**
     * 当前应用是否是EurekaServer，
     * @return 是则返回true
     */
    public boolean isEurekaServer() {
        String appType = (String)(this.customProperties.get(MilepostConstant.MILEPOST_APPLICATION_TYPE_KEY));
        return MilepostApplicationType.EUREKA.getValue().equals(appType);
    }
}




