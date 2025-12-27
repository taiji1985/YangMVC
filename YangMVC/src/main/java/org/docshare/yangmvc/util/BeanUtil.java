package org.docshare.yangmvc.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.IBean;

public class BeanUtil {
	private static final Logger logger = LoggerFactory.getLogger(BeanUtil.class);

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
		return ret; 
	}
	public static HashMap<String, Object> obj2Map(Object obj) {
		HashMap<String, Object> ret=new HashMap<String, Object>();
		
		Field[] fa = obj.getClass().getFields();
		Method[] ma = obj.getClass().getMethods();
		for(Field f: fa){
			//System.out.println("Field "+f.getName()+" "+f.getType().getName());
		//	ret.add(f.getName());
			try {
				//if(f.isAccessible()){
				Object v = f.get(obj);
				if(v!= null && v instanceof IBean){
					v  = obj2Map(v);
				}
				ret.put(f.getName(), v);
				//}
			} catch (Exception e) {
				logger.debug("{}", e);
			} 
		}
		for(Method m: ma){
			String name = m.getName();
			if(name.startsWith("get") && ! name.equals("getClass")  && name.length()> 3 ){
				Object v;
				try {
					v = m.invoke(obj, (Object[]) null);
					ret.put(TextTool.firstLower(name.substring(3)),v);
				} catch (Exception e) {
					e.printStackTrace();
				}
		//		ret.add(TextTool.firstLower(name.substring(3)));
			}
		}
		return ret;
		
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
	public static Object get(Object obj,String pname){
		if(pname.contains(".")){ //这是一个复合参数
			pname = TextTool.getBefore(pname, ".");
		}
		Class<?> clazz = obj.getClass();
		try {
			Field f = clazz.getField(pname);
			if(f != null){
				return f.get(obj);
			}	
		} catch (Exception e) {
			//  handle exception
		}

		String getname = "get"+TextTool.firstUpper(pname);
		Method getm = getMethod(obj, getname);
		if(getm == null)return null;
		try {
			Object fieldVal = getm.invoke(obj);
			return fieldVal;
		} catch (Exception e) {
			logger.debug("{}", e);
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
		}catch (SecurityException e) {
			logger.error("SecurityException");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			//e.printStackTrace();
		} catch (IllegalArgumentException e) {
			String t ;
			if(val == null) t = null;
			else t = val.toString();
			logger.error("arg type not match class=%s , fiend = %s , valtype = %s  val=  %s",obj.getClass().getName(),pname,val.getClass().getName(),t);
			//e.printStackTrace();
		} catch (IllegalAccessException e) {
			String getname = "get"+TextTool.firstUpper(pname);
			Method getm = getMethod(obj, getname);
			if(getm == null){
				logger.error("IllegalAccessException of class %s ,field  %s    make sure the class and field are all public"
						,obj.getClass().getName(),pname);
				//e.printStackTrace();
			}
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
			logger.debug("{}", e);
		}
		return false;
	}
	private static Object transType(Object obj, String toType) {
		// 最优先：如果对象为null，直接返回
		if (obj == null) return null;
		
		// 最高效：如果类型一致，直接返回原对象
		String objTypeName = obj.getClass().getSimpleName();
		if (objTypeName.equals(toType) || obj.getClass().getName().equals(toType)) {
			return obj;
		}
		
		// 处理特殊的对象类型转换 - 使用switch优化instanceof检查
		switch (objTypeName) {
			case "LocalDateTime":
				if ("Date".equals(toType)) {
					java.time.LocalDateTime ldt = (java.time.LocalDateTime) obj;
					return java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
				}
				break;
				
			case "Date":
				java.util.Date date = (java.util.Date) obj;
				java.time.Instant instant = date.toInstant();
				java.time.ZoneId zone = java.time.ZoneId.systemDefault();
				
				switch (toType) {
					case "LocalDateTime":
					case "java.time.LocalDateTime":
						return instant.atZone(zone).toLocalDateTime();
					case "ZonedDateTime":
					case "java.time.ZonedDateTime":
						return instant.atZone(zone);
					case "OffsetDateTime":
					case "java.time.OffsetDateTime":
						return instant.atOffset(java.time.OffsetDateTime.now().getOffset());
				}
				break;
				
			case "ZonedDateTime":
				java.time.ZonedDateTime zdt = (java.time.ZonedDateTime) obj;
				switch (toType) {
					case "Date":
						return java.util.Date.from(zdt.toInstant());
					case "LocalDateTime":
					case "java.time.LocalDateTime":
						return zdt.toLocalDateTime();
					case "OffsetDateTime":
					case "java.time.OffsetDateTime":
						return zdt.toOffsetDateTime();
				}
				break;
				
			case "OffsetDateTime":
				java.time.OffsetDateTime odt = (java.time.OffsetDateTime) obj;
				switch (toType) {
					case "Date":
						return java.util.Date.from(odt.toInstant());
					case "LocalDateTime":
					case "java.time.LocalDateTime":
						return odt.toLocalDateTime();
					case "ZonedDateTime":
					case "java.time.ZonedDateTime":
						return odt.toZonedDateTime();
				}
				break;
		}

		// 字符串转换处理
		String sval = obj.toString();
		
		// 标准化类型名称
		String normalizedType = normalizeTypeName(toType);
		
		switch (normalizedType) {
			case "string":
				return sval;
			case "int":
			case "integer":
				return Integer.parseInt(sval);
			case "long":
				return Long.parseLong(sval);
			case "float":
				return Float.parseFloat(sval);
			case "double":
				return Double.parseDouble(sval);
			case "boolean":
			case "java.lang.boolean":
				return Boolean.parseBoolean(sval);
			case "date":
				return TimeHelper.s2d(sval);
			case "localdatetime":
			case "java.time.localdatetime":
				return TimeHelper.s2ldt(sval);
			case "zoneddatetime":
			case "java.time.zoneddatetime":
				return TimeHelper.s2zdt(sval);
			case "offsetdatetime":
			case "java.time.offsetdatetime":
				return TimeHelper.s2odt(sval);
			default:
				return obj; // unknown, return original
		}
	}
	
	/**
	 * 标准化类型名称，统一转换为小写并处理常见的类型别名
	 */
	private static String normalizeTypeName(String toType) {
		if (toType == null) return "";
		return toType.toLowerCase().trim();
	}
	public static void prop2StaticField(Properties prop,Class<?> clz){
		Field[] fa = clz.getFields();
		for(Field f :fa){
			String name = f.getName();
			String val = prop.getProperty(name);
			try {
				Object ov = transType(val,f.getType().getName());
				if(ov == null)continue;
				f.set(null,ov );
				logger.debug("set "+clz.getName()+" field "+name +" to "+val);
			} catch (Exception e) {
				logger.debug("{}", e);
			}
		}
		//logger.info(Config.str());
		
	}

	public static void copyProperties(Object from,Object to){
		if(from == null || to == null){
			return;
		}
		
		// 获取from对象的所有字段
		Field[] fromFields = from.getClass().getFields();
		for(Field fromField : fromFields){
			try {
				String fieldName = fromField.getName();
				Object value = fromField.get(from);
				
				// 尝试设置到to对象中
				set(to, fieldName, value);
			} catch (Exception e) {
				logger.debug("{}", e);
			}
		}
		
		// 通过getter方法获取属性值
		Method[] fromMethods = from.getClass().getMethods();
		for(Method method : fromMethods){
			String methodName = method.getName();
			if(methodName.startsWith("get") && !methodName.equals("getClass") && methodName.length() > 3){
				try {
					Object value = method.invoke(from, (Object[]) null);
					String propertyName = TextTool.firstLower(methodName.substring(3));
					
					// 尝试设置到to对象中
					set(to, propertyName, value);
				} catch (Exception e) {
					logger.debug("{}", e);
				}
			}
		}
	}

}
