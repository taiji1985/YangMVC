package org.docshare.yangmvc.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 简化的YangMVC配置属性类
 * 使用HashMap存储所有配置项，支持动态配置
 * 
 * @author YangMVC
 */
@Data
@ConfigurationProperties(prefix = "yangmvc")
public class YangMvcProperties {
    
    /**
     * 所有配置项的Map
     */
    private Map<String, String> config = new HashMap<>();
    
    /**
     * 获取配置值
     * @param key 配置键
     * @return 配置值
     */
    public String getProperty(String key) {
        return config.get(key);
    }
    
    /**
     * 获取配置值，如果不存在则返回默认值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String getProperty(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    /**
     * 设置配置值
     * @param key 配置键
     * @param value 配置值
     */
    public void setProperty(String key, String value) {
        config.put(key, value);
    }
    
    /**
     * 将配置Map转换为Properties格式的InputStream
     * 这样可以直接传给Config.loadProperties(InputStream)方法
     * @return Properties格式的InputStream
     */
    public InputStream toPropertiesInputStream() {
        Properties properties = new Properties();
        properties.putAll(config);
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            sb.append(entry.getKey())
              .append("=")
              .append(entry.getValue())
              .append("\n");
        }
        
        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 从Properties对象加载配置
     * @param properties Properties对象
     */
    public void loadFromProperties(Properties properties) {
        config.clear();
        for (String key : properties.stringPropertyNames()) {
            config.put(key, properties.getProperty(key));
        }
    }
    
    /**
     * 获取所有配置项
     * @return 配置Map的副本
     */
    public Map<String, String> getAllProperties() {
        return new HashMap<>(config);
    }
    
    /**
     * 清空所有配置
     */
    public void clear() {
        config.clear();
    }
    
    /**
     * 检查是否包含指定的配置键
     * @param key 配置键
     * @return 是否包含
     */
    public boolean containsKey(String key) {
        return config.containsKey(key);
    }
    
    /**
     * 获取配置项数量
     * @return 配置项数量
     */
    public int size() {
        return config.size();
    }
}