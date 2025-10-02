package org.docshare.yangmvc.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.docshare.yangmvc.log.Log;
import org.docshare.yangmvc.mvc.anno.Param;

import com.esotericsoftware.reflectasm.MethodAccess;

public class MethodAccessCacher {
	private static Map<String, MethodAccess> ma_map = new ConcurrentHashMap<>();
	private static Map<String, Class<?>> class_map = new ConcurrentHashMap<>();
	public static MethodAccess getMethodAccess(String clsName){
		return ma_map.get(clsName);
	}
	public static Method getMethod(String clsName,String methodName){
		if(!method_map_builded.contains(clsName)){
			buildMethodMap(clsName, class_map.get(clsName));
		}
		return method_map.get(clsName+"."+methodName);
	}
	/**
	 * 每次加载类都缓存器MethodAccess和Method，加速查找
	 * @param name 类名
	 * @param clz 类
	 */
	public static void put(String name,Class<?> clz){
		if(name.endsWith("MethodAccess")){
			return ;
		}
		method_map_builded.remove(name);
		class_map.put(name, clz);
		MethodAccess access = MethodAccess.get(clz);
		ma_map.put(name, access);
		
		//buildMethodMap(name,clz); //不在加载的时候
	}
	private static Map<String, Method> method_map = new ConcurrentHashMap<>();
	private static Set<String> method_map_builded = ConcurrentHashMap.newKeySet();
	private static void buildMethodMap(String clzName,Class<?> clazz){
		if(clazz == null){
			try {
				clazz = Class.forName(clzName);
			} catch (ClassNotFoundException e) {
				Log.e(e);
				return;
			}
		}
		if(clazz == null){
			return;
		}
		try{
			Method[] ma = clazz.getDeclaredMethods();
			for(Method mm : ma){
				if( Modifier.isPublic(mm.getModifiers())){
					String mname = clzName+"."+mm.getName();
					method_map.put(mname, mm);
					cacheMethodParam(mname,mm);
				}
			}
			method_map_builded.add(clzName);
		}catch(Exception e){
			Log.e(e);
		}
		
	}
	public static void putIfNoExist(String clsName, Class<?> ret) {
		// 使用double-checked locking模式避免竞态条件
		if(ma_map.containsKey(clsName)){
			return;
		}
		synchronized(MethodAccessCacher.class) {
			// 再次检查，防止在获取锁的过程中其他线程已经添加了
			if(ma_map.containsKey(clsName)){
				return;
			}
			put(clsName, ret);
		}
	}
	public static class MyParam{
		public String param;
		public String type;
	}
	public static MyParam[] getMethodParam(String clsName,String methodName){
		return method_param_map.get(clsName+"."+methodName);
	}
	private static Map<String, MyParam[]> method_param_map = new ConcurrentHashMap<>();
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
	public static void asyncPut(String name, Class<?> ret) {
		put(name, ret);
	}
	public static Method getMethod(Class<?> parentCz, String methodName) {
		String clsName = parentCz.getName();
		if(!method_map_builded.contains(clsName)){
			buildMethodMap(clsName,parentCz);
		}
		return method_map.get(clsName+"."+methodName);
	}
}
