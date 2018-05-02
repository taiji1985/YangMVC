package org.docshare.test;

import org.docshare.mvc.Config;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestRawSQL {

	@Test
	public void test(){
		Config.dbhost ="localhost";
		Config.dbpwd = "123456";
		Config.dbname="mvc_demo";
		Config.dbport = "3308";
		
		LasyList list = LasyList.fromRawSql("select name as nn from book where id=3");
		System.out.println(JSON.toJSONString(list,true));
	}
}
