package org.docshare.test;

import org.docshare.orm.LasyList;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class TestRawSQL {

	@Test
	public void test(){
		new ConfigAll();
		
		LasyList list = LasyList.fromRawSql("select name as nn from book where id=3");
		System.out.println(JSON.toJSONString(list,true));
	}
}
