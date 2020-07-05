package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import junit.framework.TestCase;

public class TestFromSQL extends TestCase {
	@Override
	public void setUp(){
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
		Model.tool("book");
	}
	
	public void testSQL(){
		LasyList list = LasyList.fromRawSql("select id,name from book");
		for(Model m:list){
			Log.e(m);
		}
	}
	
	public void testSQL2(){
		LasyList list = LasyList.fromRawSql("select book.*,catalog.`name` as cname from book,catalog where book.catalog_id = catalog.id ");
		
		for(Model m:list){
			Log.e(m);
		}list.printColumnDesc();
	}
	
}
