package org.demo.db;

import org.docshare.orm.LasyList;
import java.util.List;
import org.docshare.orm.Model;

public class Book{
	public Integer id;	
	public String author;	
	public String chaodai;	
	public String name;	
	public String about;	
	public String file_name;	
	public String type;	
	public String tm_year;	
	public Integer catalog_id;	
	public int update(){
		return Model.tool("book").save(this);
	}
	public int insert(){
		return Model.tool("book").save(this,true);
	}
	public static Book findByKey(Object id){
		return Model.tool("book").get(id).toObject(new Book());
	}
	public static Book findByColumn(String column,Object id){
		return Model.tool("book").get(column,id).toObject(new Book());
	}
	public static List<Book>  fromList(LasyList list){
		return list.toArrayList(Book.class);
	}
	public static int delByKey(Object key){
		return Model.tool("book").del(key);
	}
	public void remove(){
		Model.tool("book").del(id);
	}
}