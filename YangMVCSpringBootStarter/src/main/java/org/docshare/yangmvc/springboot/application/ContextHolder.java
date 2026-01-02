package org.docshare.yangmvc.springboot.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Spring ApplicationContext持有者
 * <p>
 * 这个类用于在非Spring管理的类中访问Spring ApplicationContext，
 * 特别是为YangMVC的ORM层提供Spring数据源的访问
 * <p>
 * 通过YangMvcAutoConfiguration在应用启动时初始化
 *
 * @author YangMVC
 */
public class ContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(ContextHolder.class);

    private static ApplicationContext applicationContext;

    /**
     * 私有构造函数，防止实例化
     */
    private ContextHolder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 设置Spring ApplicationContext
     * <p>
     * 这个方法应该在应用启动时被调用，通常通过配置类自动调用
     *
     * @param context Spring ApplicationContext
     */
    public static void setApplicationContext(ApplicationContext context) {
        if (context == null) {
            logger.warn("Attempted to set null ApplicationContext");
            return;
        }
        applicationContext = context;
        logger.info("Spring ApplicationContext has been set in ContextHolder");
        logger.debug("ApplicationContext: {}", context.getClass().getName());
        logger.debug("Total beans: {}", context.getBeanDefinitionCount());
    }

    /**
     * 获取Spring ApplicationContext
     *
     * @return ApplicationContext实例，如果未设置返回null
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 获取指定类型的Bean
     *
     * @param clazz Bean的类类型
     * @param <T>   Bean的类型
     * @return Bean实例，如果不存在返回null
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            logger.warn("ApplicationContext not initialized, cannot get bean of type: {}", clazz.getName());
            return null;
        }

        try {
            return applicationContext.getBean(clazz);
        } catch (Exception e) {
            logger.warn("Failed to get bean of type: {}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 获取指定名称的Bean
     *
     * @param name Bean的名称
     * @param <T>   Bean的类型
     * @return Bean实例，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        if (applicationContext == null) {
            logger.warn("ApplicationContext not initialized, cannot get bean with name: {}", name);
            return null;
        }

        try {
            return (T) applicationContext.getBean(name);
        } catch (Exception e) {
            logger.warn("Failed to get bean with name: {}", name, e);
            return null;
        }
    }

    /**
     * 检查ApplicationContext是否已初始化
     *
     * @return 如果已初始化返回true，否则返回false
     */
    public static boolean isInitialized() {
        return applicationContext != null;
    }
}
