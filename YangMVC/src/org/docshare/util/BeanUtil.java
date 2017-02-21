package org.docshare.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docshare.mvc.TextTool;

public class BeanUtil {
	public static List<String> propList(Object obj){
		List<String> ret = new ArrayList<String>();
		Field[] fa = obj.getClass().getFields();
		Method[] ma = obj.getClass().getMethods();
		for(Field f: fa){
			//System.out.println("Field "+f.getName()+" "+f.getType().getName());
			ret.add(f.getName());
		}
		for(Method m: ma){
			String name = m.getName();
			if(name.startsWith("set") && name.length()> 3 ){
				ret.add(TextTool.firstLower(name.substring(3)));
			}
		}
		return ret; //TODO
	}
	private static Method getMethod(Object obj,String mname){
		Method[] ma = obj.getClass().getMethods();
		for(Method m: ma){
			//System.out.println("method "+m.getName());
			if(mname.equals(m.getName())){
				return m;
			}
		}
		return null;
	}
	
	public static boolean set(Object obj,String pname,Object val){
		String sub = null;
		if(pname.contains(".")){ //这是一个复合参数
			sub = TextTool.getAfter(pname, ".");
			pname = TextTool.getBefore(pname, ".");
		}
		Class<?> clazz = obj.getClass();
		try {
			Field f = clazz.getField(pname);
			//if(f.getType())
			if(sub == null){
				f.set(obj, transType(val,f.getType().getSimpleName()));
			}else{ //如果有.存在，则找到下一级
				Object fieldVal = f.get(obj);
				set(fieldVal, sub, val);
			}
			return true;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		Method setm,getm;
		try {
			if(sub !=null){
				String getname = "get"+TextTool.firstUpper(pname);
				getm = getMethod(obj, getname);
				Object fieldVal = getm.invoke(obj);
				if(fieldVal == null) return false;
				set(fieldVal,sub,val);
			}else{
				String setname = "set"+TextTool.firstUpper(pname);
				
				setm = getMethod(obj, setname);
				if(setm == null){
					return false; //not found
				}
				Class<?>[] params = setm.getParameterTypes();
				if(params ==null || params.length!=1){
					return false; //参数不匹配
				}
				String ptype = params[0].getSimpleName();
				//System.out.println("ptype is "+ptype);
				setm.invoke(obj, transType(val,ptype));
			}
			return true;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return false;
	}
	private static Object transType(Object obj,String toType){
		if(obj == null)return null;
		
		
		String sval = obj.toString();
		if(toType.equals("String")){
			return sval;
		}
		if(toType.equals("int") || toType.equals("Integer")){
			return Integer.parseInt(sval);
		}
		toType = toType.toLowerCase();
		if(toType.equals("long") ){
			return Long.parseLong(sval);
		}
		if(toType.equals("float")){
			return Float.parseFloat(sval);
		}
		if(toType.equals("double")){
			return Double.parseDouble(sval);
		}
		
		return obj; //unknown ,return orignal
		
	}

}
