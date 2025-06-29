package org.demo;

import com.yangmvc.core.Controller;
import com.yangmvc.model.Model;

/**
 * BookController 是一个示例控制器，用于处理与书籍相关的请求。
 */
public class BookController extends Controller {

    /**
     * 获取所有书籍信息。
     *
     * @return 包含书籍信息的 JSON 数据
     */
    public void index() {
        Model book = new Model("book");
        setAttr("books", book.select());
        renderJson();
    }

    /**
     * 根据 ID 获取单本书籍信息。
     *
     * @param id 书籍的唯一标识符
     * @return 包含书籍信息的 JSON 数据
     */
    public void show(int id) {
        Model book = new Model("book");
        setAttr("book", book.findById(id));
        renderJson();
    }
}
