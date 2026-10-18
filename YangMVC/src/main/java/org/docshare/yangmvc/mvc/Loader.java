package org.docshare.yangmvc.mvc;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.MethodAccessCacher.MyParam;
import org.docshare.yangmvc.mvc.anno.NotAction;
import org.docshare.yangmvc.mvc.except.MVCException;
import org.docshare.yangmvc.util.TextTool;

import com.esotericsoftware.reflectasm.MethodAccess;

public class Loader {
	private static final Logger logger = LoggerFactory.getLogger(Loader.class);

	/**
	 * 执行后处理程序 ， 如将obj转json
	 * @param uri 网址
	 * @param c  控制器
	 * @param ret 控制器方法返回的对象
	 */
	void runPostProcessing(String uri,Controller c,Object ret) {
		for(Interceptor ic : Config.postInterceptors){
			if(ret == null){
				break; 
			}
			ret = ic.postProcess(uri, c,ret);
		}
	}
	Reloader reloader = null;
	int loaderVersion = 0;
	public  Class<?> load(String p) throws ClassNotFoundException{
		if(p.startsWith("org.docshare.yangmvc")){
			return this.getClass().getClassLoader().loadClass(p);
		}
		if(reloader == null){
			String reload_base = Config.controller; // TextTool.getParentPackage(Config.controller);
			if(reload_base.equals("org") || reload_base.equals("org.docshare")){
				logger.error("reload base can not be 'org' or 'org.docshare', so we use your controller base as reload base.");
				reload_base =Config.controller;
			}
			logger.info("reload base : "+reload_base);

			String groovy_path = Config.getProperty("groovy", null);
			if(groovy_path == null){
				reloader=new Reloader("/", reload_base);
			}else{
				//reloader = new GroovyReloader("/", reload_base);
				try {
					reloader = (Reloader)Class.forName("org.docshare.yangmvc.mvc.GroovyReloader").newInstance();
					reloader.setParam("/", reload_base);
				} catch (Exception e) {
					//e.printStackTrace();
					logger.error("", e);
				}
			}
			logger.info("use reloader "+ reloader.getClass().getName());
		}
		
		return reloader.load(p);
		
	}

	/**
	 * 用以存储单例对象的Map。
	 * key: 类名
	 * Object: 对象
	 */
	 static final Map<String, Object> singleMap = new ConcurrentHashMap<>();

	/**
	 * 热加载换代时清空单例表：单例对象的 Class 会钉住旧 loader。
	 */
	static void clearSingletons(){
		singleMap.clear();
	}

	/**
	 * 把某个控制器从单例表里移除（{@code setSingle(false)} 时调用，让"取消单例"立即生效）。
	 */
	static void forgetSingleton(String cname){
		if(cname != null){
			singleMap.remove(cname);
		}
	}
	
