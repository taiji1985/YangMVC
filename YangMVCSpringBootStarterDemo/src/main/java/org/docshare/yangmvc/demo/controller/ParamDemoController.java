package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.Model;
import org.docshare.yangmvc.orm.LasyList;

import java.util.HashMap;
import java.util.Map;

/**
 * 方法参数自动映射演示控制器
 *
 * 演示YangMVC的方法参数自动映射功能
 * 需要在pom.xml中添加-parameters编译参数才能使用
 */
public class ParamDemoController extends Controller {

    /**
     * 演示1: 基本参数自动映射
     * 访问: /param/add?a=10&b=20
     * 返回: {"result": 30}
     */
    public Integer add(Integer a, Integer b) {
        // 参数a和b会自动从请求参数中注入
        // 如果参数不存在，则为null
        int x = (a != null) ? a : 0;
        int y = (b != null) ? b : 0;
        return x + y;
    }

    /**
     * 演示2: 字符串参数自动映射
     * 访问: /param/greeting?name=张三
     * 返回: "你好，张三！"
     */
    public String greeting(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "你好，陌生人！";
        }
        return "你好，" + name + "！";
    }

    /**
     * 演示3: 多种类型参数
     * 访问: /param/multi?username=admin&age=25&price=99.9&active=true
     * 返回: JSON对象
     */
    public Object multi(String username, Integer age, Double price, Boolean active) {
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("age", age);
        result.put("price", price);
        result.put("active", active);
        result.put("message", "参数自动映射成功");
        return result;
    }

    /**
     * 演示4: 分页查询
     * 访问: /param/users?page=1&pageSize=10
     * 返回: 用户列表
     */
    public Object users(Integer page, Integer pageSize) {
        // 设置默认值
        int p = (page != null && page > 0) ? page : 1;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 10;

        LasyList users = Model.tool("student").all()
                .orderby("id", false)  // false表示降序
                .limit((p - 1) * size, size);  // limit(start, len)

        Map<String, Object> result = new HashMap<>();
        result.put("page", p);
        result.put("pageSize", size);
        result.put("total", users.size());
        result.put("data", users.toArrayList());
        return result;
    }

    /**
     * 演示5: 模糊搜索
     * 访问: /param/search?keyword=张
     * 返回: 搜索结果
     */
    public Object search(String keyword) {
        LasyList students = Model.tool("student").all();

        if (keyword != null && !keyword.trim().isEmpty()) {
            students = students.like("name", "%" + keyword + "%");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("keyword", keyword);
        result.put("count", students.size());
        result.put("data", students.toArrayList());
        return result;
    }

    /**
     * 演示6: 登录（实际应用场景）
     * 访问: /param/login?username=admin&password=123456
     * 返回: 登录结果
     */
    public Object login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名不能为空");
            return result;
        }

        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "密码不能为空");
            return result;
        }

        // 模拟登录验证
        // 实际应用中应该查询数据库验证
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("username", username);
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }

    /**
     * 演示7: 添加用户（实际应用场景）
     * POST请求: /param/saveUser
     * Content-Type: application/json
     * Body: {"name": "李四", "height": 175.5, "weight": 65.0}
     */
    public Object saveUser(String name, Double height, Double weight) {
        Map<String, Object> result = new HashMap<>();

        // 参数验证
        if (name == null || name.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "姓名不能为空");
            return result;
        }

        // 保存到数据库
        Model student = Model.tool("student").create();
        student.put("name", name);
        if (height != null) {
            student.put("height", height);
        }
        if (weight != null) {
            student.put("weight", weight);
        }
        student.save();

        result.put("success", true);
        result.put("message", "保存成功");
        result.put("data", student);
        return result;
    }

    /**
     * 演示8: 混合使用（自动映射 + 手动获取）
     * 访问: /param/mixed?name=王五&id=1
     */
    public Object mixed(String name) {
        // 自动映射的参数
        Map<String, Object> result = new HashMap<>();
        result.put("autoMappedName", name);

        // 手动获取的参数
        Integer id = paramInt("id");
        result.put("manualId", id);

        // 结合使用
        if (id != null) {
            Model student = Model.tool("student").get(id);
            result.put("student", student);
        }

        return result;
    }

    /**
     * 演示页面：展示所有示例链接
     * 访问: /param/
     */
    public void index() {
        put("title", "YangMVC 方法参数自动映射演示");
        renderFreeMarker("/param/index.html");
    }
}
