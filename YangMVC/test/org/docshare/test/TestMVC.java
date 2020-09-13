package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.util.HttpHelper;


import junit.framework.TestCase;

public class TestMVC extends TestCase{
	public void testGet(){
		
		String url = "http://127.0.0.1:1985/json";
		String string = HttpHelper.get(url);
		Log.e("httphelper.get " , string);
		long start =System.currentTimeMillis();
		int C = 1000;
		for(int i=0;i<1000;i++){
			string = HttpHelper.get(url);
		}
		long end = System.currentTimeMillis();
		System.out.println((end-start)*1.0/C +"ms");
	}
	public void test2(){
		
	}
}