	 static boolean isGroovyObject(Object obj){
		//groovy.lang.GroovyObject
		Class<?>[] ia = obj.getClass().getInterfaces();
		
		for(Class<?> i : ia){
			if(i.getName().equals("groovy.lang.GroovyObject")) return true;
		}
		return false;
	}
	public  boolean call(String uri,String cname,String method,HttpServletRequest req,HttpServletResponse resp){
		logger.debug("Call " + cname + ", method = " + method);
		
		//load class
		Class<?> cz=null;
		Object obj;
		try{
			// 注意：ConcurrentHashMap 不允许 null value，所以这里用一次 get 判断即可
			obj = singleMap.get(cname);
			if(obj != null){
				cz = obj.getClass();
				((Controller)obj).clearOutFlag();
				logger.debug("Single Mode:"+cname);
			}else{
				cz = load(cname);
				
				if(cz == null){
					return false;
				}
				obj = cz.newInstance();
				if(! (obj instanceof Controller)){
					logger.error("error : The Class is not subclass of Controller: "+cname);
					return false;
				}
				Controller created = (Controller) obj;
				created.clearOutFlag();
				if(created.isSingle()){
					// 全局单例模式下也要做字段审计（这类控制器没有自己调 setSingle）
					created.auditSingletonFields();
					// 并发首次请求时只允许一个实例胜出，保证"全局一个单例"
					Object prev = singleMap.putIfAbsent(cname, created);
					if(prev != null){
						obj = prev;
						cz = prev.getClass();
						((Controller)prev).clearOutFlag();
					}
				}
			}
		}catch(Exception e){
			Throwable cause = e.getCause();
			if(cause == null){
				cause=e;
			}
			if(e instanceof MVCException)
				throw (MVCException)e;
			else 
				throw new MVCException(cause);
		}
		
		//call the method
		MethodAccess access;
		Method m;
		try {
			
			Controller ins = (Controller) obj;
			ins.setReq(req, resp);
			//如果方法名为login则不做检验。
			if( ! "login".equals(method) &&  ! ins.checkRequire() ){ //如果未通过检测
				ins.actionRequire(false);
				return true;
			}

			m = findActionMethod(cz, method);
			Object ret;//返回值
			
			if(m == null){
				ret = "no such method : " + method +" , in class "+cname;
				ins.response().setStatus(500);

				runPostProcessing(uri,ins,ret); //执行后处理程序
				return true;
			}

			// 方法可能声明在父类（"继承来的 action"）：参数名与 MethodAccess 都要按声明类取。
			// 但 CallCacheMap 里仍然保留具体的控制器类，否则缓存会实例化出父类对象。
			String declName = m.getDeclaringClass().getName();
			access = MethodAccessCacher.getMethodAccess(declName);
			if(!runInterceptors(uri,ins,access,method,m)){
				return true;
			}
			
			
			ret = runMethod(ins,access,m,req,declName);
			runPostProcessing(uri,ins,ret); //执行后处理程序
			
			if(ins.isSingle()){
				// 正常情况在创建时就 putIfAbsent 进去了；这里兜住"在 action 里才调 setSingle(true)"的写法
				singleMap.putIfAbsent(cname, ins);
				CallCacheMap.addCache(uri, cz, access,method, ins,m);
			}else{
				CallCacheMap.addCache(uri, cz, access,method, null,m);
			}
			return true;
		}catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause == null){
				cause=e;
			}
			throw new MVCException(cause);
		}
	}
	

	
	/**
	 * 找出要执行的 action 方法。
	 *
	 * <p>先找控制器自己声明的方法；找不到再去<b>直接父类</b>里找（"继承来的 action"，
	 * 保持历史行为）。父类是框架的 {@link Controller} 本身时不再找——它的
	 * output/param/download/require 这些工具方法绝不能直接暴露成 URL。
	 *
	 * <p>历史实现把包名写成了 {@code org.docshare.mvc.Controller}（少了一段 yangmvc），
	 * 判断永远成立，于是 Controller 的工具方法被当成了接口 —— 这里一并修掉。
	 *
	 * @param cz         控制器类
	 * @param methodName 方法名
	 * @return 找到的方法，没有则返回 null
	 */
	static Method findActionMethod(Class<?> cz, String methodName) {
		if (cz == null || methodName == null) {
			return null;
		}
		Method m = MethodAccessCacher.getMethod(cz, methodName);
		if (m != null) {
			return isNotAction(m) ? null : m;
		}

		Class<?> parent = cz.getSuperclass();
		if (parent == null || parent == Controller.class || !Controller.class.isAssignableFrom(parent)) {
			return null;
		}
		Method pm = MethodAccessCacher.getMethod(parent, methodName);
		if (pm == null) {
			return null;
		}
		if (!Config.inherited_action) {
			logger.warn("action '{}.{}' is inherited from {}, but inherited_action=false, rejected",
					cz.getName(), methodName, parent.getName());
			return null;
		}
		if (isNotAction(pm) || isNotAction(parent)) {
			return null;
		}
		logger.warn("action '{}.{}' is inherited from {}; add @NotAction to the base method if it should not be an endpoint",
				cz.getName(), methodName, parent.getName());
		return pm;
	}

	private static boolean isNotAction(AnnotatedElement e) {
		return e != null && e.isAnnotationPresent(NotAction.class);
	}


	/**
	 * 执行所有拦截器
	 */
	public  boolean runInterceptors(String uri,Controller controller, MethodAccess access,String methodName, Method m) {
		boolean result = true;
		boolean ret;
		for (Interceptor ic : Config.interceptors) {
			if (ic == null) continue;
			logger.trace("Call Intercept " + ic.name());
			ret = ic.intercept(uri, controller, access, methodName, m);
			if (!ret) {
				result = false;
				break;
			}
		}
		return result;
	}
	

	/**
	 * 执行方法
	 */
	public  Object  runMethod(Object obj,MethodAccess access,Method method ,HttpServletRequest req,String className){

		Object[] args;
		Controller controller = (Controller)obj;
		if(className == null){
			className = obj.getClass().getName();
		}
		MyParam[] mp = MethodAccessCacher.getMethodParam(className, method.getName());
		args=new Object[mp.length];
		for(int i=0;i<args.length;i++){
			args[i]=convertTo(controller.param(mp[i].param),mp[i].type);
		}

		// 如果MethodAccess不可用，使用标准反射
		if (access == null) {
			logger.debug("Using standard reflection for " + className + "." + method.getName());
			return invokeReflectively(obj, method, args);
		}

		try {
			return access.invoke(obj, method.getName(), args);
		} catch (ClassCastException e) {
			logger.error("class cast exception "+e.getMessage());
			return invokeReflectively(obj, method, args);
		}
	}

	/**
	 * 普通反射调用。
	 *
	 * <p>注意：控制器抛出的异常必须原样往外抛。老实现这里把 InvocationTargetException
	 * 吞掉返回 null，结果是"热加载/无 ReflectASM 时，业务异常变成 200 空响应"，
	 * 而且 {@link CallCacheMap} 也看不到异常、无法走统一错误页。
	 */
	private Object invokeReflectively(Object obj, Method method, Object[] args) {
		try {
			return method.invoke(obj, args);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new MVCException(cause);
		} catch (IllegalAccessException e) {
			throw new MVCException("can not access " + method, e);
		} catch (IllegalArgumentException e) {
			throw new MVCException("arguments not match " + method, e);
		}
	}
	
	/**
	 * 自动类型转换
	 * @param v 原对象
	 * @param type 要转换成的类型
	 * @return 转换后结果
	 */
	private  Object convertTo(Object v,String type){
		if(v == null){
			//要迁就一下这种基本数据类型
			if(type.equals("int")||type.equals("float")||type.equals("double")){
				return -1; //用-1表示非法数据
			}else return null; //if null, no convert
		}
		String srcType = v.getClass().getName();

		if(srcType.equals(type))return v; //need no convert
		
		if(type.equals("int")){
    		return Integer.parseInt(v.toString());
    	}else if(type.equals("long")){
    		v = Long.parseLong(v+"");
    	}
    	else if(type.equals("double")){
    		v = Double.parseDouble(v+"");
    	}
    	else if(type.equals("java.lang.Integer")){
    		v = Integer.parseInt(v + "");
    	}
    	else if(type.equals(Long.class.getName())){
    		v = Long.parseLong(v + "");
    	}else if(type.equals(Boolean.class.getName())){
    		v = Boolean.parseBoolean(v + "");
    	}else if(type.equals(Double.class.getName())){
    		v = Double.parseDouble(v + "");
    	}
		
		return v;
		
	}
	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public static Method getMethod(Class clazz,String methodName) {
//		try {
//			Method m = null;
//			try {
//				m = clazz.getDeclaredMethod(methodName,(Class[])null);
//			} catch (NoSuchMethodException e) {
//				
//			}
//			//如果找到了，且是公共的，则允许访问
//			if(m !=null &&  Modifier.isPublic(m.getModifiers()))return m; 
//			
//			Method[] ma = clazz.getDeclaredMethods();
//			for(Method mm : ma){
//				if(mm.getName().equals(methodName) &&  Modifier.isPublic(mm.getModifiers())){
//					return mm;
//				}
//			}
//		
//			return null;//没找到。。
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		}
//		return null; //因为安全问题没找到！！
//		
//	}
}
