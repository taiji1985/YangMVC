# Model类文档

## 概述

Model类是YangMVC框架中的核心ORM组件，实现了`Map<String,Object>`接口，代表数据库表中的一行记录。它采用了ActiveRecord模式，将数据和操作封装在一起，无需预先生成POJO类，能够适应任何数据库表和视图。

## 类信息

- **包名**: `org.docshare.yangmvc.orm`
- **实现接口**: `Map<String,Object>`
- **设计模式**: ActiveRecord模式
- **特点**: 动态映射，无需预定义实体类

## 核心属性

```java
private final String tableName;           // 对应的数据库表名
public boolean isNull = false;            // 标识是否为空对象
private final Map<String, Object> columns; // 数据库字段数据
private Map<String,Object> extra;         // 额外数据（非数据库字段）
DBTool joined_tool = null;                // 关联的DBTool对象
public boolean isCreated = false;         // 标识是否为新创建的对象
```

## 创建Model对象

### 1. 通过DBTool创建
```java
// 获取DBTool实例
DBTool tool = Model.tool("book"); // 等价于 DBTool.getIns("book")

// 创建新的Model对象
Model book = tool.create();

// 从数据库获取现有记录
Model book = tool.get(1); // 根据主键获取
Model book = tool.get("title", "Java编程"); // 根据指定字段获取
```

### 2. 直接创建
```java
// 创建空的Model对象
Model book = new Model("book", null);

// 使用初始数据创建
Map<String, Object> data = new HashMap<>();
data.put("title", "Java编程");
data.put("price", 59.9);
Model book = new Model("book", data);
```

## 数据访问方法

### 1. 基础数据访问
```java
// 设置字段值
book.put("title", "Java编程思想");
book.put("price", 89.0);
book.put("author", "Bruce Eckel");

// 获取字段值（Object类型）
Object title = book.get("title");
Object price = book.get("price");

// 检查是否包含字段
boolean hasTitle = book.containsKey("title");

// 移除字段
book.remove("description");
```

### 2. 类型安全的访问方法
```java
// 获取字符串值
String title = book.getStr("title");
String author = book.getStr("author");

// 获取整数值
Integer id = book.getInt("id");
Integer pageCount = book.getInt("page_count");

// 获取长整数值
Long publishTime = book.getLong("publish_time");

// 获取双精度浮点数值
Double price = book.getDouble("price");
Double rating = book.getDouble("rating");
```

**使用示例**:
```java
Model book = Model.tool("book").get(1);

// 安全的类型转换
String title = book.getStr("title");           // 自动转换为字符串
Integer price = book.getInt("price");          // 自动转换为整数
Double rating = book.getDouble("rating");      // 自动转换为浮点数

// 处理null值
if (title != null) {
    System.out.println("书名: " + title);
}
```

## 数据库操作

### 1. 保存操作
```java
// 智能保存（新记录插入，现有记录更新）
int affected = book.save();

// 强制插入
int affected = book.insert();

// 强制更新
int affected = book.update();

// 保存并返回对象本身
Model savedBook = book.saveAndRet();
Model insertedBook = book.insertAndRet();
```

**使用示例**:
```java
// 创建新书籍
Model newBook = Model.tool("book").create();
newBook.put("title", "Spring Boot实战");
newBook.put("author", "张三");
newBook.put("price", 79.0);
newBook.save(); // 自动执行INSERT

// 更新现有书籍
Model existingBook = Model.tool("book").get(1);
existingBook.put("price", 69.0); // 降价
existingBook.save(); // 自动执行UPDATE
```

### 2. 删除操作
```java
// 删除当前Model对象对应的数据库记录
int affected = book.delete();

// 通过DBTool删除
int affected = Model.tool("book").del(1); // 根据主键删除
int affected = Model.tool("book").delBy("status", "deleted"); // 根据条件删除
```

## 对象转换

### 1. 转换为自定义对象
```java
// 定义POJO类
public class Book {
    private String title;
    private String author;
    private Double price;
    
    // getters and setters...
}

// Model转换为POJO
Model bookModel = Model.tool("book").get(1);
Book book = bookModel.toObject(new Book());

// 或者使用泛型方法
Book book = bookModel.toObject(Book.class);
```

### 2. 转换为Map
```java
// Model本身就实现了Map接口
Map<String, Object> bookMap = book;

// 获取所有键值对
Set<Map.Entry<String, Object>> entries = book.entrySet();
Set<String> keys = book.keySet();
Collection<Object> values = book.values();
```

## 额外数据处理

Model支持存储非数据库字段的额外数据：

```java
// 添加额外数据（不会保存到数据库）
book.put("display_name", book.getStr("title") + " - " + book.getStr("author"));
book.put("is_favorite", true);
book.put("read_count", 0);

// 获取额外数据
String displayName = book.getStr("display_name");
Boolean isFavorite = (Boolean) book.get("is_favorite");

// 额外数据不会影响数据库操作
book.save(); // 只保存数据库字段，额外数据不会被保存
```

## 表结构信息

### 1. 获取字段信息
```java
// 获取表名
String tableName = book.getTableName();

// 获取主键字段名
String keyColumn = book.key();

// 获取字段注释
String titleRemark = book.remark("title");
String authorRemark = book.remark("author");
```

