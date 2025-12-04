package org.docshare.yangmvc.spring.config;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.log.Log;
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
    
    @Autowired
    private YangMvcProperties yangMvcSimpleProperties;
    
    @Override
    public void run(String... args) throws Exception {
        Log.i("Starting YangMVC configuration initialization...");
        
        try {
            // 将HashMap配置转换为InputStream，然后传给Config类
            InputStream configStream = yangMvcSimpleProperties.toPropertiesInputStream();
            
            // 使用新的loadProperties(InputStream)方法加载配置
            boolean success = Config.loadPropertiesFromStream(configStream);
            
            if (success) {
                Log.i("YangMVC configuration initialized successfully!");
                Log.i("Configuration item count: " + yangMvcSimpleProperties.size());
                
                // 打印一些关键配置信息
                printKeyConfigs();
            } else {
                Log.e("YangMVC configuration initialization failed!");
            }

        } catch (Exception e) {
            Log.e("Exception occurred during YangMVC configuration initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印关键配置信息
     */
    private void printKeyConfigs() {
        Log.i("=== YangMVC Key Configuration Information ===");
        
        String[] keyConfigs = {
            "controller", "template", "dbhost", "dbusr", "dbname", 
            "dbport", "dbtype", "encoding", "reloadable","conn_pool"
        };
        
        for (String key : keyConfigs) {
            String value = yangMvcSimpleProperties.getProperty(key);
            if (value != null) {
                Log.i(key + " = " + value);
            }
        }
        
        Log.i("=========================");
    }
    
    /**
     * 获取配置属性对象
     * @return YangMvcSimpleProperties实例
     */
    public YangMvcProperties getYangMvcSimpleProperties() {
        return yangMvcSimpleProperties;
    }
}