package com.milepost.core.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * 线程池<br>
 * 如果不配置，则SpringBoot默认使用SimpleAsyncTaskExecutor
 */
@Configuration
@EnableAsync
public class ThreadPoolTaskExecutorConfig implements AsyncConfigurer {

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolTaskExecutorConfig.class);

    @Autowired
    private ThreadPoolProperties threadPoolProperties;

    /**
     * 实例化线程池任务执行器
     */
    private static final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

    /**
     * 定义线程池<br>
     * 配置详解：（https://www.iteye.com/blog/wy16505-2123558）<br>
     * 当一个任务通过execute(Runnable)方法欲添加到线程池时：<br>
     * 1、 如果此时线程池中的数量小于corePoolSize，即使线程池中的线程都处于空闲状态，也要创建新的线程来处理被添加的任务。<br>
     * 2、 如果此时线程池中的数量等于corePoolSize，但是缓冲队列 workQueue未满，那么任务被放入缓冲队列。<br>
     * 3、 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量小于maximumPoolSize，建新的线程来处理被添加的任务。<br>
     * 4、 如果此时线程池中的数量大于corePoolSize，缓冲队列workQueue满，并且线程池中的数量等于maximumPoolSize，那么通过 handler所指定的策略来处理此任务。<br>
     * 也就是：处理任务的优先级为：核心线程corePoolSize、任务队列workQueue、最大线程 maximumPoolSize，如果三者都满了，使用handler处理被拒绝的任务。
     * 5、 当线程池中的线程数量大于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止。这样，线程池可以动态的调整池中的线程数。<br>
     *
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        logger.info("初始化线程池...");
        logger.info(
                "核心线程="+ threadPoolProperties.getCorePoolSize() +"，" +
                "最大线程="+ threadPoolProperties.getMaxPoolSize() +"，" +
                "队列线程="+ threadPoolProperties.getQueueCapacity() +"，" +
                "线程存活时间="+ threadPoolProperties.getKeepAliveSeconds() +"秒。");

        // 核心(最小)线程数，默认值1
        threadPoolTaskExecutor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        // 线程池最大线程数，默认值Integer.MAX_VALUE
        threadPoolTaskExecutor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        // 线程队列最大线程数，默认值Integer.MAX_VALUE
        threadPoolTaskExecutor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        // 线程所允许的空闲时间，默认60s
        threadPoolTaskExecutor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        // 初始化
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }

    /**
     * 异常处理
     *
     * @return
     */
    @Nullable
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
                logger.error("------线程池异常------");
                logger.error("--异常信息：" + throwable.getMessage());
                logger.error("--方法签名：" + method.toString());
                for(int i=0; i<params.length; i++){
                    Object param = params[i];
                    if(param != null){
                        logger.error("----参数"+ (i+1) +"=" + param.toString());
                    }else{
                        logger.error("----参数"+ (i+1) +"=" + param);
                    }
                }
                logger.error("------线程池异常------");
            }
        };
    }

    /**
     * 获取线程池剩余线程数
     *
     * @return
     */
    public static int getResiduePoolSize() {
        int residuePoolSize = 0;
        residuePoolSize = threadPoolTaskExecutor.getMaxPoolSize() - threadPoolTaskExecutor.getActiveCount();
        return residuePoolSize;
    }

    /**
     * 打印线程池信息
     */
    public static void printThreadPoolInfo(){
        logger.info(
            "核心线程="+ ThreadPoolTaskExecutorConfig.getThreadPoolTaskExecutor().getCorePoolSize() +"，" +
            "最大线程="+ ThreadPoolTaskExecutorConfig.getThreadPoolTaskExecutor().getMaxPoolSize() +"，" +
            "活跃线程="+ ThreadPoolTaskExecutorConfig.getThreadPoolTaskExecutor().getActiveCount() +"，" +
            "剩余线程="+ ThreadPoolTaskExecutorConfig.getResiduePoolSize() +"。");
    }

    /**
     * 获取线程池执行器
     *
     * @return
     */
    public static ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        return threadPoolTaskExecutor;
    }

    /**
     * 当应用关闭时，关闭线程池，否则不能正常释放资源，即不能优雅关闭服务后进程还存在。
     * @param event
     */
    @EventListener
    public void onContextClosed(ContextClosedEvent event){
        threadPoolTaskExecutor.shutdown();
        logger.info("关闭线程池...");
    }
}

