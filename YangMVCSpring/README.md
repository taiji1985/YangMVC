# YangMVCSpring

将YangMVC框架与Spring Boot完美集成的解决方案，让您能够在Spring Boot项目中使用YangMVC的强大功能。

## 项目简介

YangMVCSpring是一个创新的集成项目，它将轻量级的YangMVC框架与Spring Boot生态系统相结合。通过这个集成方案，您可以：

- 在Spring Boot项目中使用YangMVC的简洁控制器
- 享受Spring的依赖注入和生态系统
- 利用YangMVC的ORM功能和约定优于配置的设计
- 实现两个框架的优势互补

## 主要特性

- 🔗 **无缝集成**: YangMVC与Spring Boot的完美融合
- 🎯 **双重优势**: 同时享受两个框架的特性
- 🛠️ **工具支持**: 提供SpringContextUtil工具类，方便在非托管对象中使用Spring服务
- ⚙️ **配置简化**: 使用application.yml统一配置YangMVC和Spring Boot
- 🔄 **服务互通**: YangMVC控制器可以调用Spring托管的服务
- 📊 **数据库集成**: 支持Spring的数据源配置
- 🎨 **灵活架构**: 支持混合使用两种框架的控制器

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>org.docshare</groupId>
    <artifactId>yangmvc-spring</artifactId>
    <version>2025.06.20</version>
</dependency>
```

### 启动应用

```java
package org.docshare.yangmvc.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.docshare.yangmvc.spring.config.YangMvcProperties;

@SpringBootApplication
@EnableConfigurationProperties(YangMvcProperties.class)
public class YangMvcSpringApplication {
    public static void main(String[] args) {
        SpringApplication.run(YangMvcSpringApplication.class, args);
    }
}
```

### 配置文件

在`application.yml`中配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/your_database?useSSL=false&serverTimezone=GMT%2B8&characterEncoding=utf8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

# YangMVC配置
yangmvc:
  config:
    # 基本配置
    controller: org.demo
    template: /view
    
    # 数据库配置
    dbhost: localhost
    dbusr: root
    dbpwd: root
    dbname: your_database
    dbport: 3306
    dbtype: mysql
    
    # 系统配置
    encoding: utf-8
    reloadable: "true"
```

## 项目结构

```
YangMVCSpring/
├── src/main/java/
│   ├── org/docshare/yangmvc/spring/
│   │   ├── YangMvcSpringApplication.java    # 启动类
│   │   ├── config/                         # 配置类
│   │   │   ├── YangMvcProperties.java      # 配置属性类
│   │   │   ├── YangMvcConfigInitializer.java # 配置初始化器
│   │   │   ├── MVCFilterInitializer.java   # MVC过滤器初始化
│   │   │   └── SpringConnectionPool.java   # Spring连接池
│   │   ├── service/                        # Spring服务层
│   │   │   └── BookService.java           # 示例服务
│   │   └── util/                          # 工具类
│   │       ├── SpringContextUtil.java     # Spring上下文工具
│   │       ├── MyFilterChain.java         # 过滤器链
│   │       └── WrapperFilterConfig.java   # 过滤器配置包装
│   └── org/demo/                          # 示例控制器
│       ├── IndexController.java           # 主控制器
│       ├── BookController.java            # 书籍控制器
│       └── UserController.java            # 用户控制器
├── src/main/resources/
│   └── application.yml                    # 配置文件
└── pom.xml
```

## 核心功能详解

### 1. YangMVC控制器

创建继承自YangMVC Controller的控制器：

```java
package org.demo;

import org.docshare.yangmvc.mvc.Controller;

public class IndexController extends Controller {
    public String index() {
        return "Hello YangMVCSpring!";
    }
}
```

### 2. Spring服务集成

在YangMVC控制器中使用Spring服务：

```java
package org.demo;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.spring.util.SpringContextUtil;
import org.docshare.yangmvc.spring.service.BookService;

import java.util.List;

public class BookController extends Controller {

    /**
     * 使用Spring托管服务获取书籍列表
     */
    public List<BookService.Book> listFromService() {
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.getAllBooks();
    }

    /**
     * 根据ID获取书籍
     */
    public BookService.Book getBookById() {
        int id = paramInt("id", 1);
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.getBookById(id);
    }
}
```

### 3. Spring服务类

创建标准的Spring服务：

