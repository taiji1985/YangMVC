package org.docshare.yangmvc.springboot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * YangMVC配置属性类
 * <p>
 * 通过Spring Boot的@ConfigurationProperties机制，从application.yml/properties中加载yangmvc前缀的配置
 * <p>
 * 支持两种配置方式：
 * 1. 使用config Map进行细粒度配置
 * 2. 直接使用预定义的属性（如controller、template等）
 *
 * @author YangMVC
 */
@Data
@ConfigurationProperties(prefix = "yangmvc")
public class YangMvcProperties {

    /**
     * 所有配置项的Map
     * <p>
     * 可以在application.yml中这样配置：
     * <pre>
     * yangmvc:
     *   config:
     *     encoding: utf-8
     *     reloadable: true
     * </pre>
     */
    private Map<String, String> config = new HashMap<>();

    /**
     * 控制器根包名（必需）
     * <p>
     * 示例：yangmvc.controller=com.example.controllers
     */
    private String controller;

    /**
     * 模板目录路径
     * <p>
     * 默认值：/view
     * 示例：yangmvc.template=/WEB-INF/views
     */
    private String template = "/view";

    /**
     * 编码格式
     * <p>
     * 默认值：utf-8
     */
    private String encoding = "utf-8";

    /**
     * 页面编码
     * <p>
     * 默认值：utf-8
     */
    private String pageEncoding = "utf-8";

    /**
     * 是否支持热加载
     * <p>
     * 默认值：true（开发环境推荐）
     */
    private Boolean reloadable = true;

    /**
     * 是否重新加载配置
     * <p>
     * 默认值：false
     */
    private Boolean reloadConfig = false;

    /**
     * 数据库主机
     * <p>
     * 默认值：localhost
     */
    private String dbhost = "localhost";

    /**
     * 数据库用户名
     * <p>
     * 默认值：root
     */
    private String dbusr = "root";

    /**
     * 数据库密码
     * <p>
     * 默认值：空字符串
     */
    private String dbpwd = "";

    /**
     * 数据库名称
     */
    private String dbname = "";

    /**
     * 数据库端口
     * <p>
     * 默认值：3306
     */
    private String dbport = "3306";

    /**
     * 数据库类型
     * <p>
     * 默认值：mysql
     */
    private String dbtype = "mysql";

    /**
     * 数据库schema
     * <p>
     * 默认值：public
     */
    private String dbschema = "public";

    /**
     * 数据库编码
     * <p>
     * 默认值：utf8
     */
    private String dbEncoding = "utf8";

    /**
     * 是否使用SSL连接数据库
     * <p>
     * 默认值：false
     */
    private Boolean useSSL = false;

    /**
     * 日期格式
     */
    private String dateFormat;

    /**
     * 连接池类名
     * <p>
     * 如果Spring Boot配置了DataSource，将自动设置为SpringBootConnectionPool
     */
    private String connPool;

    /**
     * 获取配置值
     *
     * @param key 配置键
     * @return 配置值，如果不存在则返回null
     */
    public String getProperty(String key) {
        return config.get(key);
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public String getProperty(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    /**
     * 设置配置值
     *
     * @param key   配置键
     * @param value 配置值
     */
    public void setProperty(String key, String value) {
        config.put(key, value);
    }

    /**
     * 将配置转换为Properties格式的InputStream
     * <p>
     * 这个方法会将所有配置项（包括预定义属性和config Map）转换为Properties格式，
     * 以便传递给Config.loadPropertiesFromStream()方法
     *
     * @return Properties格式的InputStream
     */
    public InputStream toPropertiesInputStream() {
        // 先将预定义属性添加到config Map中
        if (controller != null) {
            config.put("controller", controller);
        }
        if (template != null) {
            config.put("template", template);
        }
        if (encoding != null) {
            config.put("encoding", encoding);
        }
        if (pageEncoding != null) {
            config.put("pageEncoding", pageEncoding);
        }
        if (reloadable != null) {
            config.put("reloadable", reloadable.toString());
        }
        if (reloadConfig != null) {
            config.put("reload_config", reloadConfig.toString());
        }
        if (dbhost != null) {
            config.put("dbhost", dbhost);
        }
        if (dbusr != null) {
            config.put("dbusr", dbusr);
        }
        if (dbpwd != null) {
            config.put("dbpwd", dbpwd);
        }
        if (dbname != null) {
            config.put("dbname", dbname);
        }
        if (dbport != null) {
            config.put("dbport", dbport);
        }
        if (dbtype != null) {
            config.put("dbtype", dbtype);
        }
        if (dbschema != null) {
            config.put("dbschema", dbschema);
        }
        if (dbEncoding != null) {
            config.put("dbEncoding", dbEncoding);
        }
        if (useSSL != null) {
            config.put("useSSL", useSSL.toString());
        }
        if (dateFormat != null) {
            config.put("dateFormat", dateFormat);
        }
        if (connPool != null) {
            config.put("conn_pool", connPool);
        }

        // 转换为Properties格式字符串
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
     * 检查是否包含指定的配置键
     *
     * @param key 配置键
     * @return 是否包含
     */
    public boolean containsKey(String key) {
        return config.containsKey(key);
    }

    /**
     * 获取配置项数量
     *
     * @return 配置项数量
     */
    public int size() {
        return config.size();
    }

    /**
     * 清空所有配置
     */
    public void clear() {
        config.clear();
    }

    /**
     * 获取所有配置项的副本
     *
     * @return 配置Map的副本
     */
    public Map<String, String> getAllProperties() {
        return new HashMap<>(config);
    }
}
