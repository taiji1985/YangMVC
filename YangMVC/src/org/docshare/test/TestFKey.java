package org.docshare.test;

import java.sql.SQLException;
import java.util.HashMap;

import junit.framework.TestCase;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

public class TestFKey extends TestCase{
	public void testListColumn(String[] args) throws SQLException {
		DBHelper dHelper = DBHelper.getIns();
		dHelper.conn();
		HashMap<String, ColumnDesc> r = dHelper.listColumn("book");
		Log.i(JSON.toJSONString(r));
		
	}
	public void testForeignVal(){
		Config.dbname="mvc_demo";
		Log.i("testForeignVal");
		Model m = Model.tool("book").get(1);
		Model m2=(Model) m.get("catalog");
		Log.i(JSON.toJSONString(m2));
		Model m3 = (Model)m.get("catalog_id__obj");
		Log.i(JSON.toJSONString(m2));
		
	}
	
	
}