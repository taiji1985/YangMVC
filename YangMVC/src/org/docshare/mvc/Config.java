package org.docshare.mvc;

import java.util.ArrayList;

import org.docshare.log.Log;

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
	 * 端口号
	 */
	public static String dbport = "3306";
	
	/**
	 *  数据库类型
	 */
	public static String dbtype ="mysql";

	public static String dbschema="public";
	/**
	 * 是否打开实时Reload功能
	 */
	public static boolean reloadable = true;
	
	/**
	 * 模板相对于WebRoot的目录路径
	 */
	public static  String tpl_base;
	/**
	 * 控制器根包名
	 */
	public static  String ctr_base;
	public static int level=0;
	
	/**
	 * 是否使用SSL连接，默认为false
	 */
	public static boolean useSSL = false;
	
	public static String str() {
		return "Config [dbhost=" + dbhost +", dbname=" + dbname + ", dbusr=" + dbusr + ", dbpwd="
				+ dbpwd + ", port = "+ dbport + ", tpl_base=" + tpl_base + ", ctr_base=" + ctr_base
				+",reloadable="+reloadable
				+",useSSL="+useSSL
				+",dbtype="+dbtype
				+",interceptors="+getInteNames()
				+ "]";
	}
	static ArrayList<Interceptor> interceptors =new ArrayList<Interceptor>();
	
	public static void addInterceptor(Interceptor interceptor){
		if(interceptors.contains(interceptor)){
			return;
		}
		interceptors.add(interceptor);
		Log.d("Config.registerInterceptor added, name ="+interceptor.name());
	}
	public static void removeInterceptor(Interceptor interceptor){
		interceptors.remove(interceptor);
	}
	private static String getInteNames(){
		StringBuffer sb =new StringBuffer();
		sb.append("{");
		boolean isFirst = true;
		for(Interceptor i :interceptors){
			if(isFirst){
				isFirst = false;
			}else{
				sb.append(",");
			}
			sb.append(i.name());
		}
		sb.append("}");
		return sb.toString();
	}
	
	
}
