# Controller类文档

## 概述

Controller类是YangMVC框架中所有控制器的父类，提供了完整的MVC控制器功能。它封装了HTTP请求处理、响应输出、参数获取、模板渲染、会话管理等核心功能，让开发者能够快速构建Web应用。

## 类信息

- **包名**: `org.docshare.mvc`
- **作者**: 杨同峰
- **继承关系**: 所有业务控制器都需要继承此类
- **设计模式**: 模板方法模式，提供基础功能，子类实现具体业务逻辑

## 核心属性

### HTTP相关对象
```java
public HttpServletRequest request;     // 请求对象，同JSP中的request
public HttpServletResponse response;   // 响应对象，同JSP中的response  
public HttpSession session;            // 会话对象，同JSP中的session
protected PrintWriter writer;          // 输出对象，类似JSP中的out对象
```

### 内部状态管理
```java
private Map<String, Object> paramMap;  // 参数映射表
private boolean can_out;               // 输出控制标志，防止重复输出
Map<String,Object> root;               // 渲染模板所用数据
```

## 主要功能模块

### 1. 参数获取与处理

#### 基础参数获取
```java
// 获取字符串参数
public String param(String name)
public String param(String name, String defaultValue)

// 获取整数参数
public int paramInt(String name, int defaultValue)

// 获取参数映射
public Map<String, String[]> getParamMap()
```

#### 高级参数处理
```java
// 自动收集表单数据到Model对象
public Model paramToModel(Model m)

// 检查必需参数
public void requireParam(String... params) throws NullParamException
```

**使用示例**:
```java
public class BookController extends Controller {
    public void add() {
        // 获取参数
        String title = param("title");
        int price = paramInt("price", 0);
        
        // 或者自动收集到Model
        Model book = Model.tool("book").create();
        paramToModel(book); // 自动将表单数据填充到book对象
        book.save();
    }
}
```

### 2. 输出功能

#### 文本输出
```java
// 输出普通文本
public void output(String content)

// 输出JSON数据
public void outputJSON(Object obj)
public void outputJSON(Object obj, boolean pretty)
```

#### 文件下载
```java
// 下载文件
public void downloadFile(String filePath)
public void downloadFile(String filePath, String fileName)
```

#### 页面跳转
```java
// 重定向
public void jump(String url)

// 转发
public void render(String templatePath)
```

**使用示例**:
```java
public void api() {
    List<Book> books = getBooks();
    outputJSON(books); // 输出JSON格式数据
}

public void download() {
    downloadFile("/files/report.pdf", "月度报告.pdf");
}
```

### 3. 模板渲染

#### JSP渲染
```java
// 使用默认JSP模板
public JSP jsp()

// 使用指定JSP模板
public JSP jsp(String path)

// 渲染JSP页面
public void render(String path)
```

#### FreeMarker渲染
```java
// 使用FreeMarker模板
public FreeMarker freemarker(String path)

// 渲染FreeMarker模板
public void renderFreeMarker(String path)
```

#### 数据传递
```java
// 向模板传递数据
public void put(String name, Object obj)

// 传递Model对象的所有字段
public void putModelItem(Model m)
```

**使用示例**:
```java
public void list() {
    LasyList books = Model.tool("book").all().limit(10);
    put("books", books);
    put("title", "图书列表");
    render(); // 使用默认模板渲染
}

public JSP detail() {
    int id = paramInt("id", 0);
    Model book = Model.tool("book").get(id);
    putModelItem(book); // 将book的所有字段传递给模板
    return jsp("book/detail.jsp"); // 返回特定JSP
}
```

### 4. 方法参数自动映射

#### 参数自动注入功能
YangMVC支持将HTTP请求参数自动映射到控制器方法的参数中，这需要在编译时保留参数名信息。

**Maven配置要求**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.0</version>
    <configuration>
        <source>1.8</source>
        <target>1.8</target>
        <encoding>UTF-8</encoding>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

