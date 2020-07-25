package cn.org.docshare.demo;

import org.docshare.mvc.IBean;
/**
 * @author Tongfeng yang
 *
 */
public class Book implements IBean{
	public String name;
	public double price;
//	public String getName(){return name;}
//	public void setName(String name){
//		this.name = name;
//	}
	public Book next;
	public static Book createSimple(){
		Book b = new Book();
		b.name = "Hello Master Yang";
		b.price = 1000;
		b.next = null;
		return b;
	}
}