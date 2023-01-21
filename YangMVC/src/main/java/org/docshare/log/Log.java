package org.docshare.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSON;


public class Log {
	public static boolean showClass = false;
	public static String getCaller() {  
	    StackTraceElement[] stack = (new Throwable()).getStackTrace(); 
	    for(int i=0;i<stack.length-1;i++){
	    	String c = stack[i].getClassName();
	    	if(!c.equals("org.docshare.log.Log")){
	    		return c;
	    	}
	    }
		return "Unknown";
	}  
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static Logger log = Logger.getLogger("YangMVC");
	public static String now(){
		return df.format(new Date());
	}
	public static String W(String t){
		if(!showClass) return t;
		String caller = getCaller();
		return "{"+caller+"} "+t;
	}
	public static  void i(Object str) {

		String t ;
		if(!(str instanceof String)){
			t = JSON.toJSONString(str,true);
		}else{
			t= str.toString();
		}
		log.info(W(t));
		
		//System.out.println(now()+ "[info ]" + str);
	}
	public static  void i(String ...arr){

		StringBuilder sBuffer =new StringBuilder();
		for(String t:arr){
			sBuffer.append(t);
		}
		log.info(W(sBuffer.toString()));
	}

	public static <T> void e(T i) {

		String s = i.toString();
		if(i instanceof Throwable){
			s = getErrMsg((Throwable) i);
		}
		
		log.error(W(s));
		
	}
	public static <T> void e(String f , String...args){

		StringBuilder sBuffer =new StringBuilder();
		for(String t:args){
			sBuffer.append(t);
		}
		e(sBuffer.toString());
	}

	public static <T> void d(T str) { 

		log.debug(W(str.toString()));
	}
	public static  void d(String ...arr){

		StringBuilder sBuffer =new StringBuilder();
		for(String t:arr){
			sBuffer.append(t);
		}
		log.debug(W(sBuffer.toString()));
	}
	public static String getErrMsg(Throwable e){
		ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
		PrintWriter pw = new java.io.PrintWriter(buf, true); 
		e.printStackTrace(pw);
		pw.flush();
		pw.close();
		String  expMessage = buf.toString();
		
		return expMessage;
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

		log.warn(W(m));
	}

	public static void i(String s, Object...args) {

		String str = String.format(s, args);
		log.info(str);
	}

	public static void v(Object obj) {

		log.trace(W(obj.toString()));
	}


}