package org.docshare.test;

import java.util.List;

import javax.tools.Tool;

import org.docshare.mvc.Config;
import org.docshare.orm.Model;
import org.junit.Test;

import com.alibaba.fastjson.JSON;


public class TestModelCopy {
	public static class Book{
		public int id;
		public String filename;
		public String name;
		public String author;
	}
	public TestModelCopy(){
		Config.dbhost ="localhost";
		Config.dbpwd = "123456";
		Config.dbname="mvc_demo";
		
	}
	@Test
	public void test(){

		
		Model  m = Model.tool("book").all().one();
		System.out.println(m);
		Book b = m.toObject(new Book());
		System.out.println(JSON.toJSONString(b));
	}
	
	@Test
	public void testArray(){

		List<Book>  books = Model.tool("book").all().toArrayList(Book.class);
		System.out.println(JSON.toJSONString(books));
		
	}
}
