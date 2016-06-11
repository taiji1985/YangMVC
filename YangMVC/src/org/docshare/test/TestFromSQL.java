package org.docshare.test;

import javax.management.modelmbean.ModelMBean;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import junit.framework.TestCase;

public class TestFromSQL extends TestCase {
	private DBTool tool;

	@Override
	public void setUp(){
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		tool = Model.tool("book");
	}
	
	public void testSQL(){
		LasyList list = tool.fromSQL("select id,name from book").limit(10);
		for(Model m:list){
			Log.e(m);
		}
	}
	
	public void testSQL2(){
		LasyList list = tool.fromSQL("select book.*,dingcan.* from book,dingcan where book.id = dingcan.book_id").limit(10);
		
		for(Model m:list){
			Log.e(m);
		}list.printColumnDesc();
	}
	
}
