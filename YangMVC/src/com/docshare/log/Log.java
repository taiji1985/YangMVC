package com.docshare.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class Log {
	public static <T> void i(T str) {
		System.out.println("[info ]" + str);
	}

	public static <T> void e(T i) {
		System.err.println("[error]" + i);

	}

	public static <T> void d(T str) {
		System.out.println("[debug]" + str);
	}

	public static String getErrMsg(Exception e){
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