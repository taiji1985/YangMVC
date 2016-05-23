package org.docshare.test;

import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.docshare.log.Log;

import junit.framework.TestCase;

public class TestLast  extends TestCase{
	public void test(){
		for(int i=0;i<1000;i++){
			System.out.println(i);
			aa();
		}
		System.gc();
	}
	
	void aa(){
		LasyList list = Model.tool("book").all().limit(3);
		for(Model m:list){
			Log.e(m.get("name"));
		}
		list= null;
	}
}
