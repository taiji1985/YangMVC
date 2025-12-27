package org.docshare.yangmvc.spring.config;

import org.docshare.yangmvc.mvc.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 简化的YangMVC配置初始化器
 * 使用HashMap方式加载配置到YangMVC的Config类
 * 
 * @author YangMVC
 */
@Component
public class YangMvcConfigInitializer implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(YangMvcConfigInitializer.class);

    
    @Autowired
    private YangMvcProperties yangMvcSimpleProperties;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting YangMVC configuration initialization...");
        
        try {
            // 将HashMap配置转换为InputStream，然后传给Config类
            InputStream configStream = yangMvcSimpleProperties.toPropertiesInputStream();
            
            // 使用新的loadProperties(InputStream)方法加载配置
            boolean success = Config.loadPropertiesFromStream(configStream);
            
            if (success) {
                logger.info("YangMVC configuration initialized successfully!");
                logger.info("Configuration item count: " + yangMvcSimpleProperties.size());
                
                // 打印一些关键配置信息
                printKeyConfigs();
            } else {
                logger.error("YangMVC configuration initialization failed!");
            }

        } catch (Exception e) {
            logger.error("Exception occurred during YangMVC configuration initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印关键配置信息
     */
    private void printKeyConfigs() {
        logger.info("=== YangMVC Key Configuration Information ===");
        
        String[] keyConfigs = {
            "controller", "template", "dbhost", "dbusr", "dbname", 
            "dbport", "dbtype", "encoding", "reloadable","conn_pool"
        };
        
        for (String key : keyConfigs) {
            String value = yangMvcSimpleProperties.getProperty(key);
            if (value != null) {
                logger.info("{}", key + " = " + value);
            }
        }
        
        logger.info("=========================");
    }
    
    /**
     * 获取配置属性对象
     * @return YangMvcSimpleProperties实例
     */
    public YangMvcProperties getYangMvcSimpleProperties() {
        return yangMvcSimpleProperties;
    }
}