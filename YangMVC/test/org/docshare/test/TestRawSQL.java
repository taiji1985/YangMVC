package org.docshare.test;

import org.docshare.mvc.Config;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;
import org.junit.Test;

public class TestRawSQL {

	@Test
	public void test(){
		Config.dbhost ="localhost";
		Config.dbpwd = "123456";
		Config.dbname="mvc_demo";
		
		LasyList list = LasyList.fromRawSql("select name from book where id=3");
		for(Model m:list){
			System.out.println(m.toString());
		}
	}
}
