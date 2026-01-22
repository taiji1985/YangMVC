# YangMVC API 速查手册

> 快速查找YangMVC最常用的API方法和代码示例

---

## Controller 核心API

### 包路径
```java
import org.docshare.yangmvc.mvc.Controller;
```

### 参数获取

```java
// 字符串参数
String name = param("username");
String title = param("title", "默认值");

// 整数参数
Integer id = paramInt("id");           // 返回Integer，可能为null
Integer page = paramInt("page", 1);    // 带默认值

// 获取所有参数
Map<String, String[]> map = getParamMap();
```

### ⭐ 方法参数自动映射（推荐）

```java
// 需要在pom.xml添加 -parameters 编译参数
// 请求: /user/login?username=admin&password=123456
public Map<String, Object> login(String username, String password) {
    // 参数自动注入，无需手动获取
    Model user = Model.tool("user").get("username", username);
    return Map.of("success", user != null);
}

// 支持的类型
public void demo(String name, Integer age, Double price, Boolean isActive) {
    // String, Integer, Long, Double, Boolean 等基本类型
}
```

**Maven配置**：
```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>  <!-- 必须添加 -->
        </compilerArgs>
    </configuration>
</plugin>
```

### 数据输出

```java
// 文本输出
output("Hello World");

// JSON输出
outputJSON(object);
outputJSON(object, true);  // 格式化输出

// 返回值自动输出（推荐）
public String hello() { return "Hello"; }
public Model getUser() { return Model.tool("user").get(1); }
public Map<String, Object> info() { return map; }
```

### 模板渲染

```java
// 传递数据到模板
put("title", "标题");
put("user", user);

// 渲染FreeMarker模板
renderFreeMarker("/user/list.html");
```

### 重定向与跳转

```java
jump("/login");           // 重定向
response.sendRedirect(url);
```

### Session管理

```java
// 设置
sess("user_id", 123);
sess("username", "张三");

// 获取
Integer userId = sessInt("user_id");
String name = sessStr("username");
Object obj = sess("key");

// 删除
removeSession("user_id");
```

### 请求信息

```java
request()            // HttpServletRequest
response()           // HttpServletResponse
session()            // HttpSession
method()             // GET/POST/PUT
getPath()            // 请求路径
isGet() / isPost()   // 请求方法判断
```

---

## Model 核心API

### 包路径
```java
import org.docshare.yangmvc.orm.Model;
```

### 创建Model

```java
// 获取DBTool
DBTool tool = Model.tool("user");

// 创建新记录
Model user = Model.tool("user").create();

// 获取已有记录
Model user = Model.tool("user").get(1);
Model user = Model.tool("user").get("email", "user@example.com");
```

### 数据操作

```java
// 设置值
user.put("username", "张三");
user.put("age", 25);
user.putAll(map);

// 获取值
Object name = user.get("username");
String name = user.getStr("username");
Integer age = user.getInt("age");
Long time = user.getLong("create_time");
Double price = user.getDouble("price");

// 检查字段
boolean has = user.containsKey("email");
Set<String> keys = user.keySet();  // 所有字段名

// 数据库操作
int affected = user.save();         // 保存（智能判断insert/update）
int affected = user.insert();       // 插入
int affected = user.update();       // 更新
int affected = user.delete();       // 删除
```

### 表信息

```java
String tableName = user.getTableName();  // 表名
String keyField = user.key();            // 主键字段名
String remark = user.remark("username"); // 字段注释
```

---

## DBTool 核心API

### 包路径
```java
import org.docshare.yangmvc.orm.DBTool;
```

### 获取实例

```java
DBTool tool = Model.tool("user");
// 或
DBTool tool = DBTool.getIns("user");
```

### 基础查询

```java
// 获取所有
LasyList all = tool.all();

// 主键查询
Model user = tool.get(1);

// 字段查询
Model user = tool.get("email", "test@example.com");

// 原生SQL
LasyList list = tool.sql("SELECT * FROM user WHERE age > ?", 18);
```