```java
package org.docshare.yangmvc.spring.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class BookService {
    
    public static class Book {
        public String name;
        public int id;
        public String author;
        public double price;
        
        public Book(int id, String name, String author, double price) {
            this.id = id;
            this.name = name;
            this.author = author;
            this.price = price;
        }
    }
    
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "Spring实战", "张三", 89.0));
        books.add(new Book(2, "Java编程思想", "李四", 108.0));
        return books;
    }
    
    public Book getBookById(int id) {
        return getAllBooks().stream()
            .filter(book -> book.id == id)
            .findFirst()
            .orElse(null);
    }
}
```

### 4. SpringContextUtil工具类

这是核心工具类，用于在非托管对象中获取Spring容器中的托管对象：

```java
// 根据类型获取Bean
BookService bookService = SpringContextUtil.getBean(BookService.class);

// 根据名称获取Bean
Object bean = SpringContextUtil.getBean("bookService");

// 根据名称和类型获取Bean
BookService bookService = SpringContextUtil.getBean("bookService", BookService.class);

// 检查Bean是否存在
boolean exists = SpringContextUtil.containsBean("bookService");
```

## 配置说明

### YangMVC配置属性

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| `controller` | 控制器包路径 | `org.demo` |
| `template` | 模板文件路径 | `/view` |
| `dbhost` | 数据库主机 | `localhost` |
| `dbusr` | 数据库用户名 | `root` |
| `dbpwd` | 数据库密码 | `root` |
| `dbname` | 数据库名称 | `your_database` |
| `dbport` | 数据库端口 | `3306` |
| `dbtype` | 数据库类型 | `mysql` |
| `encoding` | 字符编码 | `utf-8` |
| `reloadable` | 是否支持热重载 | `true` |

### 数据库连接池

项目自动配置了Spring连接池，通过`SpringConnectionPool`类实现：

```java
@Component
public class SpringConnectionPool implements IConnectionPool {
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

## API示例

启动应用后，可以访问以下API：

- `GET /` - 主页
- `GET /book/listFromService` - 获取书籍列表（使用Spring服务）
- `GET /book/getBookById?id=1` - 根据ID获取书籍
- `GET /user/list` - 用户列表

## 技术栈

- **Spring Boot**: 2.7.1
- **YangMVC**: 2025.06.20
- **Java**: 1.8+
- **Maven**: 构建工具
- **MySQL**: 数据库支持
- **Lombok**: 简化代码

## 开发指南

### 1. 添加新的控制器

```java
package org.demo;

import org.docshare.yangmvc.mvc.Controller;

public class UserController extends Controller {
    public String list() {
        // 您的业务逻辑
        return "user list";
    }
}
```

### 2. 添加新的Spring服务

```java
package org.docshare.yangmvc.spring.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    public String getUserInfo(int userId) {
        return "User info for: " + userId;
    }
}
```

### 3. 在控制器中使用服务

```java
public class UserController extends Controller {
    public String info() {
        int userId = paramInt("id", 1);
        UserService userService = SpringContextUtil.getBean(UserService.class);
        return userService.getUserInfo(userId);
    }
}
```

## 最佳实践

1. **服务分层**: 将业务逻辑放在Spring服务层，控制器只负责请求处理
2. **配置管理**: 使用application.yml统一管理所有配置
3. **异常处理**: 在使用SpringContextUtil时添加适当的异常处理
4. **性能优化**: 合理使用Spring的单例模式和YangMVC的轻量级特性
5. **代码组织**: 按功能模块组织包结构

## 注意事项

1. **启动顺序**: 确保Spring容器完全启动后再使用SpringContextUtil
2. **Bean生命周期**: 注意Spring Bean的生命周期管理
3. **配置冲突**: 避免YangMVC和Spring Boot的配置冲突
4. **依赖管理**: 注意排除冲突的依赖项

## 许可证

MIT License

## 相关文档

- [SpringContextUtil使用指南](README-SPRING-UTIL.md)
- [YangMVC官方文档](https://gitee.com/yangtf/YangMVC)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)

## 作者

- **yangtongfeng** - [ytf_003@126.com](mailto:ytf_003@126.com)
- **项目主页** - [https://gitee.com/yangtf/](https://gitee.com/yangtf/)

---

*YangMVCSpring - 两个优秀框架的完美结合！*