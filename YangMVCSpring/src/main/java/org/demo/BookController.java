package org.demo;

import java.util.ArrayList;
import java.util.List;

import org.docshare.mvc.Controller;

public class BookController extends Controller {
    public static class Book{
        public String name;
        public int id;
    }
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
}
