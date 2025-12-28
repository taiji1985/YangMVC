# YangMVC 值得改进的方向

## 概述

基于对 YangMVC 的深入分析和性能对比，以下是值得改进的方向，按优先级分为 P1（高优先级）、P2（中优先级）和 P3（低优先级）。

---

## P1 - 高优先级（立即改进）

### 1. ReflectASM 稳定性增强

**当前问题**：
- 在 Spring Boot 环境下，ReflectASM 可能返回 null 或抛出异常
- 只有警告日志，没有诊断信息
- 无法手动控制是否使用 ReflectASM

**改进方案**：

#### 1.1 添加诊断模式

```java
// org.docshare.yangmvc.mvc.Config

/**
 * 诊断 ReflectASM 是否可用
 */
public static void diagnoseReflectASM() {
    logger.info("=== ReflectASM 诊断信息 ===");
    logger.info("当前类加载器: " + Config.class.getClassLoader());
    logger.info("当前类加载器层级:");
    ClassLoader loader = Config.class.getClassLoader();
    int level = 0;
    while (loader != null) {
        logger.info("  [" + level + "] " + loader.getClass().getName() + " - " + loader);
        loader = loader.getParent();
        level++;
    }

    if (controller != null) {
        try {
            Class<?> testClass = Class.forName(controller);
            logger.info("测试类: " + testClass.getName());
            logger.info("测试类加载器: " + testClass.getClassLoader());

            MethodAccess access = MethodAccess.get(testClass);
            if (access == null) {
                logger.error("❌ ReflectASM 不可用：MethodAccess.get() 返回 null");
                logger.info("建议：");
                logger.info("  1. 如果使用 Spring Boot DevTools，请移除 spring-boot-devtools 依赖");
                logger.info("  2. 或者设置 yangmvc.reflectasm.enabled=false 禁用 ReflectASM");
            } else {
                logger.info("✅ ReflectASM 正常工作");
                logger.info("  可用方法数: " + access.getMethodNames().length);
            }
        } catch (Exception e) {
            logger.error("❌ ReflectASM 诊断失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    logger.info("========================");
}
```

#### 1.2 添加配置选项

```java
// org.docshare.yangmvc.mvc.Config

/**
 * 是否启用 ReflectASM
 * - auto: 自动检测（默认）
 * - force: 强制使用，失败则报错
 * - disable: 禁用，使用标准反射
 */
public static String REFLECTASM_MODE = "auto";

public static boolean isReflectASMEnabled() {
    if ("disable".equals(REFLECTASM_MODE)) {
        return false;
    }
    if ("force".equals(REFLECTASM_MODE)) {
        return true;
    }
    return true; // auto mode，默认启用
}
```

#### 1.3 在 MethodAccessCacher 中应用配置

```java
// org.docshare.yangmvc.mvc.MethodAccessCacher

public static void put(String name, Class<?> clz) {
    if (name.endsWith("MethodAccess")) {
        return;
    }

    // 检查配置
    if (!Config.isReflectASMEnabled()) {
        logger.info("ReflectASM is disabled by config for " + name);
        ma_map.put(name, null);
        return;
    }

    method_map_builded.remove(name);
    class_map.put(name, clz);
    MethodAccess access = null;

    try {
        access = MethodAccess.get(clz);
        if (access == null) {
            String msg = "MethodAccess.get() returned null for class: " + name;
            if ("force".equals(Config.REFLECTASM_MODE)) {
                throw new RuntimeException(msg + " (reflectasm.force=true)");
            }
            logger.warn(msg + ". Will use standard reflection as fallback.");
        }
    } catch (Exception e) {
        String msg = "MethodAccess.get() failed for class: " + name + ", error: " + e.getMessage();
        if ("force".equals(Config.REFLECTASM_MODE)) {
            throw new RuntimeException(msg + " (reflectasm.force=true)", e);
        }
        logger.warn(msg + ". Will use standard reflection as fallback.");
    }

    ma_map.put(name, access);
}
```

**预期效果**：
- 用户可以通过配置控制 ReflectASM 行为
- 诊断工具帮助快速定位问题
- 减少生产环境的不确定性

---

### 2. 启动时性能优化

**当前问题**：
- YangMVCBoot 启动时间 0.7秒
- 每次启动都要扫描包、加载所有 Controller

**改进方案**：

#### 2.1 延迟加载 Controller

