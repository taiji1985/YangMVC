# YangMVC 方法参数自动映射演示

## 📋 概述

本演示项目展示了YangMVC框架的**方法参数自动映射**功能。这个功能可以将HTTP请求参数自动注入到控制器方法的参数中，无需手动调用`param()`方法。

## ✨ 核心特性

- **零配置获取参数**：直接在方法参数中声明，框架自动注入
- **类型安全**：支持String、Integer、Double、Boolean等基本类型
- **代码简洁**：减少重复代码，提高可读性
- **空值处理**：参数不存在时自动为null，需注意判空

## 🔧 前置要求

### Maven配置

必须在`pom.xml`中添加`-parameters`编译参数：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
        <compilerArgs>
            <arg>-parameters</arg>  <!-- 关键配置 -->
        </compilerArgs>
    </configuration>
</plugin>
```

**注意**：如果不添加此参数，方法参数自动映射功能将无法使用！

## 🚀 快速开始

### 1. 启动项目

```bash
cd YangMVCSpringBootStarterDemo
mvn spring-boot:run
```

### 2. 访问演示页面

打开浏览器访问：http://localhost:8080/param/

## 📚 演示示例

### 示例1：基本运算
```java
// URL: /param/add?a=10&b=20
public Integer add(Integer a, Integer b) {
    return (a != null ? a : 0) + (b != null ? b : 0);
}
```

### 示例2：字符串参数
```java
// URL: /param/greeting?name=张三
public String greeting(String name) {
    if (name == null || name.trim().isEmpty()) {
        return "你好，陌生人！";
    }
    return "你好，" + name + "！";
}
```

### 示例3：多种类型参数
```java
// URL: /param/multi?username=admin&age=25&price=99.9&active=true
public Object multi(String username, Integer age, Double price, Boolean active) {
    Map<String, Object> result = new HashMap<>();
    result.put("username", username);
    result.put("age", age);
    result.put("price", price);
    result.put("active", active);
    return result;
}
```

### 示例4：分页查询
```java
// URL: /param/users?page=1&pageSize=10
public Object users(Integer page, Integer pageSize) {
    int p = (page != null && page > 0) ? page : 1;
    int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

    LasyList users = Model.tool("student").all()
            .orderby("id", false)
            .limit((p - 1) * size, size);

    return Map.of(
        "page", p,
        "pageSize", size,
        "data", users.toArrayList()
    );
}
```

### 示例5：模糊搜索
```java
// URL: /param/search?keyword=张
public Object search(String keyword) {
    LasyList students = Model.tool("student").all();

    if (keyword != null && !keyword.trim().isEmpty()) {
        students = students.like("name", "%" + keyword + "%");
    }

    return Map.of(
        "keyword", keyword,
        "count", students.size(),
        "data", students.toArrayList()
    );
}
```

### 示例6：用户登录
```java
// URL: /param/login?username=admin&password=123456
public Object login(String username, String password) {
    // 参数验证
    if (username == null || username.trim().isEmpty()) {
        return Map.of("success", false, "message", "用户名不能为空");
    }

    if (password == null || password.trim().isEmpty()) {
        return Map.of("success", false, "message", "密码不能为空");
    }

    // 业务逻辑
    return Map.of("success", true, "message", "登录成功", "username", username);
}
```

### 示例7：保存数据
```java
// POST: /param/saveUser
// Body: {"name": "李四", "height": 175.5, "weight": 65.0}
public Object saveUser(String name, Double height, Double weight) {
    if (name == null || name.trim().isEmpty()) {
        return Map.of("success", false, "message", "姓名不能为空");
    }

    Model student = Model.tool("student").create();
    student.put("name", name);
    if (height != null) student.put("height", height);
    if (weight != null) student.put("weight", weight);
    student.save();

    return Map.of("success", true, "message", "保存成功", "data", student);
}
```

### 示例8：混合使用
```java
// URL: /param/mixed?name=王五&id=1
public Object mixed(String name) {
    // 自动映射的参数
    Map<String, Object> result = new HashMap<>();
    result.put("autoMappedName", name);

    // 手动获取的参数
    Integer id = paramInt("id");
    result.put("manualId", id);

    if (id != null) {
        Model student = Model.tool("student").get(id);
        result.put("student", student);
    }

    return result;
}
```

## 🔍 测试API

### 使用浏览器测试（GET请求）

1. **基本运算**: http://localhost:8080/param/add?a=10&b=20
2. **字符串问候**: http://localhost:8080/param/greeting?name=张三
3. **多类型参数**: http://localhost:8080/param/multi?username=admin&age=25&price=99.9&active=true
4. **分页查询**: http://localhost:8080/param/users?page=1&pageSize=5
5. **模糊搜索**: http://localhost:8080/param/search?keyword=张
6. **用户登录**: http://localhost:8080/param/login?username=admin&password=123456
7. **混合使用**: http://localhost:8080/param/mixed?name=王五&id=1

### 使用curl测试

```bash
# GET请求示例
curl "http://localhost:8080/param/add?a=10&b=20"

