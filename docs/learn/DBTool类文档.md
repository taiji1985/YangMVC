# DBTool类文档

## 概述

DBTool类是YangMVC框架ORM功能的核心类，负责数据库表的查询、操作和管理。它采用了Builder模式和延迟加载技术，提供了链式调用的API，让数据库操作变得简单直观。所有数据库查询都需要先创建DBTool实例。

## 类信息

- **包名**: `org.docshare.yangmvc.orm`
- **作者**: Tongfeng Yang
- **设计模式**: Builder模式、工厂模式、单例模式
- **特点**: 链式调用、延迟加载、自动缓存

## 核心属性

```java
private String tname;                           // 数据库表名
private HashMap<String,Object> columns;         // 表的所有列信息
public Map<String, ColumnDesc> c_to_remarks;   // 列描述信息映射
String key;                                     // 主键字段名
IDBDelegate delegate;                           // 数据库操作委托对象
```

## 创建DBTool实例

### 1. 基本创建方式
```java
// 获取指定表的DBTool实例
DBTool bookTool = DBTool.getIns("book");
DBTool userTool = DBTool.getIns("user");
DBTool orderTool = DBTool.getIns("order");

// 通过Model类快速获取
DBTool bookTool = Model.tool("book");
```

### 2. 实例缓存机制
DBTool采用了缓存机制，同一个表名的实例会被复用：
```java
DBTool tool1 = DBTool.getIns("book");
DBTool tool2 = DBTool.getIns("book");
// tool1 和 tool2 是同一个实例
assert tool1 == tool2;
```

## 查询操作

### 1. 基础查询
```java
// 获取所有记录（延迟加载）
LasyList allBooks = DBTool.getIns("book").all();

// 根据主键获取单条记录
Model book = DBTool.getIns("book").get(1);
Model book = DBTool.getIns("book").get("123"); // 字符串主键

// 根据指定字段获取记录
Model book = DBTool.getIns("book").get("isbn", "978-0134685991");
Model user = DBTool.getIns("user").get("email", "user@example.com");
```

### 2. 条件查询
DBTool提供了丰富的条件查询方法，支持链式调用：

```java
// 等值查询
LasyList books = DBTool.getIns("book")
    .all()
    .eq("status", "published")     // status = 'published'
    .eq("category_id", 1);          // category_id = 1

// 比较查询
LasyList books = DBTool.getIns("book")
    .all()
    .gt("price", 50)               // price > 50
    .lt("price", 200)              // price < 200
    .gte("rating", 4.0)            // rating >= 4.0
    .lte("page_count", 500);       // page_count <= 500

// 模糊查询
LasyList books = DBTool.getIns("book")
    .all()
    .like("title", "%Java%")       // title LIKE '%Java%'
    .like("author", "张%");         // author LIKE '张%'

// 范围查询
LasyList books = DBTool.getIns("book")
    .all()
    .in("category_id", Arrays.asList(1, 2, 3))  // category_id IN (1,2,3)
    .between("publish_date", "2020-01-01", "2023-12-31"); // 日期范围
```

### 3. 排序和分页
```java
// 排序
LasyList books = DBTool.getIns("book")
    .all()
    .orderBy("create_time DESC")    // 按创建时间降序
    .orderBy("price ASC");          // 再按价格升序

// 分页
LasyList books = DBTool.getIns("book")
    .all()
    .limit(10)                      // 限制10条记录
    .offset(20);                    // 跳过前20条

// 组合使用
LasyList books = DBTool.getIns("book")
    .all()
    .eq("status", "published")
    .gt("price", 0)
    .orderBy("sales DESC")
    .limit(20)
    .offset(0);
```

### 4. 复杂查询示例
```java
// 查询热门Java书籍
LasyList javaBooks = DBTool.getIns("book")
    .all()
    .like("title", "%Java%")
    .or()
    .like("keywords", "%Java%")
    .and()
    .eq("status", "published")
    .gt("rating", 4.0)
    .orderBy("sales DESC, rating DESC")
    .limit(10);

// 查询最近一个月的订单
long oneMonthAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
LasyList recentOrders = DBTool.getIns("order")
    .all()
    .gt("create_time", oneMonthAgo)
    .in("status", Arrays.asList("paid", "shipped", "completed"))
    .orderBy("create_time DESC");
```

## 数据操作