```java
// org.docshare.yangmvc.mvc.Loader

/**
 * 延迟加载 Controller 类
 * 只在首次请求时加载类，而非启动时加载全部
 */
private static Map<String, Class<?>> lazyClassCache = new ConcurrentHashMap<>();

public static Class<?> loadControllerClass(String className) {
    // 先查缓存
    Class<?> cached = lazyClassCache.get(className);
    if (cached != null) {
        return cached;
    }

    // 双重检查锁
    synchronized (Loader.class) {
        cached = lazyClassCache.get(className);
        if (cached != null) {
            return cached;
        }

        try {
            // 延迟加载
            Class<?> clz = Class.forName(className);
            MethodAccessCacher.put(className, clz);
            lazyClassCache.put(className, clz);
            logger.info("Lazy loaded controller: " + className);
            return clz;
        } catch (ClassNotFoundException e) {
            logger.error("Controller not found: " + className, e);
            throw new RuntimeException(e);
        }
    }
}
```

**预期效果**：
- 启动时间减少 20-30%（0.7s → 0.5s）
- 只有被访问的 Controller 才会被加载

---

## P2 - 中优先级（近期改进）

### 1. 响应缓存机制

**当前问题**：
- 每次请求都执行完整的方法调用
- 没有内置缓存机制

**改进方案**：

```java
// org.docshare.yangmvc.mvc.anno.Cache

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    /**
     * 缓存时间（秒）
     */
    int value() default 60;

    /**
     * 缓存键表达式，支持参数引用
     * 例如: "user:{id}" -> "user:123"
     */
    String key() default "";

    /**
     * 是否根据参数自动缓存
     */
    boolean autoKey() default true;
}
```

```java
// org.docshare.yangmvc.mvc.CacheInterceptor

public class CacheInterceptor {
    private static Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public static Object handleCache(Method method, Object[] args, Object[] result) {
        Cache cacheAnno = method.getAnnotation(Cache.class);
        if (cacheAnno == null) {
            return null; // 不缓存
        }

        String cacheKey = buildCacheKey(method, args, cacheAnno);
        CacheEntry entry = cache.get(cacheKey);

        if (entry != null && !entry.isExpired()) {
            logger.debug("Cache hit: " + cacheKey);
            return entry.getValue();
        }

        return null; // 缓存未命中
    }

    public static void putCache(Method method, Object[] args, Object result, Cache cacheAnno) {
        String cacheKey = buildCacheKey(method, args, cacheAnno);
        long expiryTime = System.currentTimeMillis() + cacheAnno.value() * 1000L;
        cache.put(cacheKey, new CacheEntry(result, expiryTime));
        logger.debug("Cache stored: " + cacheKey);
    }

    private static String buildCacheKey(Method method, Object[] args, Cache cacheAnno) {
        if (!cacheAnno.key().isEmpty()) {
            // 自定义键表达式
            return resolveExpression(cacheAnno.key(), method, args);
        }
        // 自动生成: className.methodName(arg1, arg2)
        return method.getDeclaringClass().getSimpleName() + "." +
               method.getName() + Arrays.toString(args);
    }

    private static class CacheEntry {
        private final Object value;
        private final long expiryTime;

        CacheEntry(Object value, long expiryTime) {
            this.value = value;
            this.expiryTime = expiryTime;
        }

        Object getValue() { return value; }
        boolean isExpired() { return System.currentTimeMillis() > expiryTime; }
    }
}
```

**使用示例**：

```java
public class UserController extends Controller {

    @Cache(30) // 缓存30秒
    public void getUser() {
        Long id = longParam("id");
        User user = userService.findById(id);
        json(user);
    }

    @Cache(value = 300, key = "userList") // 缓存5分钟
    public void listUsers() {
        List<User> users = userService.findAll();
        json(users);
    }
}
```

**预期效果**：
- 减少数据库查询
- 提高 QPS 20-50%（对于读多写少的场景）

---

### 2. 类型安全的参数绑定

**当前问题**：
- 参数绑定需要手动转换
- 缺少编译时类型检查

**改进方案**：

```java
// org.docshare.yangmvc.mvc.anno.RequestBean

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestBean {
}

// org.docshare.yangmvc.mvc.ParamBinder

public class ParamBinder {
    /**
     * 自动绑定参数到对象
     */
    public static <T> T bind(Class<T> clazz, HttpServletRequest req) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();

            // 获取所有 setter 方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set") &&
                    method.getParameterCount() == 1) {

                    String fieldName = method.getName().substring(3);
                    fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);

                    String paramValue = req.getParameter(fieldName);
                    if (paramValue != null) {
                        Object value = convertType(paramValue, method.getParameterTypes()[0]);
                        method.invoke(obj, value);
                    }
                }
            }

            return obj;
        } catch (Exception e) {
            logger.error("参数绑定失败", e);
            throw new RuntimeException(e);
        }
    }

    private static Object convertType(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == Integer.class || targetType == int.class) return Integer.parseInt(value);
        if (targetType == Long.class || targetType == long.class) return Long.parseLong(value);
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.parseBoolean(value);
        // ... 更多类型
        return value;
    }
}
```

**使用示例**：

