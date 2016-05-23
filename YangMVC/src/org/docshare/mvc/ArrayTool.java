package org.docshare.mvc;

import java.util.ArrayList;
import java.util.Collection;

import com.docshare.log.Log;

public class ArrayTool {
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
		}else if(!type.contains("VAR")){
			val= o.toString();
		}else{
			val= "'"+o.toString()+"'";
		}
		if(name == null)return val;
		return name +"="+ val;	
	}
//	public static String joinWithWrapper(String joiner,Collection c){
//		StringBuffer sb = new StringBuffer();
//		for(Object o: c){
//			sb.append(joiner);
//			sb.append(valueWrapper(null,o));
//		}
//		String s = sb.toString();
//		if(s.length()<=0)return s;
//		
//		s = s.substring(1);
//		return s;
//	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Test
//	public void testJoin(){
//		ArrayList a =new ArrayList();
//		a.add("sfsf");
//		a.add(12);
//		a.add(33);
//		a.add(33.2);
//		a.add("fff");
//		Log.i(joinWithWrapper(",",a));
//	}
	
}
