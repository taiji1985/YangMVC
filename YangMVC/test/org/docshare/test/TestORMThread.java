package org.docshare.test;

import java.util.ArrayList;


import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBHelper;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import junit.framework.TestCase;

public class TestORMThread extends TestCase {
	public TestORMThread() {
		Config.dbname ="mvc_demo";
	}
	public void testConnectionClose() throws InterruptedException{
		int s  = DBHelper.getIns().getVal("show status like '%Threads_connected%'", "Value");
		Log.i(s+"");
		Model.tool("book").all().limit(3);
		
		ArrayList<Thread> tlist = new ArrayList<Thread>();
		for(int i=0;i<100;i++){
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					@SuppressWarnings("unused")
					LasyList list = Model.tool("book").all().limit(3);	
				}
			})	;
			tlist.add(t);
			t.start();
		}
		
		for(int i=0;i<tlist.size();i++){
			try {
				tlist.get(i).join();
			} catch (InterruptedException e) {
				Log.e(e);
			}
		}

		System.gc();
		Thread.sleep(1000);
		
		s  = DBHelper.getIns().getVal("show status like '%Threads_connected%'", "Value");
		Log.i(s+"");
	}
}
