package org.docshare.yangmvc.spring.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring上下文工具类
 * 用于在非托管对象中获取Spring容器中的托管对象
 * 
 * 使用方法：
 * 1. 根据类型获取Bean：SpringContextUtil.getBean(YourService.class)
 * 2. 根据名称获取Bean：SpringContextUtil.getBean("yourServiceName")
 * 3. 根据名称和类型获取Bean：SpringContextUtil.getBean("yourServiceName", YourService.class)
 * 
 * @author YangMVC
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }
    
    /**
     * 获取ApplicationContext
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * 根据Bean名称获取对象
     * @param name Bean名称
     * @return Bean对象
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }
    
    /**
     * 根据Bean类型获取对象
     * @param clazz Bean类型
     * @param <T> 泛型类型
     * @return Bean对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }
    
    /**
     * 根据Bean名称和类型获取对象
     * @param name Bean名称
     * @param clazz Bean类型
     * @param <T> 泛型类型
     * @return Bean对象
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
    
    /**
     * 检查是否包含指定名称的Bean
     * @param name Bean名称
     * @return 是否包含
     */
    public static boolean containsBean(String name) {
        return getApplicationContext().containsBean(name);
    }
    
    /**
     * 判断Bean是否为单例
     * @param name Bean名称
     * @return 是否为单例
     */
    public static boolean isSingleton(String name) {
        return getApplicationContext().isSingleton(name);
    }
    
    /**
     * 获取Bean的类型
     * @param name Bean名称
     * @return Bean类型
     */
    public static Class<?> getType(String name) {
        return getApplicationContext().getType(name);
    }
}