#### 支持的参数类型
```java
// 基本数据类型
public Integer add(Integer a, Integer b) {
    return a + b;
}

// 字符串类型
public String concat(String firstName, String lastName) {
    return firstName + " " + lastName;
}

// 布尔类型
public String checkStatus(Boolean isActive) {
    return isActive ? "激活" : "未激活";
}

// 浮点数类型
public Double calculateArea(Double width, Double height) {
    return width * height;
}
```

**使用示例**:
```java
public class CalculatorController extends Controller {
    
    // 请求: /calculator/add?a=10&b=20
    // 返回: 30
    public Integer add(Integer a, Integer b) {
        return a + b;
    }
    
    // 请求: /calculator/multiply?x=5&y=3
    // 返回: 15
    public Integer multiply(Integer x, Integer y) {
        return x * y;
    }
    
    // 请求: /calculator/divide?dividend=100&divisor=5
    // 返回: 20.0
    public Double divide(Double dividend, Double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return dividend / divisor;
    }
    
    // 请求: /calculator/power?base=2&exponent=3
    // 返回: 8.0
    public Double power(Double base, Integer exponent) {
        return Math.pow(base, exponent);
    }
}
```

#### 复杂参数映射示例
```java
public class UserController extends Controller {
    
    // 用户注册
    // 请求: /user/register?username=john&email=john@example.com&age=25
    public String register(String username, String email, Integer age) {
        if (username == null || email == null) {
            return "用户名和邮箱不能为空";
        }
        
        Model user = Model.tool("user").create();
        user.put("username", username);
        user.put("email", email);
        user.put("age", age);
        user.save();
        
        return "注册成功";
    }
    
    // 搜索用户
    // 请求: /user/search?keyword=john&minAge=18&maxAge=65&isActive=true
    public LasyList search(String keyword, Integer minAge, Integer maxAge, Boolean isActive) {
        LasyList query = Model.tool("user").all();
        
        if (keyword != null) {
            query = query.like("username", "%" + keyword + "%");
        }
        
        if (minAge != null) {
            query = query.gte("age", minAge);
        }
        
        if (maxAge != null) {
            query = query.lte("age", maxAge);
        }
        
        if (isActive != null) {
            query = query.eq("is_active", isActive);
        }
        
        return query.limit(20);
    }
}
```

### 5. 返回值自动输出

#### 自动输出机制
YangMVC支持方法返回值的自动输出，无需手动调用输出方法。不同类型的返回值会采用不同的输出方式：

- **字符串**: 直接输出文本内容
- **基本数据类型**: 转换为字符串输出
- **对象**: 自动转换为JSON格式输出
- **集合/数组**: 自动转换为JSON数组输出

#### 基本类型自动输出
```java
public class ApiController extends Controller {
    
    // 返回字符串 - 直接输出文本
    public String hello() {
        return "Hello, YangMVC!";
    }
    
    // 返回数字 - 输出: 42
    public Integer getAnswer() {
        return 42;
    }
    
    // 返回布尔值 - 输出: true
    public Boolean isReady() {
        return true;
    }
    
    // 返回浮点数 - 输出: 3.14159
    public Double getPi() {
        return 3.14159;
    }
}
```

#### 对象自动JSON输出
```java
public class BookController extends Controller {
    
    // 返回单个对象 - 自动转换为JSON
    public Model getBook(Integer id) {
        return Model.tool("book").get(id);
    }
    
    // 返回列表 - 自动转换为JSON数组
    public LasyList getBooks() {
        return Model.tool("book")
            .all()
            .eq("status", "published")
            .orderBy("create_time DESC")
            .limit(10);
    }
    
    // 返回Map - 自动转换为JSON对象
    public Map<String, Object> getBookStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", Model.tool("book").all().size());
        stats.put("publishedBooks", Model.tool("book").all().eq("status", "published").size());
        stats.put("averagePrice", calculateAveragePrice());
        return stats;
    }
}
```

