package com.milepost.core.lns;

import com.milepost.api.util.EncryptionUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by Ruifu Hua on 2020/2/3.
 */
@Aspect
@Component
@Order(99999)
public class LiceAspect {

    private static final Logger logger = LoggerFactory.getLogger(LiceAspect.class);

    private static final Logger rootLogger = LoggerFactory.getLogger("ROOT");

    @Value("${spring.application.name}")
    public String product;
    private static boolean isPrint;
    long lastTimeMillis = 0L;

    static {
        isPrint = Boolean.FALSE.booleanValue();
    }

    @Pointcut("@annotation(com.milepost.core.lns.LiceService)")
    public void advice() {
    }

    /**
     * 注解
     * @param joinPoint
     * @throws LiceE
     */
    @Before("advice()")
    public void servicelic(JoinPoint joinPoint) throws LiceE {
        this.check(joinPoint);
    }

    /**
     * service的资源服务器拦截器
     * @param joinPoint
     */
    @Before("execution(* com.milepost.service.config.*.ResourceServerConfig.*(..))")
    public void serviceAuthFilter(JoinPoint joinPoint) {
        this.check(joinPoint);
    }

    /**
     * ui的资源服务器拦截器
     * @param joinPoint
     */
    @Before("execution(* com.milepost.ui.config.*.ResourceServerConfig.*(..))")
    public void uiAuthFilter(JoinPoint joinPoint) {
        this.check(joinPoint);
    }

    /**
     * @RestController注解
     * @param joinPoint
     */
    @Before("@within(org.springframework.web.bind.annotation.RestController)")
    public void service(JoinPoint joinPoint) {
        this.check(joinPoint);
    }

    /**
     * @Controller注解
     * @param joinPoint
     */
    @Before("@within(org.springframework.stereotype.Controller)")
    public void controller(JoinPoint joinPoint) {
        this.check(joinPoint);
    }

    private void check(JoinPoint joinPoint) {
        //一个系统，第一次进来时，和两次进来的时间间隔大于10000ms时，都要检测lice是否过期，
        if(this.lastTimeMillis == 0L || System.currentTimeMillis() - this.lastTimeMillis > 10000L) {
            try {
                //获取lice管理器
                LiceM liceM = LiceM.getInstance();
                Lice lice = liceM.getLice();//获取lice
                liceM.isValidLice(lice, this.product);
                //只打印一次日志
                if(isPrint == Boolean.FALSE.booleanValue()) {
                    List<String> liceinfo = lice.getInfo();
                    Iterator liceinfoIt = liceinfo.iterator();

                    while(liceinfoIt.hasNext()) {
                        String line = (String)liceinfoIt.next();
                        rootLogger.info(line);
                    }

                    isPrint = Boolean.TRUE.booleanValue();
                }
            } catch (Exception e) {
                String line = "";
                try {
                    line = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.line));
                }catch (Exception e1){
                    logger.error(e1.getMessage(), e1);
                }

                rootLogger.info(line);
                rootLogger.info(e.getMessage());
                rootLogger.info(line);
                Random random = new Random(10000L);//传入seed，是random获取的随机数序列在每次运行程序时都是一样的

                try {
                    //random.nextInt(bound);获取[0,bound)之间的整数
                    int a = random.nextInt(5000) + 5000;
                    Thread.sleep((long)a);
                } catch (InterruptedException e2) {
                    logger.warn("Interrupted!", e2);
                    Thread.currentThread().interrupt();
                }
            } finally {
                this.lastTimeMillis = System.currentTimeMillis();
            }
        }
    }
}
