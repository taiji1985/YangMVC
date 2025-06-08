# YangMVC Spring Boot 配置集成说明

## 概述

本项目实现了将YangMVC的传统`web.properties`配置文件集成到Spring Boot的`application.yml`配置文件中，实现统一的配置管理。使用Lombok简化代码，支持自定义属性配置。

## 配置方式

### 1. 在application.yml中配置

在`src/main/resources/application.yml`文件中添加`yangmvc`配置节点：

```yaml
yangmvc:
  controller: org.demo              # 控制器根包名
  template: /view                   # 模板目录路径
  dbhost: localhost                 # 数据库主机
  dbusr: root                       # 数据库用户名
  dbpwd: 123456                     # 数据库密码
  dbname: docshare                  # 数据库名称
  dbport: 3306                      # 数据库端口
  dbtype: mysql                     # 数据库类型
  dbschema: public                  # 数据库schema
  reloadable: true                  # 是否启用热重载
  reload-config: false              # 是否重新加载配置
  encoding: utf-8                   # 编码格式
  date-format: yyyy-MM-dd HH:mm:ss  # 日期格式
  db-encoding: utf8                 # 数据库编码
  page-encoding: utf-8              # 页面编码
  use-ssl: false                    # 是否使用SSL
  prop-file: /web.properties        # 配置文件路径
  # 自定义属性配置，支持任意键值对
  custom:
    app-name: YangMVC Spring Demo   # 应用名称
    app-version: 1.0.0              # 应用版本
    max-upload-size: 10MB           # 最大上传文件大小
    session-timeout: 30             # 会话超时时间
    custom-feature-enabled: true    # 自定义功能开关
```

### 2. 配置属性说明

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| controller | String | - | 控制器根包名，对应原web.properties中的controller |
| template | String | - | 模板相对于WebRoot的目录路径 |
| dbhost | String | localhost | 数据库主机名或IP地址 |
| dbusr | String | root | 数据库用户名 |
| dbpwd | String | 123456 | 数据库密码 |
| dbname | String | - | 数据库名称 |
| dbport | String | 3306 | 数据库端口号 |
| dbtype | String | mysql | 数据库类型 |
| dbschema | String | public | 数据库schema |
| reloadable | boolean | true | 是否打开实时Reload功能 |
| reload-config | boolean | false | 是否重新加载配置 |
| encoding | String | utf-8 | 编码格式 |
| date-format | String | - | 日期格式化字符串 |
| db-encoding | String | utf8 | 数据库编码 |
| page-encoding | String | utf-8 | 页面编码 |
| use-ssl | boolean | false | 是否使用SSL连接数据库 |
| prop-file | String | /web.properties | 配置文件路径 |
| custom | Map<String,String> | {} | 自定义属性配置，支持任意键值对 |

## 实现原理

### 1. 配置属性类

`YangMvcProperties`类使用`@ConfigurationProperties(prefix = "yangmvc")`注解和Lombok的`@Data`注解，自动映射yml中的yangmvc配置项。

### 2. 配置初始化器

`YangMvcConfigInitializer`类实现`CommandLineRunner`接口，在Spring Boot启动完成后将yml配置同步到YangMVC的`Config`静态字段中。

### 3. 配置启用

在主启动类上添加`@EnableConfigurationProperties(YangMvcProperties.class)`注解启用配置属性。

### 4. 自定义属性支持

通过`custom` Map支持任意自定义配置项，并同步到YangMVC的`Config.pro` Properties对象中。

### 5. Lombok优化

使用`@Data`注解自动生成getter/setter方法，简化代码。

## 测试验证

启动应用后，可以通过以下接口验证配置是否正确加载：

- `GET /config/spring` - 查看Spring Boot中加载的配置
- `GET /config/yangmvc` - 查看YangMVC中的配置
- `GET /config/yangmvc/str` - 查看YangMVC配置的完整字符串表示
- `GET /config/custom` - 查看所有自定义属性配置
- `GET /config/custom/{key}` - 查看指定自定义属性的值
- `GET /config/sync-test` - 测试配置同步是否正确

## 兼容性说明

- 新的yml配置方式与原有的web.properties配置方式完全兼容
- 如果同时存在yml配置和web.properties文件，yml配置会覆盖properties配置
- 原有的YangMVC代码无需修改，仍然通过`Config`类访问配置

## 使用建议

1. 推荐使用yml配置方式，便于统一管理Spring Boot和YangMVC的配置
2. 可以根据不同环境（dev、test、prod）创建不同的配置文件
3. 敏感信息（如数据库密码）建议使用环境变量或配置中心管理
4. **配置加密**: 敏感信息如数据库密码建议使用Spring Boot的配置加密功能
5. **配置验证**: 可以在`YangMvcProperties`类中添加`@Valid`注解进行配置验证
6. **自定义属性**: 通过`custom`配置项可以添加任意自定义配置，无需修改代码
7. **Lombok依赖**: 确保项目中已添加Lombok依赖，IDE需要安装Lombok插件

## 示例配置

### 开发环境配置
```yaml
yangmvc:
  controller: org.demo
  template: /view
  dbhost: localhost
  dbusr: root
  dbpwd: 123456
  dbname: demo_dev
  reloadable: true
```

### 生产环境配置
```yaml
yangmvc:
  controller: org.demo
  template: /view
  dbhost: ${DB_HOST:localhost}
  dbusr: ${DB_USER:root}
  dbpwd: ${DB_PASSWORD:123456}
  dbname: ${DB_NAME:demo_prod}
  reloadable: false
  use-ssl: true
```