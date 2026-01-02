package org.docshare.yangmvc.demo.controller;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.DBTool;
import org.docshare.yangmvc.orm.LasyList;
import org.docshare.yangmvc.orm.Model;

import java.util.HashMap;
import java.util.Map;

public class DbDemoController extends Controller {

    public void index() {
        put("title", "YangMVC 数据库操作演示");
        renderFreeMarker("/db/index.html");
    }

    public void studentList() {
        LasyList students = Model.tool("student").all();
        put("students", students);
        put("total", students.size());
        put("title", "学生列表");
        renderFreeMarker("/db/student/list.html");
    }

    public Object studentGet() {
        Integer id = paramInt("id");
        if (id == null) {
            return error(400, "id 参数不能为空");
        }
        
        Model student = Model.tool("student").get(id);
        if (student == null) {
            return error(404, "学生不存在");
        }
        
        return success(modelToMap(student));
    }

    public void studentAdd() {
        put("title", "添加学生");
        renderFreeMarker("/db/student/form.html");
    }

    public Object studentSave() {
        Integer id = paramInt("id");
        String name = param("name");
        String heightStr = param("height");
        String weightStr = param("weight");

        if (name == null || name.trim().isEmpty()) {
            return error(400, "姓名不能为空");
        }

        Model student;
        if (id != null && id > 0) {
            student = Model.tool("student").get(id);
            if (student == null) {
                return error(404, "学生不存在");
            }
            student.put("name", name);
            if (heightStr != null && !heightStr.trim().isEmpty()) {
                student.put("height", Double.parseDouble(heightStr));
            }
            if (weightStr != null && !weightStr.trim().isEmpty()) {
                student.put("weight", Double.parseDouble(weightStr));
            }
            student.save();
            return success("更新成功", modelToMap(student));
        } else {
            student = Model.tool("student").create();
            student.put("name", name);
            if (heightStr != null && !heightStr.trim().isEmpty()) {
                student.put("height", Double.parseDouble(heightStr));
            }
            if (weightStr != null && !weightStr.trim().isEmpty()) {
                student.put("weight", Double.parseDouble(weightStr));
            }
            student.save();
            return success("添加成功", modelToMap(student));
        }
    }

    public void studentEdit() {
        Integer id = paramInt("id");
        if (id == null) {
            put("error", "id 参数不能为空");
            renderFreeMarker("/db/error.html");
            return;
        }

        Model student = Model.tool("student").get(id);
        if (student == null) {
            put("error", "学生不存在");
            renderFreeMarker("/db/error.html");
            return;
        }

        put("student", modelToMap(student));
        put("title", "编辑学生");
        renderFreeMarker("/db/student/form.html");
    }

    public Object studentDelete() {
        Integer id = paramInt("id");
        if (id == null) {
            return error(400, "id 参数不能为空");
        }

        DBTool studentTool = Model.tool("student");
        Model student = studentTool.get(id);
        if (student == null) {
            return error(404, "学生不存在");
        }

        studentTool.del(id);
        return success("删除成功");
    }

    public Object studentStats() {
        LasyList students = Model.tool("student").all();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", students.size());
        
        double totalHeight = 0;
        double totalWeight = 0;
        int countHeight = 0;
        int countWeight = 0;
        
        for (Model s : students) {
            Double height = s.getDouble("height");
            Double weight = s.getDouble("weight");
            if (height != null) {
                totalHeight += height;
                countHeight++;
            }
            if (weight != null) {
                totalWeight += weight;
                countWeight++;
            }
        }
        
        stats.put("avgHeight", countHeight > 0 ? totalHeight / countHeight : 0);
        stats.put("avgWeight", countWeight > 0 ? totalWeight / countWeight : 0);
        
        return success(stats);
    }

    public void courseList() {
        LasyList courses = Model.tool("course").all();
        put("courses", courses);
        put("title", "课程列表");
        renderFreeMarker("/db/course/list.html");
    }

    public Object courseApi() {
        LasyList courses = Model.tool("course").all();
        return success(courses.toArrayList());
    }

    public Object courseGet() {
        Integer id = paramInt("id");
        if (id == null) {
            return error(400, "id 参数不能为空");
        }
        
        Model course = Model.tool("course").get(id);
        if (course == null) {
            return error(404, "课程不存在");
        }
        
        return success(modelToMap(course));
    }

    public Object courseSearch() {
        String keyword = param("keyword");
        LasyList courses;
        
        if (keyword == null || keyword.trim().isEmpty()) {
            courses = Model.tool("course").all();
        } else {
            courses = Model.tool("course").all()
                .like("course_name", "%" + keyword + "%");
        }
        
        return success(courses.toArrayList());
    }

    private Object success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", data);
        return result;
    }

    private Object success(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    private Object error(int code, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("code", code);
        result.put("message", message);
        return result;
    }

    private Map<String, Object> modelToMap(Model model) {
        if (model == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (String key : model.keySet()) {
            map.put(key, model.get(key));
        }
        return map;
    }
}
