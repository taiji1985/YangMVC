package org.demo;


import org.demo.db.Book;
import org.docshare.mvc.Controller;

public class PojoController extends Controller{
	public void index(){
		Book book = Book.findByKey(1);
		outputJSON(book);
		
	}
	public void edit(){
		Book book = Book.findByKey(1);
		book.name = "haha";
		book.update();
		outputJSON(book);
	}
	public void del(){
		int r = Book.delByKey(2);
		outputJSON(r);
	}
	public String dd(){
		return "this is dd";
	}
}
