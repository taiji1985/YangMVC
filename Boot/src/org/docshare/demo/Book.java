package org.docshare.demo;

import org.docshare.mvc.IBean;
/**
 * �̳�IBean��Ͳ���д getter��setter�ˡ�
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