### 删除操作

```java
int affected = tool.del(1);                      // 主键删除
int affected = tool.delBy("status", "deleted");  // 条件删除
int affected = tool.del(model);                  // Model删除
```

---

## LasyList 核心API

### 包路径
```java
import org.docshare.yangmvc.orm.LasyList;
```

### 条件查询

```java
LasyList list = Model.tool("user").all()
    .eq("status", "active")           // 等于
    .ne("status", "deleted")          // 不等于
    .gt("age", 18)                    // 大于
    .gte("age", 18)                   // 大于等于
    .lt("age", 60)                    // 小于
    .lte("age", 60)                   // 小于等于
    .like("username", "%张%")         // 模糊查询
    .isNull("email")                  // 为空
    .isNotNull("email")               // 不为空
    .in("id", Arrays.asList(1,2,3));  // IN查询
```

### 排序与分页

```java
LasyList list = Model.tool("user").all()
    .orderBy("create_time DESC")      // 排序
    .limit(20)                        // 限制条数
    .offset(0)                        // 偏移量
    .page(1, 20);                     // 分页（第1页，每页20条）
```

### 数据获取

```java
// 获取数量
int count = list.size();

// 判断是否存在
boolean has = list.exist();
boolean empty = list.isEmpty();

// 获取单个
Model first = list.one();
Model item = list.get(0);

// 转换为List
List<Model> models = list.toArrayList();
List<User> users = list.toArrayList(User.class);

// 遍历
for (Model item : list) {
    System.out.println(item.get("name"));
}
```

### 原生SQL

```java
LasyList list = LasyList.fromRawSql("SELECT * FROM user WHERE age > ?", 18);
```

---

## URL映射规则

| 控制器类 | 方法 | URL路径 |
|---------|------|---------|
| IndexController | index() | `/` |
| BookController | index() | `/book/` |
| BookController | list() | `/book/list` |
| BookController | detail() | `/book/detail` |
| UserController | profile() | `/user/profile` |

**规则**: `{Controller名去掉Controller}/{方法名}`

---

## 常用代码片段

### CRUD完整示例

```java
// Create
Model user = Model.tool("user").create();
user.put("username", "张三");
user.put("email", "test@example.com");
user.put("create_time", System.currentTimeMillis());
user.save();

// Read
Model user = Model.tool("user").get(1);
LasyList users = Model.tool("user").all()
    .eq("status", "active")
    .orderBy("create_time DESC")
    .limit(10);

// Update
Model user = Model.tool("user").get(1);
user.put("email", "new@example.com");
user.save();

// Delete
int affected = Model.tool("user").del(1);
```

### 条件查询

```java
LasyList users = Model.tool("user").all()
    .eq("status", "active")
    .gte("age", 18)
    .like("username", "%张%")
    .orderBy("create_time DESC")
    .limit(20);
```

### Controller方法示例

```java
// 使用方法参数自动映射（推荐）
public Object getUser(Integer id) {
    Model user = Model.tool("user").get(id);
    if (user == null) {
        return Map.of("error", "用户不存在");
    }
    return Map.of("data", user);
}

public Object save(String username, String email, Integer age) {
    if (username == null) {
        return Map.of("error", "用户名不能为空");
    }

    Model user = Model.tool("user").create();
    user.put("username", username);
    user.put("email", email);
    user.put("age", age);
    user.put("create_time", System.currentTimeMillis());
    user.save();

    return Map.of("success", true, "id", user.get("id"));
}

// 传统方式：手动获取参数
public void list() {
    LasyList users = Model.tool("user").all()
        .orderBy("create_time DESC")
        .limit(20);
    put("users", users);
    renderFreeMarker("/user/list.html");
}
```

### JSON API响应

