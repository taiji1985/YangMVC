package org.docshare.yangmvc.spring.config;

import org.docshare.mvc.Config;
import org.docshare.log.Log;
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
        Log.i("开始初始化YangMVC配置...");
        
        try {
            // 将HashMap配置转换为InputStream，然后传给Config类
            InputStream configStream = yangMvcSimpleProperties.toPropertiesInputStream();
            
            // 使用新的loadProperties(InputStream)方法加载配置
            boolean success = Config.loadPropertiesFromStream(configStream);
            
            if (success) {
                Log.i("YangMVC配置初始化成功！");
                Log.i("配置项数量: " + yangMvcSimpleProperties.size());
                
                // 打印一些关键配置信息
                printKeyConfigs();
            } else {
                Log.e("YangMVC配置初始化失败！");
            }
            
        } catch (Exception e) {
            Log.e("YangMVC配置初始化过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 打印关键配置信息
     */
    private void printKeyConfigs() {
        Log.i("=== YangMVC关键配置信息 ===");
        
        String[] keyConfigs = {
            "controller", "template", "dbhost", "dbusr", "dbname", 
            "dbport", "dbtype", "encoding", "reloadable"
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