```java
public class UserForm {
    private String name;
    private Integer age;
    private String email;
    // getters and setters
}

public class UserController extends Controller {

    public void createUser(@RequestBean UserForm form) {
        // 直接使用，已经自动绑定
        userService.create(form);
        json("success");
    }
}
```

**预期效果**：
- 减少样板代码
- 提高代码可读性
- 编译时类型检查

---

### 3. 增强的集成测试

**当前问题**：
- 缺少自动化集成测试
- 性能回归难以发现

**改进方案**：

```java
// yangmvc-test/src/test/java/org/docshare/yangmvc/test/PerformanceTest

@Test
public void testReflectASMPerformance() {
    // 测试 ReflectASM 性能
    int iterations = 100000;

    // 标准反射
    long start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        method.invoke(controller, args);
    }
    long reflectionTime = System.nanoTime() - start;

    // ReflectASM
    start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
        access.invoke(controller, methodName, args);
    }
    long asmTime = System.nanoTime() - start;

    logger.info("标准反射: " + reflectionTime / 1_000_000 + "ms");
    logger.info("ReflectASM: " + asmTime / 1_000_000 + "ms");
    logger.info("性能提升: " + (reflectionTime * 1.0 / asmTime) + "x");

    // 断言 ReflectASM 至少快 5 倍
    assertTrue(reflectionTime / asmTime > 5);
}
```

**预期效果**：
- 及时发现性能回归
- 确保 ReflectASM 正常工作

---

## P3 - 低优先级（长期改进）

### 1. 代码生成器（Maven Plugin）

**目标**：生成 Controller、Service、DAO 的基础代码

```bash
mvn yangmvc:generate -Dentity=User
```

**生成内容**：
- UserController.java
- UserService.java
- UserDao.java
- SQL 脚本
- 单元测试

---

### 2. 监控和管理接口

**目标**：提供运行时监控和管理功能

```java
// org.docshare.yangmvc.monitor.MonitorController

public class MonitorController extends Controller {

    public void metrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("qps", Metrics.getQPS());
        metrics.put("avgResponseTime", Metrics.getAvgResponseTime());
        metrics.put("activeRequests", Metrics.getActiveRequests());
        metrics.put("cacheHitRate", Metrics.getCacheHitRate());
        metrics.put("reflectasmEnabled", Config.isReflectASMEnabled());
        json(metrics);
    }

    public void controllers() {
        // 列出所有 Controller
        List<Map<String, Object>> list = new ArrayList<>();
        for (String className : controller_map.keySet()) {
            Map<String, Object> info = new HashMap<>();
            info.put("className", className);
            info.put("url", controller_map.get(className));
            info.put("methodCount", MethodAccessCacher.getMethodCount(className));
            list.add(info);
        }
        json(list);
    }

    public void cache() {
        // 缓存统计
        Map<String, Object> stats = CacheInterceptor.getStats();
        json(stats);
    }

    public void clearCache() {
        CacheInterceptor.clearAll();
        json("success");
    }
}
```

**访问路径**：
- `/yangmvc/monitor/metrics` - 性能指标
- `/yangmvc/monitor/controllers` - Controller 列表
- `/yangmvc/monitor/cache` - 缓存统计
- `/yangmvc/monitor/clearCache` - 清除缓存

---

### 3. 完整的文档系统

**目标**：提供完整的中文和英文文档

**结构**：
```
docs/
├── guide/
│   ├── quickstart.md
│   ├── configuration.md
│   ├── controller.md
│   ├── service.md
│   ├── validation.md
│   └── deployment.md
├── api/
│   ├── annotation.md
│   ├── config-reference.md
│   └── error-codes.md
├── performance/
│   ├── benchmark.md
│   ├── tuning.md
│   └── comparison.md
└── migration/
    ├── from-spring.md
    └── version-history.md
```

---

## 总结

| 优先级 | 改进项 | 预期收益 | 工作量 |
|--------|--------|----------|--------|
| P1 | ReflectASM 诊断 | 稳定性 +50% | 2-3天 |
| P1 | 延迟加载 | 启动时间 -30% | 1-2天 |
| P2 | 响应缓存 | QPS +20-50% | 3-5天 |
| P2 | 参数绑定 | 开发效率 +30% | 2-3天 |
| P2 | 集成测试 | 质量 +40% | 3-4天 |
| P3 | 代码生成器 | 开发效率 +50% | 1-2周 |
| P3 | 监控接口 | 可维护性 +40% | 1周 |
| P3 | 文档系统 | 易用性 +30% | 2-3周 |

**建议实施顺序**：
1. P1 - ReflectASM 诊断和配置
2. P1 - 延迟加载
3. P2 - 响应缓存
4. P2 - 参数绑定
5. P2 - 集成测试
6. P3 - 根据实际需求选择性实施

---

**文档版本**: 2025-12-27
**相关文档**:
- `docs/performance-comparison/`
- `README.md`
