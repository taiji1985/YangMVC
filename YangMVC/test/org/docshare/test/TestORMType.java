package org.docshare.test;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.Model;

import junit.framework.TestCase;

public class TestORMType  extends TestCase{
	@Override
	public void setUp(){
		Log.e("setup");
		Config.dbname ="mvc_demo";
		Config.dbport = "3306";
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	public void testType(){
		Model m = Model.tool("testtype").all().one();
		
		System.out.println(m);
	}
}
