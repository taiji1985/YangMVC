package org.docshare.orm;

import java.util.Collection;

class ArrayTool {
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
		if(name == null)return "";
		if(o == null) return String.format(" `%s` is NULL ", name);
		return String.format(" `%s`='%s' ", name,o.toString().replace("'", "''"));
	}

	
}
