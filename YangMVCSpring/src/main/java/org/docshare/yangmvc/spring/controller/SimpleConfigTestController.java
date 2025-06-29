package org.docshare.yangmvc.spring.controller;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.spring.config.YangMvcProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化配置测试控制器
 * 用于测试HashMap方式的配置加载和管理
 * 
 * @author YangMVC
 */
@RestController
@RequestMapping("/simple-config")
public class SimpleConfigTestController {
    
    @Autowired
    private YangMvcProperties yangMvcSimpleProperties;
    
    /**
     * 获取Spring Boot中加载的简化配置
     */
    @GetMapping("/spring")
    public Map<String, Object> getSpringConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("config_size", yangMvcSimpleProperties.size());
        result.put("all_properties", yangMvcSimpleProperties.getAllProperties());
        return result;
    }
    
    /**
     * 获取YangMVC Config类中的配置
     */
    @GetMapping("/yangmvc")
    public Map<String, Object> getYangMvcConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("controller", Config.controller);
        result.put("template", Config.template);
        result.put("dbhost", Config.dbhost);
        result.put("dbusr", Config.dbusr);
        result.put("dbname", Config.dbname);
        result.put("dbport", Config.dbport);
        result.put("dbtype", Config.dbtype);
        result.put("encoding", Config.encoding);
        result.put("reloadable", Config.reloadable);
        return result;
    }
    
    /**
     * 获取指定配置项的值
     */
    @GetMapping("/property/{key}")
    public Map<String, Object> getProperty(@PathVariable String key) {
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("spring_value", yangMvcSimpleProperties.getProperty(key));
        result.put("yangmvc_value", Config.getProperty(key, "未找到"));
        return result;
    }
    
    /**
     * 动态设置配置项
     */
    @PostMapping("/property/{key}")
    public Map<String, Object> setProperty(@PathVariable String key, @RequestBody String value) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 设置到Spring配置中
            yangMvcSimpleProperties.setProperty(key, value);
            
            // 重新加载到YangMVC Config中
            InputStream configStream = yangMvcSimpleProperties.toPropertiesInputStream();
            boolean success = Config.loadPropertiesFromStream(configStream);
            
            result.put("success", success);
            result.put("key", key);
            result.put("value", value);
            result.put("message", success ? "配置更新成功" : "配置更新失败");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 测试配置同步
     */
    @GetMapping("/sync-test")
    public Map<String, Object> testConfigSync() {
        Map<String, Object> result = new HashMap<>();
        
        // 测试基本配置同步
        String[] testKeys = {"controller", "template", "dbhost", "encoding", "reloadable"};
        Map<String, Boolean> syncResults = new HashMap<>();
        
        for (String key : testKeys) {
            String springValue = yangMvcSimpleProperties.getProperty(key);
            String yangmvcValue = Config.getProperty(key, null);
            
            boolean synced = (springValue != null && springValue.equals(yangmvcValue)) ||
                           (springValue == null && yangmvcValue == null);
            syncResults.put(key, synced);
        }
        
        result.put("sync_results", syncResults);
        result.put("total_properties", yangMvcSimpleProperties.size());
        
        return result;
    }
    
    /**
     * 重新加载配置
     */
    @PostMapping("/reload")
    public Map<String, Object> reloadConfig() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            InputStream configStream = yangMvcSimpleProperties.toPropertiesInputStream();
            boolean success = Config.loadPropertiesFromStream(configStream);
            
            result.put("success", success);
            result.put("message", success ? "配置重新加载成功" : "配置重新加载失败");
            result.put("config_size", yangMvcSimpleProperties.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    

}