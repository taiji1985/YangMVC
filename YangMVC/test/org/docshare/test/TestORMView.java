package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

import junit.framework.TestCase;

public class TestORMView extends TestCase {
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
	}
	public void testView(){
		DBTool tool = Model.tool("cbook"); //cbook is a view
		LasyList list = tool.all().limit(5);
		Log.i(JSON.toJSONString(list,true));
	}
}
