package org.docshare.test;

import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.docshare.log.Log;

import junit.framework.TestCase;

public class TestLast  extends TestCase{
	public void test(){
		LasyList list = Model.tool("book").all().limit(3);
		for(Model m:list){
			Log.e(m.get("name"));
		}
	}
}
