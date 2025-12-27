# YangMVC Spring Boot Starter Demo

这是 [YangMVC Spring Boot Starter](https://gitee.com/yangtf/YangMVC) 的完整示例项目。

## 🚀 快速开始

### 1. 进入项目目录

```bash
cd YangMVCSpringBootStarterDemo
```

### 2. 运行应用

```bash
mvn spring-boot:run
```

或者：

```bash
mvn clean package
java -jar target/yangmvc-spring-boot-starter-demo-2025.07.06.jar
```

### 3. 访问应用

应用启动后，访问以下URL：

- **首页**: http://localhost:8080/hello/index
- **JSON示例**: http://localhost:8080/hello/json
- **参数示例**: http://localhost:8080/hello/echo?name=YangMVC
- **Session示例**: http://localhost:8080/hello/sessionDemo
- **重定向示例**: http://localhost:8080/hello/redirect
- **API测试**: http://localhost:8080/api/test
- **系统统计**: http://localhost:8080/api/stats
- **H2控制台**: http://localhost:8080/h2-console

## 📁 项目结构

```
YangMVCSpringBootStarterDemo/
├── src/main/
│   ├── java/org/docshare/yangmvc/demo/
│   │   ├── DemoApplication.java          # 主应用类
│   │   └── controller/
│   │       ├── HelloController.java      # 基础示例
│   │       └── ApiController.java        # REST API
│   └── resources/
│       ├── application.yml               # 应用配置
│       ├── db/
│       │   ├── schema.sql                # 数据库表结构
│       │   └── data.sql                  # 示例数据
│       ├── templates/                    # 模板文件
│       │   └── hello/index.html
│       └── static/                       # 静态资源
│           ├── css/style.css
│           └── js/app.js
└── pom.xml
```

## 🎯 功能演示

### 1. 基础示例 (HelloController)

| 方法 | URL | 说明 |
|------|-----|------|
| `index()` | `/hello/index` | 首页展示 |
| `json()` | `/hello/json` | JSON响应 |
| `echo()` | `/hello/echo?name=xxx` | 参数接收 |
| `redirect()` | `/hello/redirect` | 重定向 |
| `sessionDemo()` | `/hello/sessionDemo` | Session管理 |

### 2. REST API (ApiController)

| 方法 | URL | 说明 |
|------|-----|------|
| `test()` | `/api/test` | API测试 |
| `stats()` | `/api/stats` | 系统统计 |
| `form()` | `/api/form` (POST) | 表单提交 |

## 💻 代码示例

### 创建Controller

```java
package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;

public class MyController extends Controller {

    // 访问路径: /my/index
    public void index() {
        put("title", "我的页面");
        put("message", "Hello YangMVC!");
        render("my/index.html");
    }

    // 访问路径: /my/json
    public String json() {
        put("status", "success");
        put("data", "some data");
        return "ok";  // 自动返回JSON
    }
}
```

### URL映射规则

- 控制器类名: `HelloController` -> URL路径: `/hello/*`
- 方法名: `index()` -> URL: `/hello/index`
- 完整URL: http://localhost:8080/hello/index

### 返回类型

```java
// 返回JSON
public String json() {
    put("key", "value");
    return "ok";  // 自动转为JSON
}

// 渲染模板
public void page() {
    put("data", "value");
    render();  // 默认模板
}

// 渲染指定模板
public void custom() {
    put("data", "value");
    render("custom/template.html");
}

// 重定向
public void redirect() {
    jump("/hello/index");
}
```

## 💾 数据库配置

### 默认配置（H2内存数据库）

项目默认使用H2内存数据库，无需额外配置。

H2控制台访问：
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:demodb`
- 用户名: `sa`
- 密码: (空)

### 使用MySQL

修改 `application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yangmvc_demo?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your_password
```

并添加MySQL依赖：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

创建数据库：

```sql
CREATE DATABASE yangmvc_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行初始化脚本：

```bash
mysql -u root -p yangmvc_demo < src/main/resources/db/schema.sql
mysql -u root -p yangmvc_demo < src/main/resources/db/data.sql
```

## 🔧 配置说明

### YangMVC配置

```yaml
yangmvc:
  controller: org.docshare.yangmvc.demo.controller  # 控制器包名
  template: /templates                               # 模板目录
  reloadable: true                                   # 热加载（开发环境）
  encoding: utf-8
```

### 数据源配置

Spring Boot会自动配置数据源，YangMVC会自动使用。

## 📝 注意事项

1. **模板引擎**: 使用FreeMarker或JSP，需要确保模板文件在配置的目录下
2. **热加载**: 开发环境建议开启 `reloadable: true`
3. **数据库**: 默认使用H2，生产环境建议使用MySQL或其他数据库
4. **端口**: 默认8080，可在 `application.yml` 中修改

## 🐛 问题排查

### 应用无法启动

- 检查端口是否被占用
- 查看日志输出的错误信息
- 确认Maven依赖已正确下载

### Controller无法访问

- 检查controller包名配置是否正确
- 确认Controller类在配置的包下
- 查看启动日志中的错误信息

### 数据库连接失败

- 检查数据库配置是否正确
- 确认数据库服务已启动
- 验证数据库用户名密码

## 🎨 自定义开发

### 添加新的Controller

1. 在 `controller` 包下创建新的类
2. 继承 `Controller`
3. 实现方法，方法名即为URL路径的一部分

### 添加静态资源

将CSS、JS、图片等文件放在：
- `src/main/resources/static/css/`
- `src/main/resources/static/js/`
- `src/main/resources/static/images/`

### 添加模板文件

将HTML模板放在：
- `src/main/resources/templates/`

## 📚 相关链接

- [YangMVC官方文档](https://gitee.com/yangtf/YangMVC)
- [YangMVC Spring Boot Starter](../YangMVCSpringBootStarter/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [H2数据库文档](https://www.h2database.com/html/)

## 📄 许可证

[MIT License](../LICENSE)

