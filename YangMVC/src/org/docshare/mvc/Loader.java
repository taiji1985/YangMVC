package org.docshare.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;


class Loader {
	static Reloader reloader = null;
	static int loaderVersion = 0;
	public static Class<?> load(String p) throws ClassNotFoundException{
//		if(true){
//			Log.i("load ..."+p);
//			return Class.forName(p);
//		}
		if(reloader == null){
			reloader=new Reloader("/", Config.ctr_base);
		}
		
		return reloader.load(p);
		
	}
	
	/**
	 * 执行所有拦截器
	 * @return
	 */
	public static boolean runInterceptors(String uri,Controller controller){
		boolean ret = true;
		for(Interceptor ic : Config.interceptors){
			if(ic == null) continue;
			Log.d("Call Intercept "+ic.name());
			ret = ic.intercept(uri, controller);
			if(!ret ) return false;
		}
		return true;
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
			if(!runInterceptors(uri,ins)){
				return true;
			}
			
			
			m = cz.getMethod(method);
			m.invoke(ins);
			
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
