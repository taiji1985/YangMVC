package org.docshare.mvc;

public class Config {
	public static  String dbhost = "localhost";
	public static  String dbusr ="root";
	public static  String dbpwd = "123456";
	public static String dbname = "621m";
	public static  String tpl_base;
	public static  String ctr_base;
	
	public static String str() {
		return "Config [dbhost=" + dbhost +", dbname=" + dbname + ", dbusr=" + dbusr + ", dbpwd="
				+ dbpwd + ", tpl_base=" + tpl_base + ", ctr_base=" + ctr_base
				+ "]";
	}
	
	
}
