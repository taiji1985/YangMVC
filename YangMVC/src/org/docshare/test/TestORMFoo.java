package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;

import junit.framework.TestCase;

public class TestORMFoo  extends TestCase{
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	static int id ;
	public void testInsert(){
		DBTool tool = Model.tool("select");
		Model m  = tool.create().put("select", "haha")
		.put("update", "wuwu")
		.put("delete", "dd")
		.put("order", "oo")
		.put("limit", "ll")
		.put("group", "gg");
		int ret = m.save();
		assertEquals(ret, 1);
		id = m.getInt("id");
		assertTrue(id>0);
	}
	public void testUpdate(){
		Model.tool("select").get(id).put("update", "uuuuu").save();
		String s = Model.tool("select").get(id).getStr("update");
		assertEquals(s, "uuuuu");		
	}
	public void testCustom(){
		Model.tool("select").all().custom("`select` = 'haha'").one().dump();
	}
	public void testRemove(){
		Model.tool("select").del(id);
		assertEquals(Model.tool("select").get(id), null);
	}


}
