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
				+",interceptors="+getInteNames(interceptors)
				+", post-process="+getInteNames(postInterceptors)
				+ "]";
	}
	static ArrayList<Interceptor> interceptors =new ArrayList<Interceptor>();
	
	public static void addInterceptor(Interceptor interceptor){
		if(interceptors.contains(interceptor)){
			return;
		}
		interceptors.add(interceptor);
		Log.d("Config.addInterceptor added, name ="+interceptor.name());
	}
	
	public static void removeInterceptor(Interceptor interceptor){
		interceptors.remove(interceptor);
	}
	
	/**
	 * 在运行后执行. 比如将obj转json之类
	 */
	static ArrayList<Interceptor> postInterceptors =new ArrayList<Interceptor>();
	static{
		//默认将根据控制器的返回值的输出转换为响应的形式。
		addPostInterceptor(new BasePostIntercepter(), false);
	}
	/**
	 * 添加后处理程序。 程序是按照从前到后的执行顺序来实现的。前一个的输出作为后一个的输入。<br>
	 * 如果有一个输出为null，则后续的处理不再执行。
	 * @param interceptor 后处理类
	 * @param addToFirst  是否放到第一个。
	 */
	public static void addPostInterceptor(Interceptor interceptor,boolean addToFirst){
		if(postInterceptors.contains(interceptor)){
			return;
		}
		if(addToFirst){
			postInterceptors.add(0, interceptor);
		}else{
			postInterceptors.add(interceptor);
		}
		Log.d("Config.addPostInterceptor added, name ="+interceptor.name());
	}
	
	public static void removePostInterceptor(Interceptor interceptor){
		postInterceptors.remove(interceptor);
	}
	
	private static String getInteNames(ArrayList<Interceptor> list){
		StringBuffer sb =new StringBuffer();
		sb.append("{");
		boolean isFirst = true;
		for(Interceptor i :list){
			if(isFirst){
				isFirst = false;
			}else{
				sb.append(" , ");
			}
			sb.append(i.name());
		}
		sb.append("}");
		return sb.toString();
	}
	
	
}
