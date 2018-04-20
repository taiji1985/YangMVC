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
		Config.dbname="dc2";
		
		LasyList list = LasyList.fromRawSql("select name from crawcfg");
		for(Model m:list){
			System.out.println(m.toString());
		}
	}
}
