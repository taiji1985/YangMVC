package org.demo;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.Model;
import org.docshare.yangmvc.orm.LasyList;
import java.util.Map;
import java.util.HashMap;

/**
 * BookController 是一个示例控制器，用于处理与书籍相关的请求。
 * 使用最新的YangMVC API进行优化，支持参数自动映射和返回值自动输出。
 */
public class BookController extends Controller {

    /**
     * 获取所有书籍信息，支持分页和搜索。
     * 请求示例: /book/index?page=1&size=10&keyword=Java
     * 
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10
     * @param keyword 搜索关键词，可选
     * @return 包含书籍信息的 JSON 数据
     */
    public LasyList index(Integer page, Integer size, String keyword) {
        // 设置默认值
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        // 构建查询
        LasyList books = Model.tool("book").all();
        
        // 添加搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            books = books.mlike("title,author,description", keyword.trim());
        }
        
        // 分页查询
        return books.page(page, size);
    }

    /**
     * 根据 ID 获取单本书籍信息。
     * 请求示例: /book/show?id=1
     * 
     * @param id 书籍的唯一标识符
     * @return 包含书籍信息的 JSON 数据或错误信息
     */
    public Object show(Integer id) {
        if (id == null || id <= 0) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "无效的书籍ID");
            error.put("code", 400);
            return error;
        }
        
        Model book = Model.tool("book").get(id);
        if (book == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "书籍不存在");
            error.put("code", 404);
            return error;
        }
        
        return book;
    }
    
    /**
     * 创建新书籍。
     * 请求示例: POST /book/create
     * 
     * @param title 书籍标题
     * @param author 作者
     * @param price 价格
     * @param description 描述，可选
     * @return 创建结果
     */
    public Map<String, Object> create(String title, String author, Double price, String description) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数验证
        if (title == null || title.trim().isEmpty()) {
            result.put("error", "书籍标题不能为空");
            result.put("code", 400);
            return result;
        }
        
        if (author == null || author.trim().isEmpty()) {
            result.put("error", "作者不能为空");
            result.put("code", 400);
            return result;
        }
        
        if (price == null || price < 0) {
            result.put("error", "价格必须大于等于0");
            result.put("code", 400);
            return result;
        }
        
        try {
            // 创建新书籍
            Model book = Model.tool("book")
                .create()
                .put("title", title.trim())
                .put("author", author.trim())
                .put("price", price)
                .put("create_time", System.currentTimeMillis());
            
            if (description != null && !description.trim().isEmpty()) {
                book.put("description", description.trim());
            }
            
            int affected = book.save();
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "书籍创建成功");
                result.put("book", book);
                result.put("code", 201);
            } else {
                result.put("error", "书籍创建失败");
                result.put("code", 500);
            }
        } catch (Exception e) {
            result.put("error", "创建书籍时发生错误: " + e.getMessage());
            result.put("code", 500);
        }
        
        return result;
    }
    
    /**
     * 更新书籍信息。
     * 请求示例: PUT /book/update?id=1&title=新标题&price=99.9
     * 
     * @param id 书籍ID
     * @param title 新标题，可选
     * @param author 新作者，可选
     * @param price 新价格，可选
     * @param description 新描述，可选
     * @return 更新结果
     */
    public Map<String, Object> update(Integer id, String title, String author, Double price, String description) {
        Map<String, Object> result = new HashMap<>();
        
        if (id == null || id <= 0) {
            result.put("error", "无效的书籍ID");
            result.put("code", 400);
            return result;
        }
        
        Model book = Model.tool("book").get(id);
        if (book == null) {
            result.put("error", "书籍不存在");
            result.put("code", 404);
            return result;
        }
        
        try {
            // 更新字段
            boolean hasUpdate = false;
            
            if (title != null && !title.trim().isEmpty()) {
                book.put("title", title.trim());
                hasUpdate = true;
            }
            
            if (author != null && !author.trim().isEmpty()) {
                book.put("author", author.trim());
                hasUpdate = true;
            }
            
            if (price != null && price >= 0) {
                book.put("price", price);
                hasUpdate = true;
            }
            
            if (description != null) {
                book.put("description", description.trim());
                hasUpdate = true;
            }
            
            if (!hasUpdate) {
                result.put("error", "没有提供有效的更新数据");
                result.put("code", 400);
                return result;
            }
            
            book.put("update_time", System.currentTimeMillis());
            int affected = book.save();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "书籍更新成功");
                result.put("book", book);
                result.put("code", 200);
            } else {
                result.put("error", "书籍更新失败");
                result.put("code", 500);
            }
        } catch (Exception e) {
            result.put("error", "更新书籍时发生错误: " + e.getMessage());
            result.put("code", 500);
        }
        
        return result;
    }
    
    /**
     * 删除书籍。
     * 请求示例: DELETE /book/delete?id=1
     * 
     * @param id 书籍ID
     * @return 删除结果
     */
    public Map<String, Object> delete(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        if (id == null || id <= 0) {
            result.put("error", "无效的书籍ID");
            result.put("code", 400);
            return result;
        }
        
        Model book = Model.tool("book").get(id);
        if (book == null) {
            result.put("error", "书籍不存在");
            result.put("code", 404);
            return result;
        }
        
        try {
            int affected = book.remove();
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "书籍删除成功");
                result.put("code", 200);
            } else {
                result.put("error", "书籍删除失败");
                result.put("code", 500);
            }
        } catch (Exception e) {
            result.put("error", "删除书籍时发生错误: " + e.getMessage());
            result.put("code", 500);
        }
        
        return result;
    }
    
    /**
     * 搜索书籍。
     * 请求示例: /book/search?keyword=Java&minPrice=50&maxPrice=200&page=1&size=10
     * 
     * @param keyword 搜索关键词
     * @param minPrice 最低价格，可选
     * @param maxPrice 最高价格，可选
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10
     * @return 搜索结果
     */
    public LasyList search(String keyword, Double minPrice, Double maxPrice, Integer page, Integer size) {
        // 设置默认值
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        
        LasyList books = Model.tool("book").all();
        
        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            books = books.mlike("title,author,description", keyword.trim());
        }
        
        // 价格范围过滤
        if (minPrice != null && minPrice >= 0) {
            books = books.gte("price", minPrice);
        }
        
        if (maxPrice != null && maxPrice >= 0) {
            books = books.lte("price", maxPrice);
        }
        
        // 按创建时间倒序排列
        books = books.custom("ORDER BY create_time DESC");
        
        return books.page(page, size);
    }
}
