# YangMVC Spring Boot 简化配置集成

## 概述

本项目实现了YangMVC配置的简化集成方案，使用HashMap存储配置项，通过InputStream方式加载到YangMVC的Config类中。这种方式更加灵活，支持动态配置管理。

## 核心特性

1. **简化配置结构**: 使用单一的`config` Map存储所有配置项
2. **动态配置支持**: 支持运行时动态修改配置
3. **InputStream加载**: 通过InputStream方式加载配置到YangMVC
4. **完全兼容**: 与YangMVC原生配置完全兼容
5. **Lombok优化**: 使用Lombok简化代码

## 配置方式

在`application.yml`中配置：

```yaml
yangmvc:
  config:
    # 基本配置
    controller: org.demo
    template: /view
    
    # 数据库配置
    dbhost: localhost
    dbusr: root
    dbpwd: 123456
    dbname: docshare
    dbport: "3306"
    dbtype: mysql
    dbschema: public
    dbEncoding: utf8
    useSSL: "false"
    
    # 系统配置
    encoding: utf-8
    pageEncoding: utf-8
    dateFormat: yyyy-MM-dd HH:mm:ss
    reloadable: "true"
    reload_config: "false"
    
    # 自定义配置项 - 可以添加任意配置
    app-name: YangMVC Spring Demo
    app-version: 1.0.0
    max-upload-size: 10MB
    session-timeout: "30"
    custom-feature-enabled: "true"
    debug-mode: "true"
    cache-enabled: "false"
```

## 实现原理

### 1. YangMvcSimpleProperties

使用`@ConfigurationProperties(prefix = "yangmvc")`注解和Lombok的`@Data`注解：

- 自动绑定`application.yml`中的`yangmvc.config`配置项到HashMap
- 提供`toPropertiesInputStream()`方法将HashMap转换为InputStream
- 支持动态配置的增删改查操作

### 2. YangMvcSimpleConfigInitializer

实现`CommandLineRunner`接口：

- 在Spring Boot启动完成后自动执行
- 调用`yangMvcSimpleProperties.toPropertiesInputStream()`获取配置流
- 使用`Config.loadProperties(InputStream)`加载配置到YangMVC

### 3. Config.loadProperties(InputStream)

在YangMVC的Config类中新增的方法：

- 接受InputStream参数
- 加载Properties配置
- 同步到Config类的静态字段

## 核心类说明

### YangMvcSimpleProperties

```java
@Data
@Component
@ConfigurationProperties(prefix = "yangmvc")
public class YangMvcSimpleProperties {
    private Map<String, String> config = new HashMap<>();
    
    // 提供配置操作方法
    public String getProperty(String key);
    public void setProperty(String key, String value);
    public InputStream toPropertiesInputStream();
}
```

### YangMvcSimpleConfigInitializer

```java
@Component
public class YangMvcSimpleConfigInitializer implements CommandLineRunner {
    @Autowired
    private YangMvcSimpleProperties yangMvcSimpleProperties;
    
    @Override
    public void run(String... args) throws Exception {
        InputStream configStream = yangMvcSimpleProperties.toPropertiesInputStream();
        Config.loadProperties(configStream);
    }
}
```

## 测试验证

启动应用后，可以通过以下接口验证配置：

### 基本测试接口

- `GET /simple-config/spring` - 查看Spring Boot中加载的配置
- `GET /simple-config/yangmvc` - 查看YangMVC中的配置
- `GET /simple-config/property/{key}` - 查看指定配置项的值
- `GET /simple-config/sync-test` - 测试配置同步是否正确
- `GET /simple-config/properties-format` - 查看Properties格式的配置

### 动态配置接口

- `POST /simple-config/property/{key}` - 动态设置配置项
- `POST /simple-config/reload` - 重新加载配置

### 动态配置示例

```bash
# 设置新的配置项
curl -X POST http://localhost:8080/simple-config/property/new-config \
     -H "Content-Type: text/plain" \
     -d "new-value"

# 查看配置项
curl http://localhost:8080/simple-config/property/new-config

# 重新加载配置
curl -X POST http://localhost:8080/simple-config/reload
```

## 优势对比

### 与原有方案对比

| 特性 | 原有方案 | 简化方案 |
|------|----------|----------|
| 配置结构 | 多个独立字段 | 单一HashMap |
| 动态配置 | 不支持 | 支持 |
| 自定义配置 | 需要修改代码 | 直接添加到config |
| 配置加载 | 字段映射 | InputStream方式 |
| 代码复杂度 | 较高 | 较低 |
| 扩展性 | 有限 | 很好 |

### 主要优势

1. **更简单的配置结构**: 所有配置都在`yangmvc.config`下
2. **更好的扩展性**: 可以随意添加新的配置项
3. **动态配置支持**: 支持运行时修改配置
4. **更少的代码**: 使用HashMap减少了大量样板代码
5. **更好的维护性**: 配置逻辑更加集中和清晰

## 使用建议

1. **开发环境**: 使用`application-dev.yml`配置开发环境参数
2. **生产环境**: 使用`application-prod.yml`配置生产环境参数
3. **动态配置**: 利用动态配置接口进行运行时配置调整
4. **配置验证**: 定期使用同步测试接口验证配置一致性
5. **配置备份**: 重要配置修改前先备份当前配置

## 配置示例

### 开发环境配置

```yaml
# application-dev.yml
yangmvc:
  config:
    dbhost: localhost
    dbusr: dev_user
    dbpwd: dev_password
    dbname: dev_database
    debug-mode: "true"
    log-level: DEBUG
```

### 生产环境配置

```yaml
# application-prod.yml
yangmvc:
  config:
    dbhost: prod-db-server
    dbusr: prod_user
    dbpwd: ${DB_PASSWORD}
    dbname: prod_database
    debug-mode: "false"
    log-level: INFO
    cache-enabled: "true"
```

## 注意事项

1. **数据类型**: 所有配置值都以String形式存储，YangMVC会自动进行类型转换
2. **布尔值**: 布尔类型的配置建议使用字符串形式（"true"/"false"）
3. **数字值**: 数字类型的配置建议使用字符串形式避免类型问题
4. **敏感信息**: 数据库密码等敏感信息建议使用环境变量
5. **配置重载**: 动态修改配置后建议调用重载接口确保同步

## 总结

简化配置方案通过HashMap和InputStream的方式，实现了更加灵活和强大的配置管理功能。这种方案不仅简化了代码结构，还提供了动态配置的能力，是YangMVC与Spring Boot集成的理想解决方案。