### 2. 字段验证
```java
// 检查字段是否存在
if (book.containsKey("isbn")) {
    String isbn = book.getStr("isbn");
}

// 遍历所有字段
for (String key : book.keySet()) {
    Object value = book.get(key);
    System.out.println(key + ": " + value);
}
```

## 高级特性

### 1. 链式操作
```java
Model book = Model.tool("book")
    .create()
    .put("title", "Java核心技术")
    .put("author", "Cay Horstmann")
    .put("price", 119.0)
    .saveAndRet();
```

### 2. 批量操作
```java
// 批量设置字段
Map<String, Object> bookData = new HashMap<>();
bookData.put("title", "Effective Java");
bookData.put("author", "Joshua Bloch");
bookData.put("price", 79.0);

Model book = Model.tool("book").create();
book.putAll(bookData);
book.save();
```

### 3. 条件查询结合
```java
// 获取并修改
Model book = Model.tool("book")
    .all()
    .eq("title", "Java编程思想")
    .first(); // 获取第一个匹配的记录

if (book != null) {
    book.put("price", book.getDouble("price") * 0.8); // 打8折
    book.save();
}
```

## 实际应用示例

### 1. 用户注册
```java
public class UserController extends Controller {
    public void register() {
        // 创建新用户
        Model user = Model.tool("user").create();
        
        // 自动收集表单数据
        paramToModel(user);
        
        // 设置额外字段
        user.put("create_time", System.currentTimeMillis());
        user.put("status", "active");
        
        // 保存到数据库
        if (user.save() > 0) {
            outputJSON(Map.of("success", true, "message", "注册成功"));
        } else {
            outputJSON(Map.of("success", false, "message", "注册失败"));
        }
    }
}
```

### 2. 数据更新
```java
public void updateProfile() {
    int userId = paramInt("user_id", 0);
    Model user = Model.tool("user").get(userId);
    
    if (user != null) {
        // 只更新允许修改的字段
        user.put("nickname", param("nickname"));
        user.put("email", param("email"));
        user.put("phone", param("phone"));
        user.put("update_time", System.currentTimeMillis());
        
        user.save();
        outputJSON(Map.of("success", true, "message", "更新成功"));
    } else {
        outputJSON(Map.of("success", false, "message", "用户不存在"));
    }
}
```

### 3. 数据转换
```java
public void getUserInfo() {
    int userId = paramInt("user_id", 0);
    Model user = Model.tool("user").get(userId);
    
    if (user != null) {
        // 添加计算字段
        user.put("age", calculateAge(user.getLong("birthday")));
        user.put("avatar_url", "/avatars/" + user.getStr("avatar"));
        
        // 移除敏感信息
        user.remove("password");
        user.remove("salt");
        
        outputJSON(user);
    } else {
        outputJSON(Map.of("error", "用户不存在"));
    }
}
```

## 最佳实践

### 1. 字段验证
```java
public boolean validateBook(Model book) {
    String title = book.getStr("title");
    Double price = book.getDouble("price");
    
    if (title == null || title.trim().isEmpty()) {
        return false;
    }
    
    if (price == null || price <= 0) {
        return false;
    }
    
    return true;
}
```

### 2. 安全的数据访问
```java
// 使用类型安全的方法
String title = book.getStr("title");
if (title != null && !title.isEmpty()) {
    // 处理标题
}

// 提供默认值
Integer pageCount = book.getInt("page_count");
if (pageCount == null) {
    pageCount = 0;
}
```

### 3. 事务处理
```java
public void transferBook() {
    try {
        // 开始事务
        DBHelper.getIns("mysql").beginTransaction();
        
        Model book = Model.tool("book").get(paramInt("book_id", 0));
        book.put("owner_id", paramInt("new_owner_id", 0));
        book.put("transfer_time", System.currentTimeMillis());
        book.save();
        
        // 记录转移日志
        Model log = Model.tool("transfer_log").create();
        paramToModel(log);
        log.save();
        
        // 提交事务
        DBHelper.getIns("mysql").commit();
        
        outputJSON(Map.of("success", true));
    } catch (Exception e) {
        // 回滚事务
        DBHelper.getIns("mysql").rollback();
        outputJSON(Map.of("success", false, "error", e.getMessage()));
    }
}
```

## 注意事项

1. **线程安全**: Model对象不是线程安全的，不要在多线程环境中共享同一个Model实例

2. **内存管理**: Model对象会缓存数据库字段信息，长期持有大量Model对象可能影响内存

3. **数据类型**: 数据库字段值会根据数据库驱动自动转换，使用类型安全方法获取值

4. **主键管理**: 新创建的Model对象保存后，主键值会自动设置到对象中

5. **额外数据**: extra字段中的数据不会保存到数据库，仅用于临时计算和显示

## 扩展功能

### 自定义Model基类
```java
public abstract class BaseModel extends Model {
    public BaseModel(String tableName, Map<String, Object> columns) {
        super(tableName, columns);
    }
    
    public void setCreateTime() {
        put("create_time", System.currentTimeMillis());
    }
    
    public void setUpdateTime() {
        put("update_time", System.currentTimeMillis());
    }
    
    @Override
    public int save() {
        if (isCreated) {
            setCreateTime();
        }
        setUpdateTime();
        return super.save();
    }
}
```

Model类是YangMVC框架ORM功能的核心，通过它可以实现灵活、高效的数据库操作，无需复杂的配置和代码生成，大大提高了开发效率。