package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.Model;
import org.docshare.yangmvc.orm.LasyList;

import java.util.HashMap;
import java.util.Map;

/**
 * 调试控制器 - 用于查看数据库表结构和数据
 */
public class DebugController extends Controller {

    /**
     * 调试student表结构
     * 访问: /debug/student/structure
     */
    public Object studentStructure() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取第一条记录
            LasyList students = Model.tool("student").all();

            result.put("data", students);
            result.put("status", "success");

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 调试student表数据
     * 访问: /debug/student/data
     */
    public Object studentData() {
        Map<String, Object> result = new HashMap<>();

        try {
            LasyList students = Model.tool("student").all();
            result.put("总数", students.size());

            // 转换为ArrayList
            java.util.List<Map<String, Object>> dataList = new java.util.ArrayList<>();
            for (Model student : students) {
                Map<String, Object> dataMap = new HashMap<>();
                for (String key : student.keySet()) {
                    dataMap.put(key, student.get(key));
                }
                dataList.add(dataMap);
            }
            result.put("data", dataList);
            result.put("status", "success");

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 调试页面
     * 访问: /debug/
     */
    public void index() {
        put("title", "调试工具");
        renderFreeMarker("/debug/index.html");
    }
}