### 1. 创建和保存
```java
// 创建新记录
Model newBook = DBTool.getIns("book").create();
newBook.put("title", "Spring Boot实战");
newBook.put("author", "张三");
newBook.put("price", 79.0);
newBook.put("status", "published");

// 保存到数据库
int affected = newBook.save();  // 返回影响的行数

// 或者使用DBTool直接保存
int affected = DBTool.getIns("book").save(newBook);
Model savedBook = DBTool.getIns("book").saveAndRet(newBook); // 保存并返回对象
```

### 2. 更新操作
```java
// 获取并更新
Model book = DBTool.getIns("book").get(1);
book.put("price", 69.0);        // 修改价格
book.put("update_time", System.currentTimeMillis());
book.save();                    // 保存更新

// 批量更新（通过SQL）
String sql = "UPDATE book SET price = price * 0.8 WHERE category_id = ?";
int affected = DBTool.getIns("book").getDelegate().runSQL(sql, 1);
```

### 3. 删除操作
```java
// 根据主键删除
int affected = DBTool.getIns("book").del(1);
int affected = DBTool.getIns("book").del("abc123"); // 字符串主键

// 根据指定字段删除
int affected = DBTool.getIns("book").delBy("status", "draft");
int affected = DBTool.getIns("book").delBy("author", "某作者");

// 删除Model对象
Model book = DBTool.getIns("book").get(1);
int affected = DBTool.getIns("book").del(book);
// 或者直接调用Model的方法
int affected = book.delete();
```

## 原生SQL支持

### 1. 执行查询SQL
```java
// 执行原生SQL查询
String sql = "SELECT * FROM book WHERE price BETWEEN ? AND ? ORDER BY sales DESC";
LasyList books = DBTool.getIns("book").sql(sql, 50, 200);

// 复杂统计查询
String sql = "SELECT category_id, COUNT(*) as book_count, AVG(price) as avg_price " +
             "FROM book WHERE status = ? GROUP BY category_id";
LasyList stats = DBTool.getIns("book").sql(sql, "published");

for (Model stat : stats) {
    int categoryId = stat.getInt("category_id");
    int bookCount = stat.getInt("book_count");
    double avgPrice = stat.getDouble("avg_price");
    System.out.println(String.format("分类%d: %d本书，平均价格%.2f", 
                       categoryId, bookCount, avgPrice));
}
```

### 2. 执行更新SQL
```java
// 批量更新
String updateSql = "UPDATE book SET discount_price = price * 0.8 WHERE category_id = ?";
int affected = DBTool.getIns("book").getDelegate().runSQL(updateSql, 1);

// 复杂插入
String insertSql = "INSERT INTO book_log (book_id, action, create_time) " +
                   "SELECT id, 'view', ? FROM book WHERE status = 'published'";
int affected = DBTool.getIns("book").getDelegate().runSQL(insertSql, 
                                                          System.currentTimeMillis());
```

## 表结构信息

### 1. 获取表信息
```java
DBTool bookTool = DBTool.getIns("book");

// 获取主键字段名
String keyColumn = bookTool.getKeyColumn();

// 获取所有列信息
Map<String, ColumnDesc> columns = bookTool.getColumnDescriptions();

// 遍历所有列
for (String columnName : columns.keySet()) {
    ColumnDesc desc = columns.get(columnName);
    System.out.println(String.format("列名: %s, 类型: %s, 注释: %s", 
                       columnName, desc.typeName, desc.remark));
}
```

### 2. 获取列详细信息
```java
// 获取列注释
String titleRemark = bookTool.getColumnRemark("title");
String priceRemark = bookTool.getColumnRemark("price");

// 获取列类型
int titleType = bookTool.getColumnType("title");        // java.sql.Types中的常量
String titleTypeName = bookTool.getColumnTypeName("title"); // 如"VARCHAR"

// 检查列是否存在
boolean hasIsbn = bookTool.hasColumn("isbn");
boolean hasDescription = bookTool.hasColumn("description");
```

## 统计和聚合

### 1. 记录计数
```java
// 获取表总记录数
long totalBooks = DBTool.getIns("book").all().size();

// 条件计数
long publishedBooks = DBTool.getIns("book")
    .all()
    .eq("status", "published")
    .size();

// 复杂条件计数
long expensiveBooks = DBTool.getIns("book")
    .all()
    .gt("price", 100)
    .eq("status", "published")
    .size();
```

