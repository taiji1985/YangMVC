package org.docshare.test;

import org.docshare.mvc.Config;
import org.docshare.orm.ConfigCreator;

public class TestCreator {
	public void setUp(){
		Config.dbhost="localhost";
		Config.dbport="5432";
		Config.dbusr="bbb";
		Config.dbpwd="123456";
		Config.dbname = "postgres";
		Config.dbtype = "postgres";
		Config.dbschema = "public";
	}
	public void testPOJO(){
		try {
			ConfigCreator.main(null);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
