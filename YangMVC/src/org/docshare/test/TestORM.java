package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;


import junit.framework.TestCase;

public class TestORM extends TestCase{
	DBTool tool;
	private LasyList list;
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		tool = Model.tool("book");
	}
	
	public void testUpdate(){
		Log.e("test update");
		int r = tool.run("update book set author=? where id = ?", "haha",1);
		Log.i(r);
		
	}
	public void testDel(){
		Log.e("testDel");
		Model m = tool.get(4);
		tool.del(4);
		Model m2 = tool.get(4);
		String s = ( m2 ==null) ? "no exist": (String)m2.get("name");
		Log.i("m is delete ?" + s);
		
		tool.save(m);
		
	}
	public void testExtra(){
		Log.e("testExtra");
		Model m = tool.get(1);
		m.put("haha", "sfsf");
		Log.i(m);
		
	}
	public void testGet(){
		Log.e("testGet");
		Model m = tool.get(1);
		Log.i(m);
	}
	public void testAll(){
		Log.e("testAll");
		LasyList list = tool.all();
		Log.i("result1 "+list.size());
		Log.i(list.get(1));
	}
	public void testCustom(){
		Log.e("testCustom");
		LasyList list = tool.fromSQL("select * from book where id>30 limit 30");
		int sz = list.size();
		assertEquals(sz, 30);
		Log.i("result2 "+list.size());
		Log.i(list.get(1));
		
		
	}
	public void testCustomLimit(){
		Log.e("testCustomLimit");

		LasyList list = tool.fromSQL("select * from book where id>30 limit 30").limit(40);
		int sz = list.size();
		assertEquals(sz, 40);
		
		Log.i(list.get(1));
		
		list = tool.fromSQL("select * from book where id>30 limit 30").limit(40).limit(10,40);
		Log.i("result4 "+list.size());
		Log.i(list.get(2));
	}
	public void testCustomLimit2(){
		Log.e("testCustomLimit2");

		LasyList list = tool.fromSQL("select * from book where id>30 limit 30").limit(40).limit(10,40);
		int sz = list.size();
		assertEquals(sz, 40);
		Log.i("result4 "+sz);
		Log.i(list.get(2));
	}	
	public void testCustomAdd(){
		Log.e("testCustomAdd");
		LasyList list = tool.fromSQL("select * from book where id>30 limit 30").lt("id", 50);
		int sz = list.size();
		
		Log.i("result5 "+sz);
		Log.i(list.get(2));
	}
	
	public void testInsert(){
		Log.e("testInsert");
		Model m = tool.create();
		m.put("name", "ss");
		m.put("catalog_id", 1);
		tool.save(m);
		
		m = tool.all().eq("name", "ss").one();
		Log.i(m);
	}

	public void testOrder(){
		Log.e("testOrder");
		list = tool.all().orderby("id", false).limit(30);
		Log.i(list.get(0));
	}
	
	public void testEq(){
		Log.e("testEq");
		LasyList list = tool.all().eq("id", 12);
		Log.i(list);
		list = tool.all().eq("id", 1112);
		Log.i(list);
		list = tool.all().eq("id", null);
		Log.i(list);
	}

	public void testByExample(){
		Log.e("testByExample");
		Model m  = tool.create();
		m.put("id", 12);
		LasyList list = tool.all().byExample(m);
		Log.i(list.size());
		Log.i(list);
	}
	
	public void testCustomSQL(){
		Log.e("testCustomSQL");
		LasyList list = tool.fromSQL("select name,author from book where id>30 limit 30").lt("id", 50);
		int sz = list.size();
		
		Log.i("result5 "+sz);
		Log.i(list.get(2));
	}
	
}
