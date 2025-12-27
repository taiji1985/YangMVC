package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;

/**
 * Hello World示例控制器
 * <p>
 * 演示基本的YangMVC Controller用法
 *
 * @author YangMVC
 */
public class HelloController extends Controller {

    /**
     * 默认首页方法
     * 访问路径: /hello/index 或 /hello
     */
    public void index() {
        put("title", "YangMVC Spring Boot Starter Demo");
        put("message", "欢迎使用YangMVC Spring Boot Starter!");
        put("features", new String[]{
            "零配置启动 - 只需添加依赖即可使用",
            "自动数据源集成 - 自动使用Spring Boot的DataSource",
            "简洁的API - 轻松开发Web应用",
            "强大的ORM功能 - 内置轻量级ORM框架"
        });
        render("/hello/index.html");
    }

    /**
     * JSON数据展示页面
     * 访问路径: /hello/jsonData
     */
    public void jsonData() {
        put("status", "success");
        put("message", "这是一个数据展示示例");
        put("data", new String[]{
            "item1", "item2", "item3", "item4", "item5"
        });
        put("timestamp", System.currentTimeMillis());
        render("/hello/json.html");
    }

    /**
     * 参数接收示例
     * 访问路径: /hello/echo?name=YourName
     */
    public void echo() {
        String name = param("name");
        if (name == null || name.isEmpty()) {
            name = "World";
        }
        put("name", name);
        put("greeting", "Hello, " + name + "!");
        put("timestamp", System.currentTimeMillis());
        render("/hello/echo.html");
    }

    /**
     * 重定向示例
     * 访问路径: /hello/redirect
     */
    public void redirect() {
        jump("/hello/index");
    }

    /**
     * Session使用示例
     * 访问路径: /hello/sessionDemo
     */
    public void sessionDemo() {
        // 获取访问次数
        Integer count = (Integer) sess("visitCount");
        if (count == null) {
            count = 0;
        }
        count++;

        // 保存到Session
        sess("visitCount", count);
        sess("lastVisit", System.currentTimeMillis());

        put("visitCount", count);
        put("message", "这是您第 " + count + " 次访问");
        put("lastVisitTime", new java.util.Date());

        render("/hello/session.html");
    }

    /**
     * 关于页面
     * 访问路径: /hello/about
     */
    public void about() {
        put("title", "关于 YangMVC");
        put("description", "YangMVC 是一个轻量级的 Java MVC 框架");
        put("version", "2025.07.06");
        render("/hello/about.html");
    }

    /**
     * 文本输出示例
     * 访问路径: /hello/text
     */
    public void text() {
        response().setContentType("text/plain;charset=utf-8");
        output("这是纯文本输出示例\n");
        output("当前时间: " + new java.util.Date() + "\n");
        output("YangMVC Spring Boot Starter 运行正常！");
    }

    /**
     * 返回单个对象示例
     * 访问路径: /hello/user
     */
    public Object user() {
        return new User(1, "张三", "zhangsan@example.com", 25);
    }

    /**
     * 返回对象列表示例
     * 访问路径: /hello/users
     */
    public Object users() {
        return new User[]{
            new User(1, "张三", "zhangsan@example.com", 25),
            new User(2, "李四", "lisi@example.com", 30),
            new User(3, "王五", "wangwu@example.com", 28)
        };
    }

    /**
     * 返回Map对象示例
     * 访问路径: /hello/map
     */
    public Object map() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "success");
        result.put("code", 200);
        result.put("data", new User(100, "测试用户", "test@test.com", 20));
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 用户实体类
     */
    public static class User {
        private int id;
        private String name;
        private String email;
        private int age;

        public User() {}

        public User(int id, String name, String email, int age) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }
}
