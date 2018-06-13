package org.demo.pojo;

import org.docshare.orm.LasyList;
import java.util.List;
import org.docshare.orm.Model;

public class Catalog{
	public Integer id;	
	public String name;	
	public static final String TABLE_NAME = "catalog";
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
	public static Catalog findByKey(Object id){
		return Model.tool(TABLE_NAME).get(id).toObject(new Catalog());
	}
	/**
	*	根据其他列的值进行获取，如果获取结果有多个，则输出第一个。
	*/
	public static Catalog findByColumn(String column,Object id){
		return Model.tool(TABLE_NAME).get(column,id).toObject(new Catalog());
	}
	/**
	*	LasyList对象转对象数组
	*/
	public static List<Catalog>  fromList(LasyList list){
		return list.toArrayList(Catalog.class);
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
	
}