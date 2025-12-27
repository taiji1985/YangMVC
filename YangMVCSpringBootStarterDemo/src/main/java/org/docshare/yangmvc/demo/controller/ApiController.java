package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;

import java.util.*;

/**
 * REST API示例控制器
 * <p>
 * 演示如何使用YangMVC创建RESTful API
 *
 * @author YangMVC
 */
public class ApiController extends Controller {

    /**
     * API测试接口 - 返回响应对象
     * 访问路径: /api/test
     */
    public Object test() {
        ApiResponse response = new ApiResponse();
        response.setStatus("success");
        response.setMessage("YangMVC Spring Boot Starter API");
        response.setTimestamp(new Date());
        response.setData(Arrays.asList(
            "Feature 1: 零配置启动",
            "Feature 2: 自动数据源集成",
            "Feature 3: 简洁的API",
            "Feature 4: 强大的ORM"
        ));
        return response;
    }

    /**
     * 获取统计信息 - 返回统计对象
     * 访问路径: /api/stats
     */
    public Object stats() {
        SystemStats stats = new SystemStats();
        stats.setServerTime(new Date());
        stats.setUptime(System.currentTimeMillis());
        stats.setJavaVersion(System.getProperty("java.version"));
        stats.setOsName(System.getProperty("os.name"));
        stats.setOsVersion(System.getProperty("os.version"));
        stats.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
        stats.setFreeMemory(Runtime.getRuntime().freeMemory() / 1024 / 1024);
        stats.setMaxMemory(Runtime.getRuntime().maxMemory() / 1024 / 1024);
        stats.setTotalMemory(Runtime.getRuntime().totalMemory() / 1024 / 1024);
        return stats;
    }

    /**
     * 表单提交示例 - 返回表单数据对象
     * 访问路径: /api/form (POST)
     */
    public Object form() {
        Map<String, String[]> params = request().getParameterMap();

        FormData formData = new FormData();
        formData.setStatus("success");
        formData.setMessage("表单数据接收成功");
        formData.setTimestamp(new Date());

        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                data.put(key, values[0]);
            } else {
                data.put(key, Arrays.asList(values));
            }
        }
        formData.setData(data);
        return formData;
    }

    /**
     * 获取单个用户信息
     * 访问路径: /api/user
     */
    public Object user() {
        return new ApiUser(
            1001,
            "API用户",
            "api@example.com",
            "active",
            new Date()
        );
    }

    /**
     * 获取用户列表
     * 访问路径: /api/users
     */
    public Object users() {
        return new ApiUser[]{
            new ApiUser(1001, "张三", "zhangsan@example.com", "active", new Date()),
            new ApiUser(1002, "李四", "lisi@example.com", "active", new Date()),
            new ApiUser(1003, "王五", "wangwu@example.com", "inactive", new Date()),
            new ApiUser(1004, "赵六", "zhaoliu@example.com", "active", new Date())
        };
    }

    /**
     * 分页数据示例
     * 访问路径: /api/page?page=1&size=10
     */
    public Object page() {
        int page = parseInt(param("page"), 1);
        int size = parseInt(param("size"), 10);

        PagedResult result = new PagedResult();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(100);

        // 生成模拟数据
        List<ApiUser> users = new ArrayList<>();
        for (int i = 0; i < size && i < 100; i++) {
            int index = (page - 1) * size + i;
            users.add(new ApiUser(
                1000 + index,
                "用户" + index,
                "user" + index + "@example.com",
                index % 2 == 0 ? "active" : "inactive",
                new Date()
            ));
        }
        result.setUsers(users.toArray(new ApiUser[0]));
        result.setHasMore(page * size < 100);

        return result;
    }

    /**
     * 辅助方法：将字符串转换为int
     */
    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 内部类 ====================

    /**
     * API响应封装类
     */
    public static class ApiResponse {
        private String status;
        private String message;
        private Date timestamp;
        private Object data;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    /**
     * 系统统计信息
     */
    public static class SystemStats {
        private Date serverTime;
        private long uptime;
        private String javaVersion;
        private String osName;
        private String osVersion;
        private int availableProcessors;
        private long freeMemory;
        private long maxMemory;
        private long totalMemory;

        public Date getServerTime() { return serverTime; }
        public void setServerTime(Date serverTime) { this.serverTime = serverTime; }

        public long getUptime() { return uptime; }
        public void setUptime(long uptime) { this.uptime = uptime; }

        public String getJavaVersion() { return javaVersion; }
        public void setJavaVersion(String javaVersion) { this.javaVersion = javaVersion; }

        public String getOsName() { return osName; }
        public void setOsName(String osName) { this.osName = osName; }

        public String getOsVersion() { return osVersion; }
        public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

        public int getAvailableProcessors() { return availableProcessors; }
        public void setAvailableProcessors(int availableProcessors) { this.availableProcessors = availableProcessors; }

        public long getFreeMemory() { return freeMemory; }
        public void setFreeMemory(long freeMemory) { this.freeMemory = freeMemory; }

        public long getMaxMemory() { return maxMemory; }
        public void setMaxMemory(long maxMemory) { this.maxMemory = maxMemory; }

        public long getTotalMemory() { return totalMemory; }
        public void setTotalMemory(long totalMemory) { this.totalMemory = totalMemory; }
    }

    /**
     * 表单数据封装类
     */
    public static class FormData {
        private String status;
        private String message;
        private Date timestamp;
        private Map<String, Object> data;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

        public Map<String, Object> getData() { return data; }
        public void setData(Map<String, Object> data) { this.data = data; }
    }

    /**
     * API用户实体
     */
    public static class ApiUser {
        private int id;
        private String name;
        private String email;
        private String status;
        private Date createTime;

        public ApiUser() {}

        public ApiUser(int id, String name, String email, String status, Date createTime) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.status = status;
            this.createTime = createTime;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    /**
     * 分页结果
     */
    public static class PagedResult {
        private int page;
        private int size;
        private int total;
        private ApiUser[] users;
        private boolean hasMore;

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }

        public ApiUser[] getUsers() { return users; }
        public void setUsers(ApiUser[] users) { this.users = users; }

        public boolean isHasMore() { return hasMore; }
        public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
    }
}
