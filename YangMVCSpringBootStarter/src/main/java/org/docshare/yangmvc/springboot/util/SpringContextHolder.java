package org.docshare.yangmvc.springboot.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring ApplicationContext持有者类
 * <p>
 * 这个类实现了ApplicationContextAware接口，用于在非Spring管理的类中
 * 获取Spring容器中的Bean。这对于YangMVC的Controller等类非常有用，
 * 因为它们可能需要访问Spring管理的Bean（如DataSource、Service等）
 *
 * @author YangMVC
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SpringContextHolder.class);

    private static ApplicationContext applicationContext;

    /**
     * 设置ApplicationContext
     * <p>
     * 这个方法会在Spring容器启动时自动调用
     *
     * @param context ApplicationContext实例
     * @throws BeansException 如果设置失败
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext != null) {
            logger.warn("SpringContextHolder is being overwritten, this might be a problem");
        }
        SpringContextHolder.applicationContext = context;
        logger.info("SpringContextHolder initialized successfully");
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext实例
     */
    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    /**
     * 根据类型获取Bean
     *
     * @param clazz Bean的类型
     * @param <T>   Bean的类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            logger.error("Failed to get bean of type: {}", clazz.getName(), e);
            throw e;
        }
    }

    /**
     * 根据名称获取Bean
     *
     * @param name Bean的名称
     * @return Bean实例
     */
    public static Object getBean(String name) {
        assertContextInjected();
        try {
            return applicationContext.getBean(name);
        } catch (BeansException e) {
            logger.error("Failed to get bean with name: {}", name, e);
            throw e;
        }
    }

    /**
     * 根据名称和类型获取Bean
     *
     * @param name  Bean的名称
     * @param clazz Bean的类型
     * @param <T>   Bean的类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        assertContextInjected();
        try {
            return applicationContext.getBean(name, clazz);
        } catch (BeansException e) {
            logger.error("Failed to get bean with name: {} and type: {}", name, clazz.getName(), e);
            throw e;
        }
    }

    /**
     * 根据类型获取所有Bean
     *
     * @param clazz Bean的类型
     * @param <T>   Bean的类型
     * @return 所有匹配的Bean实例
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBeansOfType(clazz);
    }

    /**
     * 根据类型获取所有Bean的列表
     *
     * @param clazz Bean的类型
     * @param <T>   Bean的类型
     * @return 所有匹配的Bean实例列表
     */
    public static <T> List<T> getBeansList(Class<T> clazz) {
        assertContextInjected();
        Map<String, T> beans = applicationContext.getBeansOfType(clazz);
        return new ArrayList<>(beans.values());
    }

    /**
     * 检查是否包含指定名称的Bean
     *
     * @param name Bean的名称
     * @return 如果存在返回true，否则返回false
     */
    public static boolean containsBean(String name) {
        assertContextInjected();
        return applicationContext.containsBean(name);
    }

    /**
     * 检查指定名称的Bean是否是单例
     *
     * @param name Bean的名称
     * @return 如果是单例返回true，否则返回false
     */
    public static boolean isSingleton(String name) {
        assertContextInjected();
        return applicationContext.isSingleton(name);
    }

    /**
     * 获取Bean的类型
     *
     * @param name Bean的名称
     * @return Bean的类型
     */
    public static Class<?> getType(String name) {
        assertContextInjected();
        return applicationContext.getType(name);
    }

    /**
     * 检查ApplicationContext是否已注入
     *
     * @throws IllegalStateException 如果ApplicationContext未注入
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext has not been injected yet. " +
                "Make sure SpringContextHolder is registered as a Spring component.");
        }
    }

    /**
     * 清除ApplicationContext
     * <p>
     * 这个方法主要用于测试目的
     */
    public static void clear() {
        logger.debug("Clearing SpringContextHolder");
        applicationContext = null;
    }
}
