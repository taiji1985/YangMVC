package org.demo.db;

import org.docshare.orm.LasyList;
import java.util.List;
import org.docshare.orm.Model;

public class Catalog{
	public Integer id;	
	public String name;	
	public int update(){
		return Model.tool("catalog").save(this);
	}
	public int insert(){
		return Model.tool("catalog").save(this,true);
	}
	public static Catalog findByKey(Object id){
		return Model.tool("catalog").get(id).toObject(new Catalog());
	}
	public static Catalog findByColumn(String column,Object id){
		return Model.tool("catalog").get(column,id).toObject(new Catalog());
	}
	public static List<Catalog>  fromList(LasyList list){
		return list.toArrayList(Catalog.class);
	}
	public static int delByKey(Object key){
		return Model.tool("catalog").del(key);
	}
	public void remove(){
		Model.tool("catalog").del(id);
	}
}