# POST请求示例（保存用户）
curl -X POST "http://localhost:8080/param/saveUser" \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","height":175.0,"weight":70.0}'
```

### 使用Postman测试

1. **创建新请求**
2. **设置方法**：GET或POST
3. **输入URL**：例如 `http://localhost:8080/param/add?a=10&b=20`
4. **发送请求**查看结果

## ⚠️ 注意事项

### 1. 必须添加-parameters参数
如果不添加，方法参数自动映射功能将无法使用，会报参数为null的错误。

### 2. 参数名必须完全一致
```java
// ❌ 错误：参数名不匹配
// URL: /user/list?pageNum=1
public void list(Integer page) { }

// ✅ 正确：参数名一致
// URL: /user/list?page=1
public void list(Integer page) { }
```

### 3. 空值处理
参数可能为null，必须进行判空处理：

```java
public void demo(String name, Integer age) {
    // ✅ 好的做法
    if (name != null && !name.isEmpty()) {
        // 处理name
    }
    int a = age != null ? age : 0;

    // ❌ 不好的做法
    int len = name.length();  // 可能NPE
    int b = age + 1;          // 可能NPE
}
```

### 4. 支持的类型
- String
- Integer, int
- Long, long
- Double, double
- Boolean, boolean
- Float, float

### 5. 与手动获取混用
可以同时使用自动映射和手动获取参数：

```java
public void update(String email) {
    // email是自动映射的
    Integer age = paramInt("age");  // age是手动获取的
}
```

## 📖 对比传统方式

### 传统方式
```java
public void save() {
    String username = param("username");
    String email = param("email");
    Integer age = paramInt("age");
    Double price = paramInt("price", 0.0);

    Model user = Model.tool("user").create();
    user.put("username", username);
    user.put("email", email);
    user.put("age", age);
    user.put("price", price);
    user.save();
}
```

### 使用参数自动映射
```java
public Object save(String username, String email, Integer age, Double price) {
    Model user = Model.tool("user").create();
    user.put("username", username);
    user.put("email", email);
    user.put("age", age != null ? age : 0);
    user.put("price", price != null ? price : 0.0);
    user.save();

    return Map.of("success", true);
}
```

**优势**：
- ✅ 代码更简洁
- ✅ 可读性更好
- ✅ 减少重复代码
- ✅ 返回值自动输出JSON

## 🎯 最佳实践

### 1. 参数验证
```java
public Object save(String username, String email) {
    // 验证必填参数
    if (username == null || username.trim().isEmpty()) {
        return Map.of("success", false, "message", "用户名不能为空");
    }

    // 处理可选参数
    if (email == null) {
        email = "未设置";
    }

    // 业务逻辑
    // ...
}
```

### 2. 设置默认值
```java
public Object list(Integer page, Integer pageSize) {
    int p = page != null && page > 0 ? page : 1;
    int size = pageSize != null && pageSize > 0 ? pageSize : 20;
    // ...
}
```

### 3. 结合返回值自动输出
```java
// 自动映射参数 + 自动返回JSON
public Map<String, Object> getUser(Integer id) {
    Model user = Model.tool("user").get(id);
    if (user == null) {
        return Map.of("error", "用户不存在");
    }
    return Map.of("success", true, "data", user);
}
```

## 🔗 相关文档

- [YangMVC入门教程](../doc/入门教程.md)
- [Controller类文档](../doc/Controller类文档.md)
- [API速查手册](../doc/YangMVC—API速查手册.md)

## 📝 版本信息

- **YangMVC版本**: 2025.07.06
- **Spring Boot版本**: 2.7.18
- **Java版本**: 1.8
- **更新日期**: 2026-01-02

---

**作者**: YangMVC Team
**许可**: MIT License
