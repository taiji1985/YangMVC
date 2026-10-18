package org.docshare.yangmvc.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.anno.Param;

import com.esotericsoftware.reflectasm.MethodAccess;

public class MethodAccessCacher {
	private static final Logger logger = LoggerFactory.getLogger(MethodAccessCacher.class);

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
	 * 热加载模式下是否使用 ReflectASM。
	 *
	 * <p>默认不用：ReflectASM 1.11 在 JDK16+ 需要 --add-opens 才能定义 access 类，
	 * 而且它会在"加载类"的锁里反向加载类（历史死锁来源），生成类还会占 Metaspace。
	 * 热加载是低频操作，改用普通反射即可。
	 */
	private static boolean useReflectasm(){
		return !Config.reloadable || Config.reload_reflectasm;
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
		class_map.put(name, clz);
		method_map_builded.remove(name);
		if(!useReflectasm()){
			ma_map.remove(name);   // 热加载模式下不建 ReflectASM，走普通反射
			return;
		}
		MethodAccess access = null;
		try {
			access = MethodAccess.get(clz);
			if (access == null) {
				logger.warn("MethodAccess.get() returned null for class: " + name +
					", classloader: " + clz.getClassLoader() +
					". Will use standard reflection as fallback.");
			}
		} catch (Exception e) {
			logger.warn("MethodAccess.get() failed for class: " + name + ", error: " + e.getMessage() +
				". Will use standard reflection as fallback.");
		}
		ma_map.put(name, access);
	}

	/**
	 * 只登记"类名 → Class"，不碰 ReflectASM。热加载时由 {@link Reloader} 在**类加载锁外面**调用，
	 * 让 {@link #getMethod(String, String)} 能拿到当前代的 Method（而不是 Class.forName 的旧类）。
	 */
	public static void remember(String name, Class<?> clz) {
		if (name == null || clz == null) {
			return;
		}
		class_map.put(name, clz);
		method_map_builded.remove(name);
		// 清掉这个名字下的旧 Method（旧 Class 会钉住旧 loader，导致 Metaspace 回收不掉）
		String prefix = name + ".";
		java.util.Iterator<String> it = method_map.keySet().iterator();
		while (it.hasNext()) {
			if (it.next().startsWith(prefix)) {
				it.remove();
			}
		}
	}

	/**
	 * 热加载换代时调用：清空所有缓存，让旧 Class/loader 可以被回收。
	 */
	public static void clear() {
		ma_map.clear();
		class_map.clear();
		method_map.clear();
		method_param_map.clear();
		method_map_builded.clear();
	}
	private static Map<String, Method> method_map = new ConcurrentHashMap<>();
	private static Set<String> method_map_builded = ConcurrentHashMap.newKeySet();
	private static void buildMethodMap(String clzName,Class<?> clazz){
		if(clazz == null){
			if(Config.reloadable){
				// 热加载模式下 class_map 里没有就说明"这一代还没加载过"，绝不能回落到
				// Class.forName（它走应用类加载器，拿到的是父加载器里的旧类）。
				logger.debug("method map not ready for hot-loaded class: {}", clzName);
				return;
			}
			try {
				clazz = Class.forName(clzName);
			} catch (ClassNotFoundException e) {
				logger.error("", e);
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
			logger.error("", e);
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
			//logger.debug("use param name to inj, method=",mname);
			
			for(int i =0;i<pa.length;i++){
				if(mp[i]!=null)continue; //如果已经被标注赋值过就，就不要再给了。
				String name = pa[i].getName();
				if(name == null){
					logger.error("Loader:  you should add the -parameters to javac , for details see : https://blog.csdn.net/sanyuesan0000/article/details/80618913");
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
