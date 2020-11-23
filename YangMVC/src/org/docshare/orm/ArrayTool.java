package org.docshare.orm;

import java.util.Collection;

public class ArrayTool {
	@SuppressWarnings("rawtypes")
	public static String join(String joiner,Collection c){
		if(c.size() == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		boolean first =true;
		for(Object o: c){
			if(first){
				sb.append(o);
				first =false;
			}else{
				sb.append(joiner);
				sb.append(o);
			}
		}
		
		

		return sb.toString();
	}
	@SuppressWarnings("rawtypes")
	public static String joinWithLengthLimit(String joiner,Collection c,int len){
		StringBuilder sb = new StringBuilder();
		for(Object o: c){
			String t = "";
			if( o == null) t= "null";
			else if(o.toString().length()>len){
				t = o.toString().substring(0,len)+"...";
			}else t = o.toString();
			sb.append(joiner+t);
		}
		String s = sb.toString();
		if(s.length()<=0)return s;
		
		s = s.substring(joiner.length());
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
		StringBuilder sb = new StringBuilder();
		if(c == null)return "";
		for(Object o: c){
			sb.append(joiner+o);
		}
		String s = sb.toString();
		if(s.length()<=0)return s;
		
		s = s.substring(joiner.length());
		return s;
	}


	
}
