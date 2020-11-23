package org.docshare.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSON;


public class Log {
	public static String getCaller() {  
	    StackTraceElement[] stack = (new Throwable()).getStackTrace(); 
	    return stack[2].getClassName();
	}  
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static Logger log = Logger.getLogger("Log");
	static Level level = Logger.getRootLogger().getLevel();
	public static String now(){
		return df.format(new Date());
	}
	
	public static  void i(Object str) {
		if(!Level.INFO.isGreaterOrEqual(level)){
			return ;
		}
		String t ;
		if(!(str instanceof String)){
			t = JSON.toJSONString(str,true);
		}else{
			t= str.toString();
		}
		log.info(t);
		//System.out.println(now()+ "[info ]" + str);
	}
	public static  void i(String ...arr){
		if(!Level.INFO.isGreaterOrEqual(level)){
			return ;
		}
		StringBuilder sBuffer =new StringBuilder();
		for(String t:arr){
			sBuffer.append(t);
		}
		log.info(sBuffer.toString());
	}

	public static <T> void e(T i) {
		if(!Level.ERROR.isGreaterOrEqual(level)){
			return ;
		}
		String s = i.toString();
		if(i instanceof Throwable){
			s = getErrMsg((Throwable) i);
		}
		
		log.error(s);
		
	}
	public static <T> void e(String f , String...args){
		if(!Level.ERROR.isGreaterOrEqual(level)){
			return ;
		}
		StringBuilder sBuffer =new StringBuilder();
		for(String t:args){
			sBuffer.append(t);
		}
		e(sBuffer.toString());
	}

	public static <T> void d(T str) { 
		if(!Level.DEBUG.isGreaterOrEqual(level)){
			return ;
		}
		log.debug(str);
	}
	public static  void d(String ...arr){
		if(!Level.DEBUG.isGreaterOrEqual(level)){
			return ;
		}
		StringBuilder sBuffer =new StringBuilder();
		for(String t:arr){
			sBuffer.append(t);
		}
		log.debug(sBuffer.toString());
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
		StringBuilder sb = new StringBuilder();
		for(Object k:m.keySet()){
			sb.append(',').append(k).append('=').append(m.get(k));
		}
		sb.append(']');
		sb.setCharAt(0, '[');
		sb.insert(0, "MAP");
		String s= sb.toString();
		Log.i(s);
	}

	public static void w(String m) {
		if(!Level.WARN.isGreaterOrEqual(level)){
			return ;
		}
		log.warn(m);
	}

	public static void i(String s, Object...args) {
		if(!Level.INFO.isGreaterOrEqual(level)){
			return ;
		}
		String str = String.format(s, args);
		log.info(str);
	}

	public static void v(String string) {
		if(!Level.TRACE.isGreaterOrEqual(level)){
			return ;
		}
		log.trace(string);
	}


}