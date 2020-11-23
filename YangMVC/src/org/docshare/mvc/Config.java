package org.docshare.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import org.docshare.log.Log;
import org.docshare.util.BeanUtil;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.xml.internal.ws.policy.sourcemodel.PolicyModelUnmarshaller;

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
	public static  String template;
	//做一点向前兼容 ，名字由 tpl_base改为template
	public static String tpl_base(){ return template;}
	public static void tpl_base(String s){template = s;}
	
	
	/**
	 * 控制器根包名
	 */
	public static  String controller;
	//做一点向前兼容 ,名字由 ctr_base改为了controller
	public static String ctr_base(){return controller;}
	public static void ctr_base(String s){controller = s;}
	
	/**
	 * 是否使用SSL连接，默认为false
	 */
	public static boolean useSSL = false;
	
	public static String str() {
		return "Config [\n\tdbhost=" + dbhost 
				+", \n\tdbname=" + dbname 
				+ ", \n\tdbusr=" + dbusr 
				+ ", \n\tdbpwd="+ dbpwd 
				+ ", \n\tport = "+ dbport 
				+ ", \n\ttemplate=" + template 
				+ ", \n\tcontroller=" + controller
				+",\n\treloadable="+reloadable
				+",\n\tuseSSL="+useSSL
				+",\n\tdbtype="+dbtype
				+",\n\tdbEncoding="+dbEncoding
				+",\n\tinterceptors="+getInteNames(interceptors)
				+", \n\tpost-process="+getInteNames(postInterceptors)
				+ "\n]";
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
		StringBuilder sb =new StringBuilder();
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
	private static InputStream getPropertiesStream(String PROP_FILE){
		try {
			URL purl;
			
			
			File f = new File(PROP_FILE); // ./web.propertes
			if(f.exists()){
				Log.d("read prop from "+ f);

				return new FileInputStream(f);
			}
			
			f = new File("."+PROP_FILE);
			if(f.exists()){
				Log.d("read prop from "+ f);
				return new FileInputStream(f);
			}
			Log.d("class loader name "+Config.class.getClassLoader().toString());
			Log.d("try load from classpath");
			return Config.class.getResourceAsStream(PROP_FILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	static Properties pro=null;
	public static String getProperty(String key,String def){
		if(pro == null){
			return null;
		}
		
		return pro.getProperty(key,def);
	}
	
	/**
	 * 读取properties文件
	 * @param PROP_FILE 属性文件的路径
	 * @return 加载是否成功
	 */
	public static boolean loadProperties(String PROP_FILE){
		InputStream in = null;
		try {
			in = getPropertiesStream(PROP_FILE);
			if(in == null){
				Log.e("Config file NOT found : web.properties ");
				return false;
			}else{
				Log.i("Config  found ! ");
			}

			pro = new Properties();	
			pro.load(in);
			Log.i("web.properties loaded ");
			BeanUtil.prop2StaticField(pro, Config.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			try {
				in.close();
			} catch (Exception e2) {
			}
		}

	}
	
	public static String dateFormat=null; //输出的date的格式化
	public static Object dbEncoding="utf-8";
	public static String pageEncoding="utf-8";
	
	public static void main(String[] args) {
		Config.loadProperties("/web.properties");
	}
}