#### 复杂返回值示例
```java
public class OrderController extends Controller {
    
    // 创建订单并返回订单信息
    public Map<String, Object> createOrder(Integer userId, String productIds) {
        try {
            // 创建订单逻辑
            Model order = Model.tool("order").create();
            order.put("user_id", userId);
            order.put("status", "pending");
            order.put("create_time", System.currentTimeMillis());
            order.save();
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderId", order.get("id"));
            response.put("message", "订单创建成功");
            return response;
            
        } catch (Exception e) {
            // 返回错误响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }
    
    // 获取订单详情（包含关联数据）
    public Map<String, Object> getOrderDetail(Integer orderId) {
        Model order = Model.tool("order").get(orderId);
        if (order == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "订单不存在");
            return error;
        }
        
        // 获取订单项
        LasyList orderItems = Model.tool("order_item")
            .all()
            .eq("order_id", orderId);
        
        // 获取用户信息
        Model user = Model.tool("user").get(order.get("user_id"));
        
        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", orderItems);
        result.put("user", user);
        result.put("totalAmount", calculateTotalAmount(orderItems));
        
        return result;
    }
}
```

#### RESTful API示例
```java
public class ProductApiController extends Controller {
    
    // GET /api/products - 获取产品列表
    public LasyList list(Integer page, Integer size, String category) {
        int pageSize = size != null ? size : 20;
        int offset = page != null ? (page - 1) * pageSize : 0;
        
        LasyList query = Model.tool("product").all();
        
        if (category != null) {
            query = query.eq("category", category);
        }
        
        return query.limit(pageSize).offset(offset);
    }
    
    // GET /api/products/{id} - 获取单个产品
    public Model get(Integer id) {
        Model product = Model.tool("product").get(id);
        if (product == null) {
            response.setStatus(404);
            Map<String, String> error = new HashMap<>();
            error.put("error", "产品不存在");
            return null; // 或者抛出异常
        }
        return product;
    }
    
    // POST /api/products - 创建产品
    public Map<String, Object> create(String name, Double price, String description) {
        Map<String, Object> result = new HashMap<>();
        
        if (name == null || price == null) {
            response.setStatus(400);
            result.put("error", "产品名称和价格不能为空");
            return result;
        }
        
        Model product = Model.tool("product").create();
        product.put("name", name);
        product.put("price", price);
        product.put("description", description);
        product.put("create_time", System.currentTimeMillis());
        product.save();
        
        result.put("success", true);
        result.put("product", product);
        return result;
    }
}
```

### 6. 会话管理

#### Session操作
```java
// 设置Session值
public void sess(String key, Object value)

// 获取Session值
public Object sess(String key)
public Integer sessInt(String key)
public String sessStr(String key)

// 获取Application值
public Object app(String key)
```

**使用示例**:
```java
public void login() {
    String username = param("username");
    String password = param("password");
    
    if (validateUser(username, password)) {
        sess("user_id", getUserId(username));
        sess("username", username);
        jump("/dashboard");
    } else {
        put("error", "用户名或密码错误");
        render("login.jsp");
    }
}

public void profile() {
    Integer userId = sessInt("user_id");
    if (userId == null) {
        jump("/login");
        return;
    }
    
    Model user = Model.tool("user").get(userId);
    put("user", user);
    render();
}
```

### 5. 权限控制

#### 访问权限限制
```java
// 要求Session中存在指定key
public void require(String sessionKey, String jumpUrl, String errorMsg)

// 要求Session中的值等于指定值
public void require(String sessionKey, Object value, String jumpUrl, String errorMsg)

// 检查权限（框架自动调用）
public boolean checkRequire()

// 权限验证失败时的处理
public void actionRequire(boolean passed)
```

**使用示例**:
```java
public class AdminController extends Controller {
    public AdminController() {
        // 要求用户已登录且为管理员
        require("user_role", "admin", "/login", "需要管理员权限");
    }
    
    public void userList() {
        // 只有管理员能访问此方法
        LasyList users = Model.tool("user").all();
        put("users", users);
        render();
    }
}
```

