package org.docshare.orm;

import java.util.Collection;

public class ArrayTool {
	@SuppressWarnings("rawtypes")
	public static String join(String joiner,Collection c){
		StringBuffer sb = new StringBuffer();
		for(Object o: c){
			sb.append(joiner+o);
		}
		String s = sb.toString();
		if(s.length()<=0)return s;
		
		s = s.substring(1);
		return s;
	}
	
	public static String valueWrapper(String name,Object o,String type){
		String val = "";
		if(o==null){
			val = "null";
		}else if(type.contains("INT") || type.contains("FLOAT") || type.contains("DOUBLE") || type.contains("DECIMAL")){
			val = o.toString();
		}		
		else {//if(type.contains("CHAR") || type.contains("TEXT") || type.contains("DATE") || type.contains("TIME")){
			val= "'"+o.toString()+"'";
		//}else{
		//	val= o.toString();
		}
		if(name == null)return val;
		return name +"="+ val;
	}

	public static String join(String joiner, Object[] c) {
		StringBuffer sb = new StringBuffer();
		if(c == null)return "";
		for(Object o: c){
			sb.append(joiner+o);
		}
		String s = sb.toString();
		if(s.length()<=0)return s;
		
		s = s.substring(1);
		return s;
	}


	
}
