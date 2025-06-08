package org.docshare.yangmvc.spring.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 书籍服务类
 * 这是一个Spring托管的服务类，用于演示如何在非托管对象中使用
 * 
 * @author YangMVC
 */
@Service
public class BookService {
    
    /**
     * 书籍实体类
     */
    public static class Book {
        public String name;
        public int id;
        public String author;
        public double price;
        
        public Book() {}
        
        public Book(int id, String name, String author, double price) {
            this.id = id;
            this.name = name;
            this.author = author;
            this.price = price;
        }
    }
    
    /**
     * 获取所有书籍列表
     * @return 书籍列表
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "Spring实战", "张三", 89.0));
        books.add(new Book(2, "Java编程思想", "李四", 108.0));
        books.add(new Book(3, "设计模式", "王五", 76.5));
        books.add(new Book(4, "算法导论", "赵六", 128.0));
        books.add(new Book(5, "数据库原理", "钱七", 95.5));
        return books;
    }
    
    /**
     * 根据ID获取书籍
     * @param id 书籍ID
     * @return 书籍对象，如果不存在返回null
     */
    public Book getBookById(int id) {
        List<Book> books = getAllBooks();
        return books.stream()
                .filter(book -> book.id == id)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据名称搜索书籍
     * @param name 书籍名称（支持模糊搜索）
     * @return 匹配的书籍列表
     */
    public List<Book> searchBooksByName(String name) {
        List<Book> books = getAllBooks();
        List<Book> result = new ArrayList<>();
        for (Book book : books) {
            if (book.name.contains(name)) {
                result.add(book);
            }
        }
        return result;
    }
    
    /**
     * 获取书籍总数
     * @return 书籍总数
     */
    public int getBookCount() {
        return getAllBooks().size();
    }
    
    /**
     * 计算所有书籍的总价值
     * @return 总价值
     */
    public double getTotalValue() {
        return getAllBooks().stream()
                .mapToDouble(book -> book.price)
                .sum();
    }
}