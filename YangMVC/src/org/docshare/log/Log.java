package org.docshare.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;

import org.apache.log4j.Logger;
import org.docshare.mvc.Config;


public class Log {
	public static final int LEVEL_DEBUG =0;
	public static final int LEVEL_INFO = 1;
	public static final int LEVEL_ERROR =2;
	public static String getCaller() {  
	    StackTraceElement[] stack = (new Throwable()).getStackTrace(); 
	    return stack[2].getClassName();
	}  
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static Logger log = Logger.getLogger("Log");
	public static String now(){
		return df.format(new Date());
	}
	
	public static <T> void i(T str) {
		if(Config.level > LEVEL_INFO)return;
		log.info(str);
		//System.out.println(now()+ "[info ]" + str);
	}

	public static <T> void e(T i) {
		
		String s = i.toString();
		if(i instanceof Throwable){
			s = getErrMsg((Throwable) i);
		}
		
		log.error(s);
		
	}
	public static <T> void e(String f , String...args){
		String s = new Formatter().format(f, (Object[])args).toString();
		e(s);
	}
	public static boolean debugEnabled(){
		return Config.level > LEVEL_DEBUG;
	}
	public static <T> void d(T str) { 
		//if(Config.level > LEVEL_DEBUG)return;
		log.debug(str);
		//System.out.println(now()+ "[debug]" + str);
	}

	public static String getErrMsg(Throwable e){
		try {
			ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
			e.printStackTrace(new java.io.PrintWriter(buf, true));
			String  expMessage = buf.toString();
			buf.close();
			return expMessage;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "";
	}
	
	@SuppressWarnings("rawtypes")
	public static void map(Map m){
		StringBuffer stringBuffer = new StringBuffer();
		for(Object k:m.keySet()){
			stringBuffer.append(","+k+"="+m.get(k));
		}
		stringBuffer.append("]");
		stringBuffer.setCharAt(0, '[');
		stringBuffer.insert(0, "MAP");
		String s= stringBuffer.toString();
		Log.i(s);
	}

	public static void w(String m) {
		log.warn(m);
	}

	public static void i(String s, Object...args) {
		String str = String.format(s, args);
		log.info(str);
	}

	public static void v(String string) {
		//log.debug(string);
		
	}


}