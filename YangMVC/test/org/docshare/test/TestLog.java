package org.docshare.test;

import org.docshare.log.Log;

import junit.framework.TestCase;

public class TestLog extends TestCase {
	public void testLog(){
		String c = Log.getCaller();
		System.out.println(c);
	}
	public void testI(){
		Log.v("hello");
		Log.d("hello");
		Log.w("hello");
		Log.i("hello");
		Log.e("hello");
		Log.showClass = false;
		Log.v("hello");
		Log.d("hello");
		Log.w("hello");
		Log.i("hello");
		Log.e("hello");
		
	}
}
