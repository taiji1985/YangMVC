package org.docshare.test;

import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

import junit.framework.TestCase;


public class TestORM3 extends TestCase{
	DBTool tool;
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		tool = Model.tool("book");
		assertNotNull(tool);
	}
	@Override
	protected void tearDown() throws Exception {
		tool.run("delete from book where id >909");
		super.tearDown();
	}
	
	public void testObject(){
		DBTool tool = Model.tool("catalog");
		List<Catalog> cl = tool.all().toArrayList(Catalog.class);
		for(Catalog c : cl){
			System.out.println("c.id " +c.id);
			System.out.println("c.name " +c.name);
			c.name = c.name+"_haha";
			tool.save(c);
		}
		
	}
	public void testObjectInsert(){
		DBTool tool = Model.tool("catalog");
		Catalog  c = new Catalog();
		c.name = "haha";
		tool.save(c);
	}
	public void testLike(){
		String s = JSON.toJSONString(Model.tool("book").all().like("name", "本草").limit(10));
		System.out.println(s);
	}
	public void testMLike(){
		String s = JSON.toJSONString(Model.tool("book").all().mlike("name,author", "李").limit(10));
		System.out.println(s);
	}
}
