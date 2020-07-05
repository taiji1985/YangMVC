package org.docshare.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;
import org.docshare.mvc.anno.Param;
import org.docshare.util.TextTool;


class Loader {
	static Reloader reloader = null;
	static int loaderVersion = 0;
	public static Class<?> load(String p) throws ClassNotFoundException{
//		if(true){
//			Log.i("load ..."+p);
//			return Class.forName(p);
//		}
		if(reloader == null){
			String reload_base = TextTool.getParentPackage(Config.ctr_base);
			if(reload_base.equals("org") || reload_base.equals("org.docshare")){
				Log.e("reload base can not be 'org' or 'org.docshare', so we use your controller base as reload base.");
				reload_base =Config.ctr_base;
			}
			Log.i("reload base : "+reload_base);
			reloader=new Reloader("/", reload_base);
		}
		
		return reloader.load(p);
		
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Method getMethod(Class clazz,String methodName) {
		try {
			Method m = null;
			try {
				m = clazz.getDeclaredMethod(methodName,(Class[])null);
			} catch (NoSuchMethodException e) {
				
			}
			//如果找到了，且是公共的，则允许访问
			if(m !=null &&  Modifier.isPublic(m.getModifiers()))return m; 
			Method[] ma = clazz.getDeclaredMethods();
			for(Method mm : ma){
				if(mm.getName().equals(methodName)){
					return mm;
				}
			}
			return null;//没找到。。
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null; //因为安全问题没找到！！
		
	}
	
	/**
	 * 执行所有拦截器
	 * @param method 
	 * @return
	 */
	public static boolean runInterceptors(String uri,Controller controller, Method method){
		boolean ret = true;
		for(Interceptor ic : Config.interceptors){
			if(ic == null) continue;
			Log.d("Call Intercept "+ic.name());
			ret = ic.intercept(uri, controller,method);
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
	public static Object  runMethod(Object obj,Method method ,HttpServletRequest req) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();  
		
		Object[] args  = null;
		Class<?>[] types = method.getParameterTypes();
		/**
		 * 根据参数标注进行赋值
		 */
		if (parameterAnnotations != null && parameterAnnotations.length != 0) {  
			
			
			args =new Object[parameterAnnotations.length];
	        //String[] parameterNames = new String[parameterAnnotations.length];  
	        int i = 0;  
	        for (Annotation[] parameterAnnotation : parameterAnnotations) {  
	            for (Annotation annotation : parameterAnnotation) {  
	                if (annotation instanceof Param) {  
	                    Param param = (Param) annotation;  
	            //        parameterNames[i] = param.value();
	                    args[i] = req.getParameter(param.value());
	                    args[i] = convertTo(args[i],types[i].getName());
	                    
	                    break;
	                }
	            }  
	            
	            i++;
	        }  
        }
		//再根据名字进行赋值
		if(types.length>0){
			Parameter[] pa = method.getParameters();
			Log.d("use param name to inj ");
			if(args == null){
				args =new Object[pa.length];
			}
			for(int i =0;i<pa.length;i++){
				if(args[i]!=null)continue; //如果已经被标注赋值过就，就不要再给了。
				String name = pa[i].getName();
				if(name == null){
					Log.e("Loader:  you should add the -parameters to javac , for details see : https://blog.csdn.net/sanyuesan0000/article/details/80618913");
				}
				args[i] = req.getParameter(name);
				args[i] = convertTo(args[i],types[i].getName());
			}
		}
		
		//调用方法
		Object ret = method.invoke(obj, args);
		return ret;
		
	}
	/**
	 * 自动类型转换
	 * @param v
	 * @param type
	 * @return
	 */
	private static Object convertTo(Object v,String type){
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
	
	/**
	 * 用以存储单例对象的Map。
	 * key: 类名
	 * Object: 对象
	 */
	static Map<String, Object> singleMap = new HashMap<String, Object>();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean call(String uri,String cname,String method,HttpServletRequest req,HttpServletResponse resp){
		if(cname == null || method == null){
			return false;
		}
		
		if(! cname.endsWith("Controller") ){
			int p = cname.lastIndexOf(".");
			String lastname = cname.substring(p+1);
			String first = lastname.substring(0, 1).toUpperCase();
			lastname = first + lastname.substring(1)+"Controller";
			cname = cname.substring(0, p+1)+lastname;
		}
		Log.d("Call " + cname + ", method = " + method);
		
		//load class
		Class cz=null;
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
			String msg = Log.getErrMsg(cause);
			Log.e(msg);
			outMsg(msg,resp);
			return false;
		}
		
		//call the method
		Method m;
		try {
			
			Controller ins = (Controller) obj;
			ins.setReq(req, resp);
			//如果方法名为login则不做检验。
			if( ! "login".equals(method) &&  ! ins.checkRequire() ){ //如果未通过检测
				ins.actionRequire(false);
				return true;
			}

			//m = cz.getMethod(method);
			m = getMethod(cz,method);
			if(m == null){
				throw new NoSuchMethodException();
			}
			if(!runInterceptors(uri,ins,m)){
				return true;
			}
			
			
			Object ret = runMethod(ins,m,req);
			runPostProcessing(uri,ins,ret); //执行后处理程序
			
			if(ins.isSingle()){
				singleMap.put(cname, ins);
				CallCacheMap.addCache(uri, cz, m, ins);
			}else{
				CallCacheMap.addCache(uri, cz, m, null);
			}
			//Log.d("call success");
			return true;
		}catch(NoSuchMethodException ne){
			Throwable cause = ne.getCause();
			if(cause == null){
				cause=ne;
			}
			String msg = "没这个方法, no such method " + cname+"."+method +",去查查是否有拼写错误？";
			//String msg = Log.getErrMsg(cause);
			//Log.e(msg);
			Log.e(msg);
			outMsg(msg,resp);
			
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause == null){
				cause=e;
			}
			String msg = Log.getErrMsg(cause);
			Log.e(msg);
			outMsg(msg,resp);
			
			return false;
		}
		return true;
		
	}
	/**
	 * 执行后处理程序 ， 如将obj转json
	 * @param uri
	 * @param c
	 * @param ret
	 */
	static void runPostProcessing(String uri,Controller c,Object ret) {
		for(Interceptor ic : Config.postInterceptors){
			if(ret == null){
				break; 
			}
			ret = ic.postProcess(uri, c,ret);
		}
	}
	public static void outMsg(String msg,HttpServletResponse resp){
		PrintWriter pw;
		try {
			resp.setCharacterEncoding("utf-8");
			resp.setContentType("text/html; charset=UTF-8");
			pw = resp.getWriter();
			pw.println("<html><head><meta charset='utf-8'/></head><body>");
			String m =msg.replace("\n", "\n<br>").replace("\t","&nbsp;&nbsp;&nbsp;&nbsp;");
			m = m.replace(Config.ctr_base, "<font color='red'>"+Config.ctr_base+"</font>");
			pw.print(m);
			pw.println("</body></html>");
			//pw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
}
