# YangMVC Spring Boot Starter - 使用指南

本指南演示如何将 Spring Boot 项目与 [YangMVC](https://gitee.com/yangtf/YangMVC) 框架集成。

## 📖 什么是 YangMVC Spring Boot Starter？

YangMVC Spring Boot Starter 是一个自动化配置包，让你在 Spring Boot 项目中零配置使用 YangMVC 框架。

### 核心特性

- **零配置启动** - 只需添加依赖即可使用
- **自动数据源集成** - 自动使用 Spring Boot 的 DataSource
- **简洁的 API** - 比传统 Controller 更简洁
- **内置 ORM** - 轻量级数据库操作
- **FreeMarker 模板** - 自动集成模板引擎

## 🚀 快速开始

### 步骤 1: 添加依赖

在你的 Spring Boot 项目的 `pom.xml` 中添加：

```xml
<dependencies>
    <!-- YangMVC Spring Boot Starter -->
    <dependency>
        <groupId>org.docshare</groupId>
        <artifactId>yangmvc-spring-boot-starter</artifactId>
        <version>2025.07.06</version>
    </dependency>

    <!-- Spring Boot Web（通常已有） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot JDBC（用于数据源支持） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
</dependencies>
```

### 步骤 2: 创建配置文件

在 `application.yml` 或 `application.properties` 中添加 YangMVC 配置：

**application.yml:**

```yaml
yangmvc:
  controller: com.yourpackage.controller  # 你的 Controller 包名
  template: /view                          # 模板目录（classpath 路径）
  reloadable: false                        # 生产环境设为 false
  encoding: utf-8
```

**application.properties:**

```properties
yangmvc.controller=com.yourpackage.controller
yangmvc.template=/view
yangmvc.reloadable=false
yangmvc.encoding=utf-8
```

### 步骤 3: 创建 Controller

在你的 controller 包下创建类，继承 `org.docshare.yangmvc.mvc.Controller`：

```java
package com.yourpackage.controller;

import org.docshare.yangmvc.mvc.Controller;

public class HelloController extends Controller {

    // 访问路径: /hello/index
    public void index() {
        put("title", "我的页面");
        put("message", "Hello YangMVC!");
        renderFreeMarker("/hello/index.html");
    }

    // 访问路径: /hello/json
    public Object json() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", "some data");
        return result;  // 自动转换为 JSON
    }
}
```

### 步骤 4: 创建模板文件

在 `src/main/resources/view/` 目录下创建模板：

`src/main/resources/view/hello/index.html:`

```html
<!DOCTYPE html>
<html>
<head>
    <title>${title!}</title>
</head>
<body>
    <h1>${message!}</h1>
</body>
</html>
```

### 步骤 5: 启动应用

运行你的 Spring Boot 应用，访问：
- http://localhost:8080/hello/index
- http://localhost:8080/hello/json

**就这么简单！** YangMVC 会自动扫描配置的包并注册路由。

## 📦 完整集成示例

### 项目结构

```
your-spring-boot-project/
├── src/main/
│   ├── java/com/yourpackage/
│   │   ├── YourApplication.java           # Spring Boot 主类
│   │   └── controller/
│   │       ├── HelloController.java       # YangMVC Controller
│   │       └── UserController.java        # 另一个 Controller
│   └── resources/
│       ├── application.yml                # Spring Boot 配置
│       ├── view/                          # YangMVC 模板目录
│       │   ├── hello/
│       │   │   └── index.html
│       │   └── user/
│       │       └── list.html
│       └── static/                        # 静态资源
│           ├── css/
│           └── js/
└── pom.xml
```

### 完整的 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>com.yourpackage</groupId>
    <artifactId>your-app</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>1.8</java.version>
        <yangmvc.version>2025.07.06</yangmvc.version>
    </properties>

    <dependencies>
        <!-- YangMVC Spring Boot Starter -->
        <dependency>
            <groupId>org.docshare</groupId>
            <artifactId>yangmvc-spring-boot-starter</artifactId>
            <version>${yangmvc.version}</version>
        </dependency>

        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot JDBC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <!-- 数据库驱动（根据需要选择） -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!-- 或 MySQL -->
        <!--
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        -->
    </dependencies>
</project>
```

### 完整的 application.yml

```yaml
server:
  port: 8080

# YangMVC 配置
yangmvc:
  controller: com.yourpackage.controller
  template: /view
  reloadable: false
  encoding: utf-8

# Spring 数据源配置（YangMVC 会自动使用）
spring:
  datasource:
    url: jdbc:h2:mem:mydb
    driver-class-name: org.h2.Driver
    username: sa
    password:

# 日志配置
logging:
  level:
    com.yourpackage: DEBUG
    org.docshare.yangmvc: DEBUG
```

## 🔄 从 Spring Boot Controller 迁移

### 传统 Spring Boot Controller

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("id", id);
        return result;
    }

    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("user", user);
        return result;
    }
}
```

### YangMVC Controller

```java
public class UserController extends Controller {

    // 访问路径: /user/get?id=123
    public Object get() {
        Long id = longParam("id");
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("id", id);
        return result;
    }

    // 访问路径: /user/create (POST)
    public Object create() {
        User user = paramToObject(User.class);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("user", user);
        return result;
    }
}
```

### 对比优势

| 特性 | Spring Boot Controller | YangMVC Controller |
|------|----------------------|-------------------|
| 代码量 | 需要注解 | 零注解 |
| URL 映射 | `@RequestMapping` 等注解 | 方法名即 URL |
| 参数获取 | `@RequestParam`, `@PathVariable` | `param()`, `intParam()` |
| 返回 JSON | `@ResponseBody` 或 `@RestController` | 返回对象自动转 JSON |
| 模板渲染 | 需要 `ModelAndView` | `renderFreeMarker()` |

## 🎯 URL 映射规则

YangMVC 使用约定优于配置的原则：

### 命名规则

```
Controller 类名       URL 路径           示例
HelloController   →   /hello/*      →   /hello/index, /hello/test
UserController    →   /user/*       →   /user/list, /user/add
ApiUserController →   /apiuser/*    →   /apiuser/get, /apiuser/list
```

### 方法名映射

```java
public class HelloController extends Controller {
    public void index()       // /hello/index
    public void list()        // /hello/list
    public void addUser()     // /hello/addUser
    public Object getJson()   // /hello/getJson
}
```

## 📝 核心 API

### 1. 参数获取

```java
public void demo() {
    // 获取字符串参数
    String name = param("name");

    // 获取整数参数
    Integer age = intParam("age");

    // 获取长整数参数
    Long id = longParam("id");

    // 获取默认值
    String name = param("name", "default");
}
```

### 2. 返回数据

```java
// 返回 JSON
public Object jsonData() {
    Map<String, Object> result = new HashMap<>();
    result.put("status", "success");
    return result;  // 自动转 JSON
}

// 返回单个对象
public Object user() {
    return new User(1, "张三");
}

// 返回数组
public Object users() {
    return new User[]{
        new User(1, "张三"),
        new User(2, "李四")
    };
}
```

### 3. 渲染模板

```java
// 渲染模板
public void page() {
    put("title", "页面标题");
    put("users", getUserList());
    renderFreeMarker("/user/list.html");
}
```

### 4. 重定向

```java
public void redirect() {
    jump("/hello/index");
}
```

### 5. Session 操作

```java
// 存储 Session
sess("username", "张三");
sess("userId", 123);

// 获取 Session
String username = (String) sess("username");
Integer userId = (Integer) sess("userId");
```

### 6. 输出文本

```java
public void text() {
    response().setContentType("text/plain;charset=utf-8");
    output("纯文本输出");
}
```

## 🔌 数据库集成

YangMVC 会自动使用 Spring Boot 配置的 DataSource。

### 配置数据源

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: password
```

### 使用 YangMVC ORM

```java
public class UserController extends Controller {

    // 查询列表
    public Object list() {
        List<User> users = DB.list(User.class);
        return users;
    }

    // 根据 ID 查询
    public Object get() {
        Integer id = intParam("id");
        User user = DB.get(User.class, id);
        return user;
    }

    // 保存
    public Object save() {
        User user = paramToObject(User.class);
        DB.save(user);
        return success("保存成功");
    }

    // 更新
    public Object update() {
        User user = paramToObject(User.class);
        DB.update(user);
        return success("更新成功");
    }

    // 删除
    public Object delete() {
        Integer id = intParam("id");
        DB.delete(User.class, id);
        return success("删除成功");
    }
}
```

## 🔧 配置详解

### YangMVC 配置参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `yangmvc.controller` | String | 是 | 无 | Controller 包名 |
| `yangmvc.template` | String | 否 | `/view` | 模板目录路径 |
| `yangmvc.reloadable` | Boolean | 否 | `false` | 是否启用热加载 |
| `yangmvc.encoding` | String | 否 | `utf-8` | 编码格式 |

### 开发环境配置

```yaml
yangmvc:
  controller: com.yourpackage.controller
  template: /view
  reloadable: true   # 开发环境开启热加载
  encoding: utf-8

logging:
  level:
    org.docshare.yangmvc: DEBUG  # 开启调试日志
```

### 生产环境配置

```yaml
yangmvc:
  controller: com.yourpackage.controller
  template: /view
  reloadable: false  # 生产环境关闭热加载
  encoding: utf-8

logging:
  level:
    org.docshare.yangmvc: INFO  # 只显示重要日志
```

## 🎨 模板使用

### 模板位置

模板文件放在：`src/main/resources/view/`

### FreeMarker 模板示例

```html
<!DOCTYPE html>
<html>
<head>
    <title>${title!}</title>
</head>
<body>
    <h1>${message!}</h1>

    <#if users??>
        <table>
            <#list users as user>
                <tr>
                    <td>${user.id!}</td>
                    <td>${user.name!}</td>
                    <td>${user.email!}</td>
                </tr>
            </#list>
        </table>
    </#if>
</body>
</html>
```

## 🐛 常见问题

### 1. Controller 无法访问

**问题**: 访问 URL 返回 404

**解决**:
- 检查 `application.yml` 中 `yangmvc.controller` 包名是否正确
- 确认 Controller 类在配置的包下
- 确认 Controller 继承了 `org.docshare.yangmvc.mvc.Controller`
- 查看启动日志是否有错误

### 2. 模板找不到

**问题**: 渲染模板时报错找不到文件

**解决**:
- 确认模板文件在 `src/main/resources/view/` 目录
- 检查 `application.yml` 中 `yangmvc.template` 配置
- `renderFreeMarker()` 中的路径应以 `/` 开头

### 3. JSON 乱码

**问题**: 返回的 JSON 中文乱码

**解决**:
- 检查 `yangmvc.encoding` 是否设置为 `utf-8`

### 4. 数据库连接失败

**问题**: 无法连接数据库

**解决**:
- 检查 `spring.datasource` 配置是否正确
- 确认数据库服务已启动
- 验证用户名和密码

## 📚 进阶话题

### 与 Spring Boot Controller 共存

YangMVC 可以和 Spring Boot 的 Controller 共存：

```java
// Spring Boot Controller
@RestController
@RequestMapping("/api/v1")
public class SpringController {
    @GetMapping("/test")
    public String test() {
        return "Spring Boot Controller";
    }
}

// YangMVC Controller
public class YangMVCController extends Controller {
    public void test() {
        // /yangmvc/test
        renderFreeMarker("/test.html");
    }
}
```

### 使用 Spring Bean

在 YangMVC Controller 中使用 Spring Bean：

```java
import org.docshare.yangmvc.springboot.util.SpringContextHolder;

public class MyController extends Controller {
    public void index() {
        // 获取 Spring Bean
        MyService service = SpringContextHolder.getBean(MyService.class);
        service.doSomething();
        renderFreeMarker("/index.html");
    }
}
```


## 💾 数据库功能演示

本项目包含完整的数据库操作演示，使用 YangMVC 的 ORM 功能操作 MySQL 数据库。

### 数据库配置

项目使用 `teach` 数据库，包含以下表：

- **student** - 学生表
- **course** - 课程表
- **user** - 用户表
- **selection** - 选课表

### 配置数据源

在 `application.yml` 中配置 MySQL 连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3309/teach?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
```

### 访问数据库演示

启动应用后，访问以下 URL 体验数据库功能：

#### 学生管理

- **学生列表**: http://localhost:8080/db/student/list
- **添加学生**: http://localhost:8080/db/student/add
- **编辑学生**: http://localhost:8080/db/student/edit?id=1

#### JSON API

- **获取单个学生**: http://localhost:8080/db/student/get?id=1
- **学生统计**: http://localhost:8080/db/student/stats
- **课程列表 API**: http://localhost:8080/db/course/api
- **搜索课程**: http://localhost:8080/db/course/search?keyword=java

#### 数据库首页

- **数据库演示首页**: http://localhost:8080/db/index

### YangMVC ORM API 速查

#### 1. 查询数据

```java
// 获取表的工具对象
Model.tool("表名")

// 查询所有记录
LasyList students = Model.tool("student").all();

// 根据主键获取
Model student = Model.tool("student").get(1);

// 条件查询
LasyList list = Model.tool("student").all()
    .eq("name", "张三")
    .gt("height", 170);

// 模糊查询
LasyList list = Model.tool("course").all()
    .like("course_name", "%Java%");
```

#### 2. 创建和保存

```java
// 创建新记录
Model student = Model.tool("student").create();
student.put("name", "张三");
student.put("height", 175.0);
student.put("weight", 70.0);
student.save();  // 保存到数据库
```

#### 3. 更新数据

```java
// 获取并更新
Model student = Model.tool("student").get(1);
student.put("height", 180.0);
student.save();  // 保存更新
```

#### 4. 删除数据

```java
// 根据主键删除
Model.tool("student").del(1);

// 或通过 Model 对象删除
Model student = Model.tool("student").get(1);
student.delete();
```

#### 5. 分页和排序

```java
// 分页查询
LasyList students = Model.tool("student").all()
    .orderby("id", false)
    .page(1, 10);

// 排序
LasyList students = Model.tool("student").all()
    .orderby("height", false);  // 降序
```

### Model 对象方法

```java
// 获取字段值
Object id = model.get("id");
String name = model.getStr("name");
Integer age = model.getInt("age");
Double height = model.getDouble("height");

// 设置字段值
model.put("name", "李四");
model.put("age", 20);

// 保存到数据库
model.save();

// 获取所有字段名
String[] keys = model.keys();
```

### 完整示例

查看 `DbDemoController.java` 了解完整的 CRUD 操作示例：

- **查询列表**: `studentList()` 方法
- **查询单个**: `studentGet()` 方法
- **新增记录**: `studentSave()` 方法（id 为 null 时）
- **更新记录**: `studentSave()` 方法（id 不为 null 时）
- **删除记录**: `studentDelete()` 方法
- **统计查询**: `studentStats()` 方法

## 📦 Demo 项目

本目录包含完整的 Demo 项目，展示了各种用法：

- **HelloController** - 基础用法
- **ApiController** - REST API 示例
- **UserController** - CRUD 操作示例

运行 Demo：

```bash
cd YangMVCSpringBootStarterDemo
mvn spring-boot:run
```

访问：
- http://localhost:8080/hello/index
- http://localhost:8080/user/list
- http://localhost:8080/api/test

## 📚 相关资源

- [YangMVC 官方文档](https://gitee.com/yangtf/YangMVC)
- [YangMVC Spring Boot Starter 源码](../YangMVCSpringBootStarter/)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [FreeMarker 文档](https://freemarker.apache.org/docs/)

## 📄 许可证

[MIT License](../LICENSE)

---

**作者**: YangMVC Team
**版本**: 2025.07.06
