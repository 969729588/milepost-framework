package com.milepost.core.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by Ruifu Hua on 2019/12/31.
 * 获取ApplicationContext
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware{
    private static ApplicationContext applicationContext;

    public ApplicationContextProvider() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    public static ApplicationContext getContext() {
        return ApplicationContextProvider.applicationContext;
    }

    public static <T> T getBean(String name) throws BeansException {
        return (T) applicationContext.getBean(name);
    }
}
