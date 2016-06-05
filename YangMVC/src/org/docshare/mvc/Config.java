package org.docshare.mvc;

public class Config {
	/**
	 * 数据库主机名或IP地址
	 */
	public static  String dbhost = "localhost";
	/**
	 * 数据库用户名
	 */
	public static  String dbusr ="root";
	/**
	 * 数据库密码
	 */
	public static  String dbpwd = "123456";
	/**
	 * 数据库名称
	 */
	public static String dbname = "621m";
	
	/**
	 * 
	 */
	public static String dbport = "3306";
	
	
	
	/**
	 * 模板相对于WebRoot的目录路径
	 */
	public static  String tpl_base;
	/**
	 * 控制器根包名
	 */
	public static  String ctr_base;
	
	public static String str() {
		return "Config [dbhost=" + dbhost +", dbname=" + dbname + ", dbusr=" + dbusr + ", dbpwd="
				+ dbpwd + ", port = "+ dbport + ", tpl_base=" + tpl_base + ", ctr_base=" + ctr_base
				+ "]";
	}
	
	
}
