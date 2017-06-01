package org.docshare.test;

import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import junit.framework.TestCase;


public class TestORM3 extends TestCase{
	DBTool tool;
	private LasyList list;
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
		tool.run("delete from book where id >909", null);
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
}