### 6. 分页功能

```java
// 生成分页数据
public void genPage(int total, int pageSize)
public void genPage(int total, int pageSize, String queryString)
```

**使用示例**:
```java
public void list() {
    int page = paramInt("page", 1);
    int pageSize = 20;
    
    LasyList books = Model.tool("book")
        .all()
        .limit(pageSize)
        .offset((page - 1) * pageSize);
    
    long total = Model.tool("book").all().size();
    
    put("books", books);
    genPage((int)total, pageSize); // 生成分页数据
    render();
}
```

### 7. 工具方法

#### 路径和方法信息
```java
// 获取当前请求路径
public String getPath()

// 获取请求方法
public String method()

// 获取默认模板路径
public String getDefaultTemp()
```

#### JSON处理
```java
// 对象转JSON字符串
public String json(Object obj)
public String json(Object obj, boolean pretty)

// 调试输出参数
public void dump()
```

#### 性能优化
```java
// 启用/禁用Gzip压缩
public static void enableGzip()
public static void disableGzip()

// 设置单例模式
public void setSingleton(boolean singleton)
```

## 约定优于配置

### 控制器命名约定
- `IndexController` → 对应网站根目录 `/`
- `BookController` → 对应路径 `/book`
- `UserController` → 对应路径 `/user`

### 方法映射约定
- `public void index()` → `/controller/`
- `public void list()` → `/controller/list`
- `public void add()` → `/controller/add`

### 模板路径约定
- 默认模板路径：`/view/{controller}/{method}.jsp`
- 如：`BookController.list()` → `/view/book/list.jsp`

## 最佳实践

### 1. 错误处理
```java
public void save() {
    try {
        requireParam("title", "content"); // 检查必需参数
        
        Model article = Model.tool("article").create();
        paramToModel(article);
        article.save();
        
        outputJSON(Map.of("success", true, "message", "保存成功"));
    } catch (NullParamException e) {
        outputJSON(Map.of("success", false, "message", e.getMessage()));
    }
}
```

### 2. 统一响应格式
```java
public void apiResponse(boolean success, String message, Object data) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", success);
    result.put("message", message);
    result.put("data", data);
    outputJSON(result);
}
```

### 3. 权限检查
```java
public class BaseController extends Controller {
    protected boolean isLoggedIn() {
        return sessInt("user_id") != null;
    }
    
    protected void requireLogin() {
        if (!isLoggedIn()) {
            jump("/login");
        }
    }
}
```

## 注意事项

1. **单次输出原则**: 每个控制器方法只能输出一次，避免在同一方法中多次调用`output`、`outputJSON`等方法

2. **资源管理**: 框架自动管理`PrintWriter`等资源的关闭，无需手动处理

3. **线程安全**: 控制器默认为多例模式，每次请求创建新实例，确保线程安全

4. **参数验证**: 使用`requireParam`进行参数验证，避免空指针异常

5. **模板数据**: 使用`put`方法传递数据到模板，支持EL表达式和JSTL

## 扩展功能

### 自定义控制器基类
```java
public abstract class BaseController extends Controller {
    protected Model getCurrentUser() {
        Integer userId = sessInt("user_id");
        return userId != null ? Model.tool("user").get(userId) : null;
    }
    
    protected void requireRole(String role) {
        Model user = getCurrentUser();
        if (user == null || !role.equals(user.getStr("role"))) {
            jump("/unauthorized");
        }
    }
}
```

### 统一异常处理
```java
public class ApiController extends Controller {
    protected void handleException(Exception e) {
        Log.e(e);
        outputJSON(Map.of(
            "success", false,
            "message", "系统错误，请稍后重试",
            "error", e.getMessage()
        ));
    }
}
```

Controller类是YangMVC框架的核心组件，通过继承它可以快速构建功能完整的Web控制器，实现高效的Web开发。