### 2. 聚合查询
```java
// 使用SQL进行聚合
String sql = "SELECT COUNT(*) as total, AVG(price) as avg_price, " +
             "MAX(price) as max_price, MIN(price) as min_price FROM book " +
             "WHERE status = ?";
LasyList result = DBTool.getIns("book").sql(sql, "published");

if (result.size() > 0) {
    Model stats = result.get(0);
    long total = stats.getLong("total");
    double avgPrice = stats.getDouble("avg_price");
    double maxPrice = stats.getDouble("max_price");
    double minPrice = stats.getDouble("min_price");
    
    System.out.println(String.format("总数: %d, 平均价格: %.2f, 最高价格: %.2f, 最低价格: %.2f",
                       total, avgPrice, maxPrice, minPrice));
}
```

## 高级特性

### 1. 事务支持
```java
try {
    // 开始事务
    DBHelper.getIns("mysql").beginTransaction();
    
    // 创建订单
    Model order = DBTool.getIns("order").create();
    order.put("user_id", userId);
    order.put("total_amount", totalAmount);
    order.put("status", "pending");
    order.save();
    
    // 创建订单项
    for (CartItem item : cartItems) {
        Model orderItem = DBTool.getIns("order_item").create();
        orderItem.put("order_id", order.get("id"));
        orderItem.put("book_id", item.getBookId());
        orderItem.put("quantity", item.getQuantity());
        orderItem.put("price", item.getPrice());
        orderItem.save();
        
        // 更新库存
        Model book = DBTool.getIns("book").get(item.getBookId());
        int newStock = book.getInt("stock") - item.getQuantity();
        book.put("stock", newStock);
        book.save();
    }
    
    // 提交事务
    DBHelper.getIns("mysql").commit();
    
} catch (Exception e) {
    // 回滚事务
    DBHelper.getIns("mysql").rollback();
    throw e;
}
```

### 2. 连接池管理
```java
// 获取数据库连接信息
DBHelper helper = DBHelper.getIns("mysql");

// 手动管理连接（通常不需要）
Connection conn = helper.getConnection();
try {
    // 执行数据库操作
} finally {
    helper.closeConnection(conn);
}
```

### 3. 缓存机制
```java
// DBTool实例缓存
DBTool tool1 = DBTool.getIns("book"); // 创建新实例
DBTool tool2 = DBTool.getIns("book"); // 返回缓存的实例

// 表结构信息缓存
// 第一次访问时从数据库获取表结构，后续访问使用缓存
Map<String, ColumnDesc> columns = tool1.getColumnDescriptions();
```

## 实际应用示例

### 1. 用户管理系统
```java
public class UserService {
    private DBTool userTool = DBTool.getIns("user");
    
    // 用户注册
    public Model registerUser(String username, String email, String password) {
        // 检查用户名是否存在
        Model existingUser = userTool.get("username", username);
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        Model user = userTool.create();
        user.put("username", username);
        user.put("email", email);
        user.put("password", hashPassword(password));
        user.put("create_time", System.currentTimeMillis());
        user.put("status", "active");
        
        user.save();
        return user;
    }
    
    // 用户登录
    public Model login(String username, String password) {
        Model user = userTool.get("username", username);
        if (user != null && verifyPassword(password, user.getStr("password"))) {
            // 更新最后登录时间
            user.put("last_login_time", System.currentTimeMillis());
            user.save();
            return user;
        }
        return null;
    }
    
    // 获取活跃用户列表
    public LasyList getActiveUsers(int page, int pageSize) {
        return userTool.all()
            .eq("status", "active")
            .orderBy("last_login_time DESC")
            .limit(pageSize)
            .offset((page - 1) * pageSize);
    }
}
```

