package com.milepost.test;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Ruifu Hua on 2020/2/13.
 * 单元测试类，调用启动类的main方法，获取ioc容器中的bean，测试，然后关闭应用。
 */
public class BaseTest<T> {

    private ConfigurableApplicationContext context = null;

    /**
     * 启动应用，子类的@Before在此方法之后运行
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Before
    public void startApp() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Type baseTestType = getClass().getGenericSuperclass();//BaseTest
        Type genericType = ((ParameterizedType) baseTestType).getActualTypeArguments()[0];//泛型
        Class genericClass = Class.forName(genericType.getTypeName());//泛型class
        Class genericSuperClass = Class.forName(genericClass.getSuperclass().getTypeName());//泛型的父类
        Method[] genericSuperclassMethods = genericSuperClass.getMethods();
        for(Method method : genericSuperclassMethods){
            if("run".equals(method.getName())){
                context = (ConfigurableApplicationContext)method.invoke(null, genericClass, new String[]{});
            }
        }
    }

    /**
     * 关闭应用，子类的@After在此方法之前运行
     */
    @After
    public void stopApp(){
        context.stop();
        context.close();
    }

    public Object getBean(String name){
        return context.getBean(name);
    }

    public <T> T getBean(Class<T> requiredType){
        return context.getBean(requiredType);
    }

    public <T> T getBean(String name, Class<T> requiredType){
        return context.getBean(name, requiredType);
    }

    public String getProperty(String key){
        return context.getEnvironment().getProperty(key);
    }

    public String getProperty(String key, String defaultValue){
        return context.getEnvironment().getProperty(key, defaultValue);
    }

    public <T> T getProperty(String key, Class<T> targetType){
        return context.getEnvironment().getProperty(key, targetType);
    }

    public <T> T getProperty(String key, Class<T> targetType, T defaultValue){
        return context.getEnvironment().getProperty(key, targetType, defaultValue);
    }
}
