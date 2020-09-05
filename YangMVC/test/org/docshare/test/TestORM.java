package org.docshare.test;

import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;


import junit.framework.TestCase;

public class TestORM extends TestCase{
	DBTool tool;
	private LasyList list;
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3308";
		tool = Model.tool("book");
		assertNotNull(tool);
	}
	@Override
	protected void tearDown() throws Exception {
		tool.run("delete from book where id >909");
		super.tearDown();
	}
	public void testOne(){
		Model m = tool.all().eq("name", "本草纲目").one();
		Log.i("m = "+m);
	}
	public void testAddAll(){
		LasyList list = Model.tool("book").all().limit(1);
		LasyList list2 = Model.tool("book").all().limit(1);
		list.addAll(list2);
		System.out.println(JSON.toJSONString(list));
	}
	public void testOrder(){
		Log.e("testOrder");
		list = tool.all().orderby("id", false).limit(30);
		Log.i(list.get(0));
		assertNotNull(list.get(0));
	}
	public void testUpdate(){
		Log.e("test update");
		int r = tool.run("update book set author=? where id = ?", "haha",1);
		Log.i(r);
		
		Model m = tool.get(1);
		assertEquals(m.get("author"),"haha");		
	}
	
	public void testDel(){
		Log.e("testDel");
		
		Model m = tool.get(4);
		tool.del(4);
		Model m2 = tool.get(4);

		assertEquals(null,m2);
		String s = ( m2 ==null) ? "no exist": (String)m2.get("name");
		Log.i("m is delete ?" + s);
		
		if(m !=null){
			tool.save(m);
		}
		
	}
	public void testInsert1(){
		Log.e("testInsert");
		Model m = tool.create();
		m.put("id", 4);
		m.put("name", "haha");
		int r = tool.save(m,true);
		Log.i("insert return "+r);
	}
	public void testExtra(){
		Log.e("testExtra");
		Model m = tool.get(1);
		m.put("haha", "sfsf");
		Log.i(m);
		assertEquals("sfsf", m.get("haha"));
		Log.i("haha = "+ m.get("haha"));
		
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
		LasyList list = LasyList.fromRawSql("select * from book where id>30 limit 30");
		int sz = list.size();
		assertEquals(sz, 30);
		Log.i("result2 "+list.size());
		Log.i(list.get(1));
		
		
	}
//	public void testCustomLimit(){
//		Log.e("testCustomLimit");
//
//		LasyList list = tool.fromSQL("select * from book where id>30 limit 30");
//		int sz = list.size();
//		assertEquals(sz, 30);
//		
//		Log.i(list.get(1));
//		
//		list = tool.fromSQL("select * from book where id>30 limit 30");
//		Log.i("result4 "+list.size());
//		Log.i(list.get(2));
//	}
//	public void testCustomLimit2(){
//		Log.e("testCustomLimit2");
//
//		LasyList list = tool.fromSQL("select * from book where id>30 limit 30").limit(40).limit(10,40);
//		int sz = list.size();
//		assertEquals(sz, 40);
//		Log.i("result4 "+sz);
//		Log.i(list.get(2));
//	}	

	
	public void testInsert(){
		Log.e("testInsert");
		Model m = tool.create();
		m.put("name", "ss");
		m.put("catalog_id", 1);
		tool.save(m);
		
		m = tool.all().eq("name", "ss").one();
		Log.i(m);
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
	

	public void testPS(){
		Model m = tool.create();
		m.put("catalog_id","2");
		m.put("name", "wwww");
		tool.save(m);
	}
	public void testColumnFilter(){
		LasyList lasyList =  Model.tool("book").all().columnFilter("id,name").limit(2);
		String string = JSON.toJSONString(lasyList.toArrayList());
		System.out.println(string);
	}
	public void testQueue(){
		List<Model> a = Model.tool("book").all().gt("id", 12).lt("id", 33).eq("id", 14).orderby("name", false).limit(0,20).toArrayList();
		Log.i(a);
	}
}
