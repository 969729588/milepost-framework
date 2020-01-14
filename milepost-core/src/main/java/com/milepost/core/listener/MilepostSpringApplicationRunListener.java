package com.milepost.core.listener;

import com.milepost.core.banner.PrintBanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Created by Ruifu Hua on 2020/1/14.
 * SpringApplicationRunListener
 */
public class MilepostSpringApplicationRunListener implements SpringApplicationRunListener {

    private SpringApplication application;
    private String[] args;


    public MilepostSpringApplicationRunListener(SpringApplication application, String[] args) {
        this.application = application;
        this.args = args;
    }

    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        //打印banner
        PrintBanner printBanner = new PrintBanner(application, environment);
        printBanner.print();
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }

}

