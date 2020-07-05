package org.docshare.test;


import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;

import junit.framework.TestCase;

public class TestORM2 extends TestCase{
	private DBTool tool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		tool = Model.tool("book");
	}

	public void testUpdate(){
		Model m = tool.get(2);
		if(m == null)return;
		m.put("name", "haha");
		int ret = tool.save(m);
		System.out.println("update ret = "+ret);
		System.out.println("update id = "+m.get("id"));
	}
	public void testInsert(){
		Model m = tool.create();
		m.put("name", "haha");
		int ret = tool.save(m);
		System.out.println("ret = ------------"+ret);
		System.out.println(m.get("id"));
	}
	public void testClear() {
		tool.run("delete from book where id >909");
	}
}
