# Spring工具类使用指南

## 概述

本项目提供了一个非常简便的工具类 `SpringContextUtil`，用于在非托管对象中获取Spring容器中的托管对象。这解决了在YangMVC框架的Controller等非Spring管理的类中使用Spring服务的问题。

## 核心组件

### 1. SpringContextUtil 工具类

位置：`src/main/java/org/docshare/yangmvc/spring/util/SpringContextUtil.java`

这是一个Spring组件，实现了 `ApplicationContextAware` 接口，用于获取和缓存Spring应用上下文。

### 2. BookService 示例服务

位置：`src/main/java/org/docshare/yangmvc/spring/service/BookService.java`

这是一个标准的Spring服务类，用@Service注解标记，提供书籍相关的业务逻辑。

## 使用方法

### 基本用法

在任何非托管对象中，你可以通过以下方式获取Spring容器中的托管对象：

```java
// 1. 根据类型获取Bean
BookService bookService = SpringContextUtil.getBean(BookService.class);

// 2. 根据名称获取Bean
Object bean = SpringContextUtil.getBean("bookService");

// 3. 根据名称和类型获取Bean
BookService bookService = SpringContextUtil.getBean("bookService", BookService.class);
```

### 在BookController中的实际应用

```java
public class BookController extends Controller {
    
    /**
     * 使用Spring托管服务获取书籍列表
     */
    public List<BookService.Book> listFromService() {
        // 获取Spring容器中的BookService
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.getAllBooks();
    }
    
    /**
     * 根据ID获取书籍
     */
    public BookService.Book getBookById(int id) {
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.getBookById(id);
    }
}
```

## 工具类提供的方法

| 方法 | 描述 | 示例 |
|------|------|------|
| `getBean(Class<T> clazz)` | 根据类型获取Bean | `SpringContextUtil.getBean(BookService.class)` |
| `getBean(String name)` | 根据名称获取Bean | `SpringContextUtil.getBean("bookService")` |
| `getBean(String name, Class<T> clazz)` | 根据名称和类型获取Bean | `SpringContextUtil.getBean("bookService", BookService.class)` |
| `containsBean(String name)` | 检查是否包含指定名称的Bean | `SpringContextUtil.containsBean("bookService")` |
| `isSingleton(String name)` | 判断Bean是否为单例 | `SpringContextUtil.isSingleton("bookService")` |
| `getType(String name)` | 获取Bean的类型 | `SpringContextUtil.getType("bookService")` |
| `getApplicationContext()` | 获取ApplicationContext | `SpringContextUtil.getApplicationContext()` |

## 优势

1. **简单易用**：只需要一行代码就能获取Spring容器中的任何托管对象
2. **类型安全**：支持泛型，编译时类型检查
3. **功能完整**：提供了多种获取Bean的方式，满足不同场景需求
4. **无侵入性**：不需要修改现有的Spring配置，只需要添加工具类
5. **性能优良**：直接使用Spring的ApplicationContext，性能与原生Spring调用相同

## 注意事项

1. **确保Spring容器已启动**：在使用工具类之前，确保Spring应用已经完全启动
2. **Bean必须存在**：确保要获取的Bean已经在Spring容器中注册
3. **异常处理**：建议在使用时添加适当的异常处理

```java
try {
    BookService bookService = SpringContextUtil.getBean(BookService.class);
    // 使用bookService
} catch (Exception e) {
    // 处理Bean不存在或其他异常
    e.printStackTrace();
}
```

## 扩展使用

你可以创建更多的Spring服务类，然后在任何非托管对象中使用：

```java
@Service
public class UserService {
    // 用户相关业务逻辑
}

@Service
public class OrderService {
    // 订单相关业务逻辑
}

// 在Controller中使用
public class MyController extends Controller {
    public void someMethod() {
        UserService userService = SpringContextUtil.getBean(UserService.class);
        OrderService orderService = SpringContextUtil.getBean(OrderService.class);
        // 使用服务
    }
}
```

这样，你就可以在YangMVC的非托管对象中充分利用Spring的依赖注入和服务管理功能了！