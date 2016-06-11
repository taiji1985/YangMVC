package org.docshare.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.docshare.mvc.Config;

public class Log {
	public static final int LEVEL_DEBUG =0;
	public static final int LEVEL_INFO = 1;
	public static final int LEVEL_ERROR =2;
	
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String now(){
		return df.format(new Date());
	}
	
	public static <T> void i(T str) {
		if(Config.level > LEVEL_INFO)return;
		
		System.out.println(now()+ "[info ]" + str);
	}

	public static <T> void e(T i) {
		
		String s = i.toString();
		if(i instanceof Throwable){
			s = getErrMsg((Throwable) i);
		}
		System.err.println(now()+ "[error]" + s);
		
	}

	public static <T> void d(T str) { 
		if(Config.level > LEVEL_DEBUG)return;
		System.out.println(now()+ "[debug]" + str);
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


}