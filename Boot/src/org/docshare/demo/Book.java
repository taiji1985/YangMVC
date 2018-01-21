package org.docshare.demo;

import org.docshare.mvc.IBean;
/**
 * 继承IBean后就不用写 getter和setter了。
 * @author Administrator
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
}