# 修复报告：URL链接错误

## 问题描述

在 `/db-demo/` 页面中，所有的链接都指向错误的URL：
- ❌ 错误：`/db/student/list`
- ✅ 正确：`/db-demo/studentList`

## 根本原因

URL映射规则理解错误。YangMVC的URL映射规则是：

```
{Controller名去掉Controller后缀转为小写-kebab-case}/{方法名}
```

**DbDemoController的映射**：
- Controller名：`DbDemoController`
- 去掉Controller：`DbDemo`
- 转为小写-kebab-case：`db-demo`
- 方法名直接映射：`studentList` → `/db-demo/studentList`

## 修复的文件

### 1. /view/db/index.html
**修复的链接**：
- `/db/student/list` → `/db-demo/studentList`
- `/db/student/add` → `/db-demo/studentAdd`
- `/db/course/list` → `/db-demo/courseList`
- `/db/student/get?id=1` → `/db-demo/studentGet?id=1`
- `/db/student/stats` → `/db-demo/studentStats`
- `/db/course/api` → `/db-demo/courseApi`
- `/db/course/search?keyword=java` → `/db-demo/courseSearch?keyword=java`

### 2. /view/db/student/list.html
**修复的链接**：
- `/db/index` → `/db-demo/`
- `/db/student/add` → `/db-demo/studentAdd`
- `/db/student/edit?id=...` → `/db-demo/studentEdit?id=...`
- `/db/student/delete?id=...` → `/db-demo/studentDelete?id=...`

### 3. /view/db/student/form.html
**修复的链接**：
- `/db/student/list` → `/db-demo/studentList`
- `/db/student/save` → `/db-demo/studentSave`

### 4. /view/db/course/list.html
**修复的链接**：
- `/db/index` → `/db-demo/`

## 完整的URL映射表

| Controller | 方法 | 正确URL | 错误URL |
|-----------|------|---------|--------|
| DbDemoController | index() | /db-demo/ | /db/index |
| DbDemoController | studentList() | /db-demo/studentList | /db/student/list |
| DbDemoController | studentAdd() | /db-demo/studentAdd | /db/student/add |
| DbDemoController | studentEdit() | /db-demo/studentEdit | /db/student/edit |
| DbDemoController | studentSave() | /db-demo/studentSave | /db/student/save |
| DbDemoController | studentDelete() | /db-demo/studentDelete | /db/student/delete |
| DbDemoController | studentGet() | /db-demo/studentGet | /db/student/get |
| DbDemoController | studentStats() | /db-demo/studentStats | /db/student/stats |
| DbDemoController | courseList() | /db-demo/courseList | /db/course/list |
| DbDemoController | courseApi() | /db-demo/courseApi | /db/course/api |
| DbDemoController | courseGet() | /db-demo/courseGet | /db/course/get |
| DbDemoController | courseSearch() | /db-demo/courseSearch | /db/course/search |

## YangMVC URL映射规则总结

### 基本规则
1. Controller名去掉"Controller"后缀
2. 转换为小写-kebab-case（大写字母前加连字符）
3. 方法名直接附加到路径后

### 示例

| Controller类 | URL前缀 |
|-------------|---------|
| IndexController | / |
| UserController | /user/ |
| BookController | /book/ |
| DbDemoController | /db-demo/ |
| APIUserController | /api-user/ |
| StudentInfoController | /student-info/ |

### 方法映射

```java
public class DbDemoController extends Controller {
    public void index()          // → /db-demo/
    public void studentList()    // → /db-demo/studentList
    public void studentAdd()      // → /db-demo/studentAdd
    public void studentSave()     // → /db-demo/studentSave
    public Object courseApi()     // → /db-demo/courseApi
}
```

## 验证步骤

### 1. 重启应用

```bash
mvn spring-boot:run
```

### 2. 测试链接

访问 `http://localhost:8080/db-demo/`，测试以下功能：

- ✅ 点击"学生列表" → 应跳转到 `/db-demo/studentList`
- ✅ 点击"添加学生" → 应跳转到 `/db-demo/studentAdd`
- ✅ 点击"课程列表" → 应跳转到 `/db-demo/courseList`
- ✅ 测试JSON API链接，确保返回正确的数据

### 3. 测试完整流程

1. 查看学生列表
2. 添加新学生
3. 编辑学生
4. 删除学生
5. 查看课程列表
6. 测试JSON API

## 注意事项

### 1. URL大小写
YangMVC的URL是**大小写敏感**的：
- ✅ `/db-demo/studentList` 正确
- ❌ `/db-DEMO/StudentList` 错误

### 2. 方法命名建议
为了避免URL过长和复杂，建议：
- 使用驼峰命名：`studentList`, `courseApi`
- 避免过度缩写：`stuList`（不推荐）→ `studentList`（推荐）

### 3. 模板中的链接
在FreeMarker模板中，总是使用正确的URL路径：
```html
<a href="/db-demo/studentList">学生列表</a>
<form action="/db-demo/studentSave" method="POST">
```

## 修复状态

- ✅ /view/db/index.html - 已修复
- ✅ /view/db/student/list.html - 已修复
- ✅ /view/db/student/form.html - 已修复
- ✅ /view/db/course/list.html - 已修复
- ✅ 编译成功

## 后续建议

1. **创建URL常量类** - 避免硬编码URL
2. **使用URL生成工具** - 在Controller中提供URL生成方法
3. **文档化URL规则** - 在项目文档中明确说明URL映射规则
4. **添加URL测试** - 在单元测试中验证URL映射正确性

---

**修复日期**: 2026-01-02
**修复版本**: 2025.07.06
**影响范围**: DbDemoController相关所有模板文件
**重要程度**: 🟡 中（影响用户导航和功能访问）
