package org.docshare.test;

import org.docshare.mvc.Config;

public class ConfigAll {
	static{
		Config.dbhost ="localhost";
		Config.dbpwd = "123456";
		Config.dbname="mvc_demo";
		Config.dbport = "3306";
	}
}
