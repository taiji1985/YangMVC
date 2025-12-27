package org.docshare.yangmvc.springboot.autoconfigure;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.springboot.filter.DelayedInitMVCFilter;
import org.docshare.yangmvc.springboot.properties.YangMvcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * YangMVC Filter自动配置类
 * <p>
 * 这个类负责在Spring Boot环境中注册YangMVC的MVCFilter，
 * 并将其初始化为Spring Boot的FilterRegistrationBean
 * <p>
 * Filter会被配置为拦截所有请求（/*），并设置为最低优先级，
 * 确保其他Spring Security等Filter可以先执行
 *
 * @author YangMVC
 */
@Configuration
@ConditionalOnClass(DelayedInitMVCFilter.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(YangMvcProperties.class)
public class YangMvcFilterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(YangMvcFilterConfiguration.class);

    /**
     * 注册YangMVC的延迟初始化Filter
     * <p>
     * 使用延迟初始化的Filter包装器，确保：
     * 1. 配置在Filter使用前已加载
     * 2. Filter初始化在第一次请求时才发生
     *
     * @return FilterRegistrationBean实例
     */
    @Bean
    public FilterRegistrationBean<DelayedInitMVCFilter> yangMvcFilterRegistration() {
        logger.info("Creating YangMVC DelayedInit Filter Registration Bean...");

        // 创建延迟初始化Filter
        DelayedInitMVCFilter filter = new DelayedInitMVCFilter();

        // 创建FilterRegistrationBean
        FilterRegistrationBean<DelayedInitMVCFilter> registration = new FilterRegistrationBean<>(filter);

        // 配置Filter
        registration.addUrlPatterns("/*");  // 拦截所有请求
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);  // 最低优先级，最后执行
        registration.setName("yangMvcFilter");
        registration.setAsyncSupported(true);  // 支持异步请求

        logger.info("YangMVC DelayedInit Filter Registration Bean created successfully");
        return registration;
    }

    /**
     * 初始化YangMVC配置和Filter
     * <p>
     * 这个Runner会在应用启动后执行，确保：
     * 1. YangMVC配置已加载
     * 2. MVCFilter已正确初始化
     *
     * @param properties          YangMVC配置属性
     * @param dataSourceProvider  Spring Boot数据源（可选）
     * @return CommandLineRunner实例
     */
    @Bean
    public CommandLineRunner yangMvcInitializer(
            YangMvcProperties properties,
            ObjectProvider<DataSource> dataSourceProvider) {

        return args -> {
            logger.info("Initializing YangMVC...");

            // 检查Spring Boot是否配置了DataSource
            if (dataSourceProvider.getIfAvailable() != null) {
                String connPoolClass = "org.docshare.yangmvc.springboot.pool.SpringBootConnectionPool";
                properties.setConnPool(connPoolClass);
                properties.setProperty("conn_pool", connPoolClass);
                logger.info("Spring Boot DataSource detected, will use SpringBootConnectionPool");
            }

            // 加载配置
            try (InputStream configStream = properties.toPropertiesInputStream()) {
                boolean success = Config.loadPropertiesFromStream(configStream);

                if (success) {
                    logger.info("YangMVC configuration loaded successfully");
                    logger.debug("YangMVC Config: {}", Config.str());

                    // 记录关键配置信息
                    if (Config.controller != null) {
                        logger.info("YangMVC controller package: {}", Config.controller);
                    }
                    if (Config.template != null) {
                        logger.info("YangMVC template directory: {}", Config.template);
                    }
                    logger.info("YangMVC reloadable: {}", Config.reloadable);
                } else {
                    logger.warn("Failed to load YangMVC configuration");
                }
            } catch (Exception e) {
                logger.error("Error loading YangMVC configuration", e);
                throw e;
            }

            // 尝试加载Init类（如果存在）
            loadInitClassIfPresent();

            logger.info("YangMVC initialization completed!");
        };

    }

    /**
     * 尝试加载Init类
     * <p>
     * 如果在controller包下存在Init类，则实例化它
     */
    private void loadInitClassIfPresent() {
        if (Config.controller == null || Config.controller.isEmpty()) {
            logger.debug("No controller package configured, skipping Init class loading");
            return;
        }

        String initClassName = Config.controller + ".Init";
        try {
            Class<?> initClass = Class.forName(initClassName);
            Object initInstance = initClass.newInstance();
            logger.info("YangMVC Init class loaded: {}", initClassName);
        } catch (ClassNotFoundException e) {
            logger.debug("No Init class found at: {}", initClassName);
        } catch (Exception e) {
            logger.warn("Failed to load Init class: {}", initClassName, e);
        }
    }
}
