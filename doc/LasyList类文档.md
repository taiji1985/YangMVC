# LasyList类文档

## 概述

`LasyList` 是 YangMVC 框架中的核心ORM类，实现了延迟加载（Lazy Loading）技术的List。该类继承自 `ListAdapter`，提供了强大的数据库查询和操作功能。

**核心特性：**
- 延迟加载：数据只有在真正需要时才从数据库中读取
- 链式调用：支持流畅的API设计，可以连续调用多个方法
- 丰富的查询条件：支持等于、大于、小于、模糊查询等多种条件
- 排序和分页：内置排序和分页功能
- 类型转换：支持将查询结果转换为指定类型的对象列表

## 类结构

```java
package org.docshare.yangmvc.orm;

public class LasyList extends ListAdapter {
    // 核心属性
    private final ArrayList<SQLConstains> cons = new ArrayList<>(); // 约束列表
    private final DBTool tool;
    private ResultSet rs = null;
    private final IDBDelegate delegate;
    private String tbName;   // 表格名称
    private String rawSql = null;
    private Object[] args;
    private List<Model> arrList = null; // 枚举时直接放入此List中
    
    // 内部接口
    public interface Each {
        boolean one(Model m);
    }
}
```

## 构造方法

### 1. 基于表名构造
```java
public LasyList(DBTool tool, String tbName)
```
- **参数：**
  - `tool`: 数据库工具对象
  - `tbName`: 表名
- **用途：** 创建基于特定表的LasyList对象

### 2. 基于原生SQL构造（私有）
```java
private LasyList(String rawSql, Object[] args)
```
- **参数：**
  - `rawSql`: 原生SQL语句
  - `args`: SQL参数数组
- **用途：** 创建基于原生SQL的LasyList对象

## 核心方法

### 查询条件方法

#### 等值查询
```java
public LasyList eq(String column, Object val)
```
- **功能：** 添加等于条件（column = val）
- **返回：** 当前LasyList对象，支持链式调用

#### 比较查询
```java
public LasyList gt(String column, Object val)    // 大于
public LasyList gte(String column, Object val)   // 大于等于
public LasyList lt(String column, Object val)    // 小于
public LasyList lte(String column, Object val)   // 小于等于
public LasyList ne(String column, Object val)    // 不等于
```

#### 模糊查询
```java
public LasyList like(String column, String q)
public LasyList mlike(String columnList, String q)
```
- **like：** 单列模糊查询，使用 `column LIKE '%q%'`
- **mlike：** 多列模糊查询，多个列之间是OR关系

#### 空值查询
```java
public LasyList isNull(String column)
public LasyList isNotNull(String column)
```

### 排序方法

```java
public LasyList orderby(String column, boolean asc)
public LasyList addOrderby(String column, boolean asc)
```
- **orderby：** 设置排序规则（会清除之前的排序条件）
- **addOrderby：** 添加排序规则（支持多字段排序）
- **参数：**
  - `column`: 排序字段
  - `asc`: true为升序，false为降序

### 分页和限制方法

```java
public LasyList limit(int len)
public LasyList limit(int start, int len)
public LasyList page(int pageno, int pageSize)
```
- **limit(len)：** 限制返回记录数
- **limit(start, len)：** 从指定位置开始限制记录数
- **page：** 分页查询

### 数据获取方法

#### 获取单个记录
```java
public Model one()
public Model get(int index)
```
- **one()：** 获取第一个记录
- **get(index)：** 获取指定索引的记录

#### 转换为列表
```java
public List<Model> toArrayList()
public <T> List<T> toArrayList(Class<T> clazz)
```
- **toArrayList()：** 转换为Model列表
- **toArrayList(Class<T>)：** 转换为指定类型的对象列表

### 遍历和过滤方法

```java
public LasyList each(Each fun)
public List<Model> filter(Each fun)
```
- **each：** 遍历每个元素，执行指定操作
- **filter：** 过滤元素，返回满足条件的元素列表

### 静态工厂方法

```java
public static LasyList fromRawSql(String sql)
public static LasyList fromRawSql(String sql, Object[] args)
```
- **功能：** 基于原生SQL创建LasyList对象
- **用途：** 执行复杂的自定义SQL查询

### 其他实用方法

```java
public boolean exist()           // 判断是否存在记录
public boolean isEmpty()         // 判断是否为空
public int size()               // 获取记录总数
public LasyList columnFilter(String filter)  // 设置列过滤
public LasyList custom(String any)           // 添加自定义SQL条件
public LasyList byExample(Model m)           // 根据示例对象查询
```

## 使用示例

### 基本查询
```java
// 获取所有图书
LasyList books = Model.tool("book").all();

// 条件查询
LasyList result = Model.tool("book")
    .all()
    .eq("status", "published")
    .gt("price", 50)
    .like("title", "Java")
    .orderby("create_time", false)
    .limit(10);
```

### 复杂查询
```java
// 多条件查询
LasyList books = Model.tool("book")
    .all()
    .gt("id", 12)
    .lt("id", 33)
    .ne("status", "deleted")
    .isNotNull("author")
    .orderby("price", true)
    .addOrderby("create_time", false);
```

### 分页查询
```java
// 第2页，每页20条记录
LasyList pageResult = Model.tool("book")
    .all()
    .eq("category", "技术")
    .page(2, 20);
```

### 原生SQL查询
```java
// 使用原生SQL
LasyList result = LasyList.fromRawSql(
    "SELECT * FROM book WHERE price > ? AND author LIKE ?", 
    new Object[]{50, "%张%"}
);
```

### 数据转换
```java
// 转换为Book对象列表
List<Book> bookList = Model.tool("book")
    .all()
    .eq("status", "published")
    .toArrayList(Book.class);
```

### 遍历处理
```java
// 遍历处理每个记录
Model.tool("book").all().each(model -> {
    System.out.println("书名: " + model.get("title"));
    return true; // 继续遍历
});

// 过滤数据
List<Model> expensiveBooks = Model.tool("book")
    .all()
    .filter(model -> {
        Double price = (Double) model.get("price");
        return price != null && price > 100;
    });
```

## 性能特性

### 延迟加载
- **原理：** 查询条件设置时不会立即执行SQL，只有在真正需要数据时（如调用`get()`、`iterator()`、`toArrayList()`等方法）才会执行
- **优势：** 避免不必要的数据库查询，提高性能

### 缓存机制
- 一旦数据被加载到`arrList`中，后续访问将直接使用缓存数据
- 添加新的查询条件时会清空缓存，确保数据一致性

### 资源管理
- 自动管理ResultSet的生命周期
- 提供`closeRS()`方法确保资源正确释放

## 注意事项

1. **链式调用：** 所有查询条件方法都返回当前对象，支持链式调用
2. **延迟执行：** 查询条件设置后不会立即执行，需要调用数据获取方法才会执行SQL
3. **缓存失效：** 添加新查询条件会清空已缓存的数据
4. **资源释放：** 使用完毕后会自动释放数据库连接资源
5. **类型安全：** 建议使用`toArrayList(Class<T>)`方法获得类型安全的对象列表

## 相关类

- **Model：** 数据模型基类
- **DBTool：** 数据库工具类
- **SQLConstains：** SQL约束条件类
- **IDBDelegate：** 数据库代理接口
- **ListAdapter：** 列表适配器基类