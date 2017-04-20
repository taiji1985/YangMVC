package org.docshare.test;

import java.util.HashMap;

import junit.framework.TestCase;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

public class TestDBHelper  extends TestCase{
	DBTool tool;
	private LasyList list;
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		tool = Model.tool("test");
		assertNotNull(tool);
	}
	public void testColumn(){
		HashMap<String, ColumnDesc> r = DBHelper.getIns().listColumn("test");
		System.out.println(r);
		Model m =tool.create();
		m.put("c", "ss");
		tool.save(m);
	}
}