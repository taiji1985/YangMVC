# YangMVCBoot

一个基于YangMVC框架的快速启动工具，类似于Spring Boot的设计理念，让您能够快速构建和部署Web应用程序。

## 项目简介

YangMVCBoot是YangMVC框架的Boot版本，提供了开箱即用的Web服务器功能。通过简单的`ServerMain.main(args)`调用即可启动一个完整的Web服务器，支持MVC控制器、模板渲染、WebSocket、Session管理等功能。

## 主要特性

- 🚀 **快速启动**: 一行代码启动Web服务器
- 🎯 **零配置**: 采用约定优于配置的设计理念
- 🌐 **内置Jetty**: 集成Jetty服务器，无需外部容器
- 📡 **WebSocket支持**: 内置WebSocket功能
- 🗄️ **Session管理**: 支持内存和Redis两种Session存储方式
- 🎨 **模板引擎**: 支持FreeMarker模板渲染
- 📊 **JSON输出**: 便捷的JSON API开发
- 🔧 **热重载**: 支持开发时的代码热重载

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>org.docshare</groupId>
    <artifactId>yangmvc-boot</artifactId>
    <version>2025.06.20</version>
</dependency>
```

### 最简单的启动方式

```java
import org.docshare.yangmvc.boot.ServerMain;

public class Application {
    public static void main(String[] args) {
        ServerMain.main(args);
    }
}
```

### 创建控制器

```java
package cn.org.docshare.demo;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.util.MapHelper;

public class IndexController extends Controller {

    // 访问 http://localhost:1985/
    public void index() {
        renderFreeMarker("/index.html");
    }

    // 访问 http://localhost:1985/json
    public void json() {
        outputJSON(MapHelper.toMap("message", "Hello YangMVC!", "timestamp", System.currentTimeMillis()));
    }

    // 访问 http://localhost:1985/hello
    public void hello() {
        output("Hello World!");
    }
}
```

## 项目结构

```
YangMVCBoot/
├── src/main/java/
│   ├── org/docshare/boot/          # Boot核心类
│   │   ├── ServerMain.java         # 服务器启动主类
│   │   ├── YangHandle.java         # 请求处理器
│   │   ├── MyWebSocketHandler.java # WebSocket处理器
│   │   └── ...
│   ├── cn/org/docshare/demo/       # 示例代码
│   │   ├── IndexController.java    # 主控制器
│   │   ├── BookController.java     # 书籍管理示例
│   │   ├── Init.java              # 初始化类
│   │   └── ...
│   └── web.properties             # 配置文件
├── view/                          # 模板文件目录
│   ├── index.html                # 首页模板
│   ├── ws.html                   # WebSocket测试页面
│   └── ...
└── pom.xml
```

## 配置说明

### web.properties 配置文件

在`src/main/java/web.properties`中配置基本信息：

```properties
# 控制器包路径
controller=cn.org.docshare.demo

# 模板文件路径
template=/view

# 数据库配置
dbhost=localhost
dbusr=root
dbpwd=root
dbname=mvc_demo
dbport=3306

# 开发配置
reloadable=true
groovy=groovy
reload_config=true
```

### 启动参数

```bash
# 默认端口1985启动
java -jar yangmvc-boot.jar

# 指定端口启动
java -jar yangmvc-boot.jar 8080

# 禁用WebSocket功能
java -jar yangmvc-boot.jar 8080 nowebsocket
```

## 功能特性详解

### 1. MVC控制器

- 控制器类需继承`Controller`基类
- 支持参数自动绑定：`param("name")`, `paramInt("id")`
- 支持多种输出方式：`output()`, `outputJSON()`, `renderFreeMarker()`

### 2. Session管理

```java
// 设置Session
sess("key", "value");

// 获取Session
String value = sess("key");

// 删除Session
removeSession("key");
```

### 3. WebSocket支持

```java
// 在Init类中注册WebSocket监听器
public class Init {
    public Init() {
        MyWebSocketHandler.register(MyWebSocketListener.class);
    }
}
```

### 4. Redis Session（可选）

支持使用Redis作为Session存储，适用于集群部署：

```properties
# 在web.properties中配置Redis
redis_host=localhost
redis_port=6379
```

## 示例功能

启动服务器后，可以访问以下示例页面：

- `http://localhost:1985/` - 主页
- `http://localhost:1985/json` - JSON输出示例
- `http://localhost:1985/fm` - FreeMarker模板示例
- `http://localhost:1985/setsess` - Session设置示例
- `http://localhost:1985/getsess` - Session获取示例
- `http://localhost:1985/demo_ws` - WebSocket测试页面

## 技术栈

- **Web服务器**: Eclipse Jetty 9.4.56
- **模板引擎**: FreeMarker
- **WebSocket**: Jetty WebSocket
- **Session存储**: 内存 / Redis
- **构建工具**: Maven
- **JDK版本**: 1.7+

## 开发建议

1. **项目结构**: 建议按功能模块组织控制器类
2. **配置管理**: 使用`web.properties`集中管理配置
3. **模板文件**: 统一放在`view`目录下
4. **静态资源**: 可以直接放在项目根目录
5. **热重载**: 开发时启用`reloadable=true`提高效率

## 许可证

MIT License

## 相关链接

- [YangMVC主项目](https://gitee.com/yangtf/YangMVC)
- [官方文档](https://gitee.com/yangtf/YangMVC)
- [问题反馈](https://gitee.com/yangtf/YangMVC/issues)

## 作者

- **yangtongfeng** - [ytf_003@126.com](mailto:ytf_003@126.com)
- **项目主页** - [https://gitee.com/yangtf/](https://gitee.com/yangtf/)

---

*YangMVCBoot - 让Web开发更简单！*