```java
// 成功响应
Map<String, Object> result = new HashMap<>();
result.put("code", 200);
result.put("message", "success");
result.put("data", data);
return result;

// 错误响应
Map<String, Object> error = new HashMap<>();
error.put("code", 400);
error.put("message", "参数错误");
return error;
```

### 类型转换

```java
Model user = Model.tool("user").get(1);
String name = user.getStr("username");
Integer age = user.getInt("age");
Long time = user.getLong("create_time");
Double score = user.getDouble("score");
```

### 事务处理

```java
try {
    DBHelper.getIns("mysql").beginTransaction();

    Model user1 = Model.tool("user").get(1);
    Model user2 = Model.tool("user").get(2);

    user1.put("balance", user1.getInt("balance") - 100);
    user2.put("balance", user2.getInt("balance") + 100);

    user1.save();
    user2.save();

    DBHelper.getIns("mysql").commit();
} catch (Exception e) {
    DBHelper.getIns("mysql").rollback();
    throw e;
}
```

---

## 常用方法签名速查

### Controller

```java
// 参数
String param(String name)
String param(String name, String def)
Integer paramInt(String name)
Integer paramInt(String name, int def)

// 输出
void output(String text)
void outputJSON(Object obj)

// 模板
void put(String key, Object value)
void renderFreeMarker(String path)

// 跳转
void jump(String url)

// Session
void sess(String key, Object value)
Object sess(String key)
Integer sessInt(String key)
String sessStr(String key)
void removeSession(String key)

// 请求
HttpServletRequest request()
HttpServletResponse response()
HttpSession session()
String method()
String getPath()
boolean isGet()
boolean isPost()
```

### Model

```java
// 创建
static DBTool tool(String tableName)
Model create()
Model get(Object id)
Model get(String column, Object value)

// 数据
Object get(String key)
String getStr(String key)
Integer getInt(String key)
Long getLong(String key)
Double getDouble(String key)
void put(String key, Object value)
void putAll(Map<String, Object> map)

// 数据库
int save()
int insert()
int update()
int delete()

// 信息
String getTableName()
String key()
Set<String> keySet()
boolean containsKey(String key)
```

### LasyList

```java
// 条件
LasyList eq(String column, Object value)
LasyList ne(String column, Object value)
LasyList gt(String column, Object value)
LasyList gte(String column, Object value)
LasyList lt(String column, Object value)
LasyList lte(String column, Object value)
LasyList like(String column, String pattern)
LasyList isNull(String column)
LasyList isNotNull(String column)
LasyList in(String column, Collection<?> values)

// 排序分页
LasyList orderBy(String column, boolean asc)
LasyList limit(int len)
LasyList offset(int start)
LasyList page(int pageNo, int pageSize)

// 获取
int size()
boolean exist()
boolean isEmpty()
Model one()
Model get(int index)
List<Model> toArrayList()
<T> List<T> toArrayList(Class<T> clazz)
```

---

## 注意事项

1. **包名正确**:
   - Controller: `org.docshare.yangmvc.mvc.Controller`
   - Model/DBTool/LasyList: `org.docshare.yangmvc.orm.*`

2. **⭐ 方法参数自动映射**:
   - 必须在pom.xml添加`-parameters`编译参数
   - 参数名与HTTP请求参数名必须一致
   - 参数可能为`null`，建议判空处理

3. **参数获取**:
   - `paramInt("id")` 返回 `Integer`，可能为 `null`
   - `paramInt("page", 1)` 带默认值，不会为 `null`

4. **Model遍历**:
   - 使用 `model.keySet()` 获取所有字段名（不是 `keys()`）

5. **单次输出**:
   - 每个Controller方法只能输出一次
   - 返回值自动输出与 `output()` 不能混用

6. **URL映射**:
   - Controller类名去掉`Controller`后缀
   - 方法名直接映射到URL

---

**版本**: 2025.07.06
**更新**: 2026-01-02