### 2. 商品管理系统
```java
public class ProductService {
    private DBTool productTool = DBTool.getIns("product");
    private DBTool categoryTool = DBTool.getIns("category");
    
    // 搜索商品
    public LasyList searchProducts(String keyword, Integer categoryId, 
                                  Double minPrice, Double maxPrice) {
        LasyList query = productTool.all().eq("status", "active");
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query = query.like("name", "%" + keyword + "%")
                        .or()
                        .like("description", "%" + keyword + "%");
        }
        
        if (categoryId != null) {
            query = query.eq("category_id", categoryId);
        }
        
        if (minPrice != null) {
            query = query.gte("price", minPrice);
        }
        
        if (maxPrice != null) {
            query = query.lte("price", maxPrice);
        }
        
        return query.orderBy("sales DESC, rating DESC");
    }
    
    // 获取热门商品
    public LasyList getHotProducts(int limit) {
        return productTool.all()
            .eq("status", "active")
            .gt("sales", 0)
            .orderBy("sales DESC")
            .limit(limit);
    }
    
    // 更新商品库存
    public boolean updateStock(int productId, int quantity) {
        Model product = productTool.get(productId);
        if (product != null) {
            int currentStock = product.getInt("stock");
            if (currentStock >= quantity) {
                product.put("stock", currentStock - quantity);
                product.put("sales", product.getInt("sales") + quantity);
                product.save();
                return true;
            }
        }
        return false;
    }
}
```

### 3. 报表统计
```java
public class ReportService {
    
    // 销售统计报表
    public List<Map<String, Object>> getSalesReport(long startTime, long endTime) {
        String sql = "SELECT DATE(FROM_UNIXTIME(create_time/1000)) as date, " +
                     "COUNT(*) as order_count, SUM(total_amount) as total_sales " +
                     "FROM order WHERE create_time BETWEEN ? AND ? " +
                     "AND status IN ('paid', 'completed') " +
                     "GROUP BY DATE(FROM_UNIXTIME(create_time/1000)) " +
                     "ORDER BY date DESC";
        
        LasyList results = DBTool.getIns("order").sql(sql, startTime, endTime);
        
        List<Map<String, Object>> report = new ArrayList<>();
        for (Model row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row.getStr("date"));
            item.put("orderCount", row.getInt("order_count"));
            item.put("totalSales", row.getDouble("total_sales"));
            report.add(item);
        }
        
        return report;
    }
    
    // 商品销量排行
    public LasyList getTopSellingProducts(int limit) {
        String sql = "SELECT p.id, p.name, p.price, SUM(oi.quantity) as total_sold " +
                     "FROM product p " +
                     "JOIN order_item oi ON p.id = oi.product_id " +
                     "JOIN order o ON oi.order_id = o.id " +
                     "WHERE o.status IN ('paid', 'completed') " +
                     "GROUP BY p.id, p.name, p.price " +
                     "ORDER BY total_sold DESC " +
                     "LIMIT ?";
        
        return DBTool.getIns("product").sql(sql, limit);
    }
}
```

## 性能优化

### 1. 查询优化
```java
// 使用索引字段进行查询
LasyList users = DBTool.getIns("user")
    .all()
    .eq("email", email)        // email字段建立了索引
    .eq("status", "active");   // status字段建立了索引

// 避免全表扫描
LasyList products = DBTool.getIns("product")
    .all()
    .eq("category_id", categoryId)  // 先用索引字段过滤
    .like("name", "%" + keyword + "%"); // 再进行模糊查询

// 合理使用limit
LasyList recentOrders = DBTool.getIns("order")
    .all()
    .orderBy("create_time DESC")
    .limit(100);  // 限制结果集大小
```

### 2. 批量操作
```java
// 批量插入
List<Map<String, Object>> batchData = new ArrayList<>();
for (ImportData data : importList) {
    Map<String, Object> item = new HashMap<>();
    item.put("name", data.getName());
    item.put("price", data.getPrice());
    item.put("category_id", data.getCategoryId());
    batchData.add(item);
}

// 使用事务批量插入
DBHelper.getIns("mysql").beginTransaction();
try {
    for (Map<String, Object> item : batchData) {
        Model product = DBTool.getIns("product").create();
        product.putAll(item);
        product.save();
    }
    DBHelper.getIns("mysql").commit();
} catch (Exception e) {
    DBHelper.getIns("mysql").rollback();
    throw e;
}
```

## 最佳实践

1. **合理使用缓存**: DBTool实例和表结构信息都有缓存，避免重复创建

2. **索引优化**: 在经常查询的字段上建立索引，特别是where条件中的字段

3. **事务管理**: 对于涉及多表操作的业务，使用事务确保数据一致性

4. **分页查询**: 对于大数据量查询，使用limit和offset进行分页

5. **SQL优化**: 复杂查询使用原生SQL，简单查询使用链式API

6. **连接池配置**: 合理配置数据库连接池参数，避免连接泄漏

DBTool类是YangMVC框架数据访问层的核心，通过它可以实现高效、灵活的数据库操作，大大简化了数据访问代码的编写。