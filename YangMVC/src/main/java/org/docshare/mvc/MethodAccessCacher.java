package org.docshare.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.mvc.anno.Param;

import com.esotericsoftware.reflectasm.MethodAccess;

public class MethodAccessCacher {
	private static HashMap<String, MethodAccess> ma_map = new HashMap<>();
	public static MethodAccess getMethodAccess(String clsName){
		return ma_map.get(clsName);
	}
	public static Method getMethod(String clsName,String methodName){
		return method_map.get(clsName+"."+methodName);
	}
	/**
	 * 每次加载类都缓存器MethodAccess和Method，加速查找
	 * @param name
	 * @param clz
	 */
	public static void put(String name,Class<?> clz){
		MethodAccess access = MethodAccess.get(clz);
		ma_map.put(name, access);
		buildMethodMap(name,clz);
	}
	private static HashMap<String, Method> method_map = new HashMap<>();
	private static void buildMethodMap(String clzName,Class<?> clazz){
		Method[] ma = clazz.getDeclaredMethods();
		for(Method mm : ma){
			if( Modifier.isPublic(mm.getModifiers())){
				String mname = clzName+"."+mm.getName();
				method_map.put(mname, mm);
				cacheMethodParam(mname,mm);
			}
		}
	}
	public static void putIfNoExist(String clsName, Class<?> ret) {
		if(ma_map.containsKey(clsName)){
			return;
		}
		put(clsName, ret);
	}
	public static class MyParam{
		public String param;
		public String type;
	}
	public static MyParam[] getMethodParam(String clsName,String methodName){
		return method_param_map.get(clsName+"."+methodName);
	}
	private static HashMap<String, MyParam[]> method_param_map = new HashMap<>();
	public static void cacheMethodParam(String mname,Method method){
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();  

		Class<?>[] types = method.getParameterTypes();
		MyParam[] mp=new MyParam[types.length];
		//Controller controller = (Controller)obj;
		/**
		 * 根据参数标注进行赋值
		 */
		if (parameterAnnotations != null && parameterAnnotations.length != 0) {  
			
	        //String[] parameterNames = new String[parameterAnnotations.length];  
	        int i = 0;  
	        for (Annotation[] parameterAnnotation : parameterAnnotations) {  
	            for (Annotation annotation : parameterAnnotation) {  
	                if (annotation instanceof Param) {  
	                    Param param = (Param) annotation;  
	            //        parameterNames[i] = param.value();
	                    mp[i] =new MyParam();
	                    mp[i].param = param.value();//req.getParameter(param.value());
	                    mp[i].type = types[i].getName();
	                    
	                    break;
	                }
	            }  
	            
	            i++;
	        }  
        }
		//再根据名字进行赋值
		if(types.length>0){
			Parameter[] pa = method.getParameters();
			//Log.d("use param name to inj, method=",mname);
			
			for(int i =0;i<pa.length;i++){
				if(mp[i]!=null)continue; //如果已经被标注赋值过就，就不要再给了。
				String name = pa[i].getName();
				if(name == null){
					Log.e("Loader:  you should add the -parameters to javac , for details see : https://blog.csdn.net/sanyuesan0000/article/details/80618913");
				}

                mp[i] =new MyParam();
				mp[i].param =  name;//req.getParameter(name);
				mp[i].type = types[i].getName();
			}
		}
		method_param_map.put(mname, mp);
	}
}
