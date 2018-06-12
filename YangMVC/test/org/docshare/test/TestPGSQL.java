package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

import junit.framework.TestCase;

public class TestPGSQL extends TestCase{
	public void setUp(){
		Config.dbhost="localhost";
		Config.dbport="5432";
		Config.dbusr="bbb";
		Config.dbpwd="123456";
		Config.dbname = "postgres";
		Config.dbtype = "postgres";
	}
	public void testSelect(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all();
		Log.i(JSON.toJSONString(list));
	}
}
