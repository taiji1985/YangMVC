package org.docshare.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;
import org.docshare.mvc.MethodAccessCacher.MyParam;
import org.docshare.mvc.except.MVCException;
import org.docshare.util.TextTool;

import com.esotericsoftware.reflectasm.MethodAccess;

public class Loader {
	/**
	 * 执行后处理程序 ， 如将obj转json
	 * @param uri
	 * @param c
	 * @param ret
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

		if(reloader == null){
			String reload_base = TextTool.getParentPackage(Config.controller);
			if(reload_base.equals("org") || reload_base.equals("org.docshare")){
				Log.e("reload base can not be 'org' or 'org.docshare', so we use your controller base as reload base.");
				reload_base =Config.controller;
			}
			Log.i("reload base : "+reload_base);

			String groovy_path = Config.getProperty("groovy", null);
			if(groovy_path == null){
				reloader=new Reloader("/", reload_base);
			}else{
				//reloader = new GroovyReloader("/", reload_base);
				try {
					reloader = (Reloader)Class.forName("org.docshare.mvc.GroovyReloader").newInstance();
					reloader.setParam("/", reload_base);
				} catch (Exception e) {
					//e.printStackTrace();
					Log.e(e);
				}
			}
			Log.i("use reloader "+ reloader.getClass().getName());
		}
		
		return reloader.load(p);
		
	}

	/**
	 * 用以存储单例对象的Map。
	 * key: 类名
	 * Object: 对象
	 */
	 Map<String, Object> singleMap = new HashMap<String, Object>();
	
	 static boolean isGroovyObject(Object obj){
		//groovy.lang.GroovyObject
		Class<?>[] ia = obj.getClass().getInterfaces();
		
		for(Class<?> i : ia){
			if(i.getName().equals("groovy.lang.GroovyObject")) return true;
		}
		return false;
	}
	public  boolean call(String uri,String cname,String method,HttpServletRequest req,HttpServletResponse resp){
		Log.d("Call " + cname + ", method = " + method);
		
		//load class
		Class<?> cz=null;
		Object obj =null;
		try{
			if(singleMap.containsKey(cname)){
				obj = singleMap.get(cname);
				cz = obj.getClass();
				((Controller)obj).clearOutFlag();
				Log.d("Single Mode:"+cname);
			}else{
				cz = load(cname);
				
				if(cz == null){
					return false;
				}
				obj = cz.newInstance();
				if(! (obj instanceof Controller)){
					Log.e("error : The Class is not subclass of Controller: "+cname);
					return false;
				}
				((Controller)obj).clearOutFlag();
			}
		}catch(Exception e){
			Throwable cause = e.getCause();
			if(cause == null){
				cause=e;
			}
			if(e instanceof MVCException ) 
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

			m = MethodAccessCacher.getMethod(cname,method);
			Object ret = "";//返回值
			
			//添加控制器的继承支持。
			if(m == null){ //当前类没找到这个方法，找他的父类，如果他父类是Controller类的子类（不是Controller本身）就尝试查找
				Class<?> parentCz = cz.getSuperclass();
				String parentClass =  parentCz.getName();
				//如果父类不是Controller
				if(! parentClass.equals("org.docshare.mvc.Controller")){
					m = MethodAccessCacher.getMethod(parentClass,method);					
					if(m!=null) {
						cname = parentClass;
						cz = parentCz;
					}
				}				
			}
			
//			if(m == null && isGroovyObject(ins)){ //如果是groovy
//				
//			}
			
			if(m == null){
				ret = "no such method : " + method +" , in class "+cname;
				ins.response.setStatus(500);
				runPostProcessing(uri,ins,ret); //执行后处理程序
				return true;
			}
			access = MethodAccessCacher.getMethodAccess(cname);
			if(!runInterceptors(uri,ins,access,method,m)){
				return true;
			}
			
			
			ret = runMethod(ins,access,m,req,cname);
			runPostProcessing(uri,ins,ret); //执行后处理程序
			
			if(ins.isSingle()){
				singleMap.put(cname, ins);
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
	 * 执行所有拦截器
	 * @param m 
	 * @param method 
	 * @return
	 */
	public  boolean runInterceptors(String uri,Controller controller, MethodAccess access,String methodName, Method m){
		boolean ret = true;
		for(Interceptor ic : Config.interceptors){
			if(ic == null) continue;
			Log.d("Call Intercept "+ic.name());
			ret = ic.intercept(uri, controller,access,methodName,m);
			if(!ret ) return false;
		}
		return true;
	}
	

	/**
	 * 执行方法
	 * @param m
	 * @param req
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public  Object  runMethod(Object obj,MethodAccess access,Method method ,HttpServletRequest req,String className){
		
		Object[] args  = null;
		Controller controller = (Controller)obj;
		if(className == null){
			className = obj.getClass().getName();
		}
		MyParam[] mp = MethodAccessCacher.getMethodParam(className, method.getName());
		args=new Object[mp.length];
		for(int i=0;i<args.length;i++){
			args[i]=convertTo(controller.param(mp[i].param),mp[i].type);
		}
		
		
		try {
			return access.invoke(obj, method.getName(), args);
		} catch (ClassCastException e) {
			Log.e("class cast exception "+e.getMessage());
			try {
				return method.invoke(obj, args);
			} catch (IllegalAccessException e1) {
				Log.e("IllegalAccessException " +e1);
			} catch (IllegalArgumentException e1) {
				Log.e("IllegalArgumentException " +e1);
			} catch (InvocationTargetException e1) {
				Log.e("InvocationTargetException " +e1);
			}
		}
		return null;
	}
	
	/**
	 * 自动类型转换
	 * @param v
	 * @param type
	 * @return
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
    		v =new Integer(Integer.parseInt(v+""));
    	}
    	else if(type.equals(Long.class.getName())){
    		v = new Long( Long.parseLong(v+""));
    	}else if(type.equals(Boolean.class.getName())){
    		v = new Boolean(Boolean.parseBoolean(v+""));
    	}else if(type.equals(Double.class.getName())){
    		v = new Double(Double.parseDouble(v+""));
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
