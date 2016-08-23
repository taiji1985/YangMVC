package org.docshare.test;

import java.util.List;

import junit.framework.TestCase;

import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

public class TestToArray  extends TestCase {
	DBTool tool;
	private LasyList list;
	@Override
	public void setUp(){
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		tool = Model.tool("book");
	}
	
	public void test(){
		List<Model> mlist = tool.all();
		for(Model m :mlist){
			System.out.println(m);
		}
	}
	
	public void testExtra(){
		Model m = tool.all().one();
		m.put("sss", "fff");
		System.out.println(JSON.toJSONString(m));
		
	}
	
}
