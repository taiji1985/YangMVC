# YangMVC Spring Boot Starter

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7%2B-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-1.8%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

让 [YangMVC](https://gitee.com/yangtf/YangMVC) 在 Spring Boot 中使用变得超级简单！

## 特性

- **零配置启动** - 只需添加依赖即可使用
- **自动数据源集成** - 自动使用 Spring Boot 配置的 DataSource
- **完全自动化** - 使用 Spring Boot 的自动配置机制
- **向后兼容** - 支持 Spring Boot 2.7+ 到 3.x 版本
- **灵活可扩展** - 支持自定义配置覆盖

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.docshare</groupId>
    <artifactId>yangmvc-spring-boot-starter</artifactId>
    <version>2025.06.19</version>
</dependency>
```

### 2. 配置 application.yml

```yaml
yangmvc:
  controller: com.example.controllers  # 控制器包名（必需）
  template: /view                     # 模板目录（默认：/view）
  reloadable: true                    # 开发环境建议开启

spring:
  datasource:                         # 可选：如果需要数据库功能
    url: jdbc:mysql://localhost:3306/mydb
    username: user
    password: pass
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 3. 创建控制器

```java
package com.example.controllers;

import org.docshare.yangmvc.mvc.Controller;

public class UserController extends Controller {

    // 访问路径：/user/index
    public String index() {
        put("message", "Hello YangMVC!");
        return "ok";  // 自动返回JSON
    }

    // 访问路径：/user/list
    public void list() {
        // 使用YangMVC的ORM功能
        LasyList users = L("users").all();
        put("users", page(users));  // 分页
        render();  // 渲染模板
    }
}
```

### 4. 启动应用

```java
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```

就这么简单！访问 `http://localhost:8080/user/index` 即可看到效果。

## 配置说明

### 必需配置

| 配置项 | 说明 | 示例 |
|--------|------|------|
| `yangmvc.controller` | 控制器包名 | `com.example.controllers` |

### 可选配置

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `yangmvc.template` | 模板目录 | `/view` |
| `yangmvc.reloadable` | 是否支持热加载 | `true` |
| `yangmvc.encoding` | 编码格式 | `utf-8` |
| `yangmvc.pageEncoding` | 页面编码 | `utf-8` |
| `yangmvc.dateFormat` | 日期格式 | - |

### 数据库配置（可选）

如果不需要使用 YangMVC 的 ORM 功能，可以不配置数据库。

#### 方式一：使用 Spring Boot 数据源（推荐）

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: user
    password: pass
```

Starter 会自动检测并使用 Spring Boot 的 DataSource。

#### 方式二：使用 YangMVC 独立配置

```yaml
yangmvc:
  dbhost: localhost
  dbusr: root
  dbpwd: password
  dbname: mydb
  dbport: 3306
  dbtype: mysql
```

## 控制器开发

### URL映射规则

控制器类名和方法名会自动映射为URL：

```
UserController.index()   -> /user/index
UserController.list()    -> /user/list
AdminUserController.show() -> /adminUser/show
```

**注意**：URL中的连字符会自动转换为驼峰式：
```
/user-info/detail -> UserInfoController.detail()
```

### 返回类型

```java
// 返回字符串（自动转为JSON）
public String index() {
    put("message", "Hello");
    return "ok";
}
// 输出：{"message": "Hello", "status": "ok"}

// 渲染模板
public void page() {
    put("title", "My Page");
    render();  // 渲染 /view/user/page.html
}

// 渲染指定模板
public void custom() {
    put("data", "value");
    render("custom_view");  // 渲染 /view/custom_view.html
}

// 重定向
public void redirect() {
    jump("/user/list");
}
```

### 获取参数

```java
public void method() {
    // 获取请求参数
    String name = param("name");
    Integer age = paramInt("age");

    // 获取Session
    String user = sess("user");
    sess("user", "value");

    // 获取请求对象
    HttpServletRequest req = request();
    HttpServletResponse resp = response();
}
```

### 数据库操作

```java
public void db() {
    // 查询所有
    LasyList users = L("users").all();

    // 条件查询
    LasyList list = L("users")
        .where("status=?", 1)
        .all();

    // 分页
    put("users", page(L("users").all()));

    // 使用DBTool
    DBTool db = T("users");
    db.insert(map);
    db.update(1, map);
    db.delete(1);
}
```

## 高级功能

### 拦截器

```java
public class MyInterceptor implements Interceptor {
    @Override
    public boolean before(String uri, Object controller) {
        // 前置处理
        return true;  // 返回false中断请求
    }

    @Override
    public Object after(String uri, Object controller, Object result) {
        // 后置处理
        return result;
    }
}

// 注册拦截器
Config.addInterceptor(new MyInterceptor());
```

### 访问 Spring Bean

在控制器中可以通过 `SpringContextHolder` 访问 Spring 容器中的 Bean：

```java
public void method() {
    MyService service = SpringContextHolder.getBean(MyService.class);
    service.doSomething();
}
```

### 自定义配置

如果需要完全自定义配置，可以创建配置类：

```java
@Configuration
public class YangMvcCustomConfiguration {

    @Bean
    public CommandLineRunner customYangMvcInit() {
        return args -> {
            // 自定义初始化逻辑
            Config.addInterceptor(new MyInterceptor());
        };
    }
}
```

## 示例项目

完整示例请参考：

- [YangMVCSpringBootDemo](../YangMVCSpringBootDemo/) - 完整的 Web 应用示例
- [YangMVCSpring](../YangMVCSpring/) - Spring Boot 集成示例

## 常见问题

### 1. 控制器无法访问

检查 `yangmvc.controller` 配置的包名是否正确，控制器是否在该包下。

### 2. 模板找不到

检查 `yangmvc.template` 配置和模板文件路径，确保模板文件在正确的目录。

### 3. 数据库连接失败

如果使用 Spring Boot 数据源，确保 `spring-boot-starter-jdbc` 依赖已添加，数据库配置正确。

### 4. 与 Spring Security 冲突

YangMVC Filter 配置为最低优先级，会先执行 Spring Security Filter。如果需要调整顺序，可以自定义 FilterRegistrationBean。

## 兼容性

- **Spring Boot**: 2.7.x - 3.x
- **Java**: 1.8+
- **Servlet**: 3.1+

## 许可证

[MIT License](LICENSE)

## 链接

- [YangMVC 官方文档](https://gitee.com/yangtf/YangMVC)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

## 支持

如有问题，请提交 [Issue](https://gitee.com/yangtf/YangMVC/issues)
