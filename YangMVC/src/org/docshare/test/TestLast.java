package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.orm.DBHelper;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;


import junit.framework.TestCase;

public class TestLast  extends TestCase{
	public void test(){
		for(int i=0;i<1000;i++){
			System.out.println(i);
			aa();
		}
		System.gc();
	}
	
	public void aa(){
		LasyList list = Model.tool("book").all().limit(3);
		for(Model m:list){
			Log.e(m.get("name"));
		}
		list= null;
	}
	
	public void testConnectionClose(){
		for(int i=0;i<10000;i++){
			LasyList list = Model.tool("book").all().limit(3);			
		}
		int s  = DBHelper.getIns().getVal("show status like '%Threads_connected%'", "Value");
		Log.i(s+"");
	}
	
	
}
