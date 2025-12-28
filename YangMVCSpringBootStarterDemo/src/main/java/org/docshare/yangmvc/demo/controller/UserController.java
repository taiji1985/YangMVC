package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 演示 CRUD 操作和数据库集成
 *
 * @author YangMVC
 */
public class UserController extends Controller {

    /**
     * 用户列表页面
     * 访问路径: /user/list
     */
    public void list() {
        // 模拟用户数据
        List<User> users = new ArrayList<User>();
        users.add(new User(1, "张三", "zhangsan@example.com", 25, "北京"));
        users.add(new User(2, "李四", "lisi@example.com", 30, "上海"));
        users.add(new User(3, "王五", "wangwu@example.com", 28, "深圳"));
        users.add(new User(4, "赵六", "zhaoliu@example.com", 35, "杭州"));
        users.add(new User(5, "钱七", "qianqi@example.com", 27, "成都"));

        put("users", users);
        put("total", users.size());
        put("title", "用户管理");
        renderFreeMarker("/user/list.html");
    }

    /**
     * 添加用户页面
     * 访问路径: /user/add
     */
    public void add() {
        put("title", "添加用户");
        renderFreeMarker("/user/form.html");
    }

    /**
     * 编辑用户页面
     * 访问路径: /user/edit?id=1
     */
    public void edit() {
        Integer id = intParam("id");
        User user = getUserById(id);
        if (user != null) {
            put("user", user);
            put("title", "编辑用户");
            renderFreeMarker("/user/form.html");
        } else {
            put("error", "用户不存在");
            jsonError(404, "用户不存在");
        }
    }

    /**
     * 查看用户详情
     * 访问路径: /user/view?id=1
     */
    public void view() {
        Integer id = intParam("id");
        User user = getUserById(id);
        if (user != null) {
            put("user", user);
            put("title", "用户详情");
            renderFreeMarker("/user/view.html");
        } else {
            put("error", "用户不存在");
            jsonError(404, "用户不存在");
        }
    }

    /**
     * 保存用户（新增或更新）
     * 访问路径: /user/save (POST)
     */
    public Object save() {
        Integer id = intParam("id");
        String name = param("name");
        String email = param("email");
        Integer age = intParam("age");
        String city = param("city");

        // 简单验证
        if (name == null || name.trim().isEmpty()) {
            return error(400, "用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            return error(400, "邮箱不能为空");
        }

        Map<String, Object> result = new HashMap<String, Object>();
        if (id != null && id > 0) {
            // 更新用户
            result.put("message", "用户更新成功");
            result.put("id", id);
        } else {
            // 新增用户
            id = (int) (System.currentTimeMillis() % 10000); // 模拟 ID 生成
            result.put("message", "用户创建成功");
            result.put("id", id);
        }
        result.put("status", "success");

        return result;
    }

    /**
     * 删除用户
     * 访问路径: /user/delete?id=1
     */
    public Object delete() {
        Integer id = intParam("id");

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "success");
        result.put("message", "用户删除成功");
        result.put("id", id);

        return result;
    }

    /**
     * 用户 API - 获取用户列表（JSON）
     * 访问路径: /user/api/list
     */
    public Object apiList() {
        List<User> users = new ArrayList<User>();
        users.add(new User(1, "张三", "zhangsan@example.com", 25, "北京"));
        users.add(new User(2, "李四", "lisi@example.com", 30, "上海"));
        users.add(new User(3, "王五", "wangwu@example.com", 28, "深圳"));
        users.add(new User(4, "赵六", "zhaoliu@example.com", 35, "杭州"));
        users.add(new User(5, "钱七", "qianqi@example.com", 27, "成都"));

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "success");
        result.put("data", users);
        result.put("total", users.size());

        return result;
    }

    /**
     * 模拟根据 ID 获取用户
     */
    private User getUserById(Integer id) {
        if (id == null) return null;

        Map<Integer, User> userMap = new HashMap<Integer, User>();
        userMap.put(1, new User(1, "张三", "zhangsan@example.com", 25, "北京"));
        userMap.put(2, new User(2, "李四", "lisi@example.com", 30, "上海"));
        userMap.put(3, new User(3, "王五", "wangwu@example.com", 28, "深圳"));
        userMap.put(4, new User(4, "赵六", "zhaoliu@example.com", 35, "杭州"));
        userMap.put(5, new User(5, "钱七", "qianqi@example.com", 27, "成都"));

        return userMap.get(id);
    }

    /**
     * 返回错误信息
     */
    private Object error(int code, String message) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", "error");
        result.put("code", code);
        result.put("message", message);
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
        private String city;

        public User() {}

        public User(int id, String name, String email, int age, String city) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.age = age;
            this.city = city;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }
}
