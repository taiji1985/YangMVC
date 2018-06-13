package org.demo.pojo;

import org.docshare.orm.LasyList;
import java.util.List;
import org.docshare.orm.Model;

public class Book{
	public String id;	
	public String name;	
	public Integer catalog_id;	
	public static final String TABLE_NAME = "book";
	/**
	*	更新
	*/
	public int update(){
		return Model.tool(TABLE_NAME).save(this);
	}
	/**
	*	插入
	*/
	public int insert(){
		return Model.tool(TABLE_NAME).save(this,true);
	}
	
	/**
	*	根据主键获取
	*/
	public static Book findByKey(Object id){
		return Model.tool(TABLE_NAME).get(id).toObject(new Book());
	}
	/**
	*	根据其他列的值进行获取，如果获取结果有多个，则输出第一个。
	*/
	public static Book findByColumn(String column,Object id){
		return Model.tool(TABLE_NAME).get(column,id).toObject(new Book());
	}
	/**
	*	LasyList对象转对象数组
	*/
	public static List<Book>  fromList(LasyList list){
		return list.toArrayList(Book.class);
	}
	/**
	*	根据主键进行删除
	*/
	public static int delByKey(Object key){
		return Model.tool(TABLE_NAME).del(key);
	}
	/**
	*	删除当前对象
	*/
	public void remove(){
		Model.tool(TABLE_NAME).del(id);
	}
	
	/**
	 * 外键查询
	 * @return
	 */
	public Catalog getCatalog(){
		return Catalog.findByKey(catalog_id);
	}
}