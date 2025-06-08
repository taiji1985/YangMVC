package org.demo;

import java.util.ArrayList;
import java.util.List;

import org.docshare.mvc.Controller;
import org.docshare.yangmvc.spring.util.SpringContextUtil;
import org.docshare.yangmvc.spring.service.BookService;

public class BookController extends Controller {
    public static class Book{
        public String name;
        public int id;
    }
    
    /**
     * 原始的list方法（保留作为对比）
     */
    public List<Book> list(){
        List<Book> list = new ArrayList<Book>();
        for(int i=0;i<10;i++){
            Book b = new Book();
            b.name = "bookA"+i;
            b.id = i;
            list.add(b);
        }
        return list;
    }
    
    /**
     * 使用Spring托管服务的新方法
     * 演示如何在非托管对象中获取Spring容器中的托管对象
     */
    public List<BookService.Book> listFromService() {
        // 使用SpringContextUtil获取Spring容器中的BookService
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.getAllBooks();
    }
    
    /**
     * 根据ID获取书籍
     */
    public BookService.Book getBookById(int id) {
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.getBookById(id);
    }
    
    /**
     * 搜索书籍
     */
    public List<BookService.Book> searchBooks(String name) {
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        return bookService.searchBooksByName(name);
    }
    
    /**
     * 获取书籍统计信息
     */
    public java.util.Map<String, Object> getBookStats() {
        BookService bookService = SpringContextUtil.getBean(BookService.class);
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", bookService.getBookCount());
        stats.put("totalValue", bookService.getTotalValue());
        stats.put("averagePrice", bookService.getTotalValue() / bookService.getBookCount());
        return stats;
    }
}
