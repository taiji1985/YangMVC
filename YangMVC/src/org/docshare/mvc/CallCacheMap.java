package org.docshare.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;

import com.esotericsoftware.reflectasm.MethodAccess;


class CallCacheMap {
	/**
	 * 为了减少路径映射中字符串处理所设置的缓存
	 * @author Administrator
	 *
	 */
	public static class CallCache{
		public String uri;//来访最原始的uri，未经处理
		
		@SuppressWarnings("rawtypes")
		public Class clazz;//对应的类
		public Method m ;//对应的方法
		public MethodAccess access;
		public String methodName;
		public Controller single =null;//如果是单例模式，则保存对应的对象，否则保存null
	}
	static HashMap<String, CallCache> map=new HashMap<String, CallCache>();
	
	public static boolean runCallCache(String uri,HttpServletRequest req,HttpServletResponse resp){
		if(!map.containsKey(uri))return false;
		
		try {
			CallCache cach = map.get(uri);
			Controller ins=cach.single;
			if(ins == null){ //单例
				ins = (Controller) cach.clazz.newInstance();
			}
			Log.d("CacheCall class=", cach.clazz.getName(),",method=",cach.methodName);
			ins.setReq(req, resp);
			
			if( ! "login".equals(cach.methodName) &&  ! ins.checkRequire()){ //如果未通过检测
				ins.actionRequire(false);
				return true;
			}
			ins.clearOutFlag(); //清空输出标志，说明还未输出

			if(!Loader.runInterceptors(uri,ins,cach.access,cach.methodName,cach.m)){
				return true;
			}
			//cach.m.invoke(obj);
			Object ret = Loader.runMethod(ins,cach.access,cach.m,req,cach.clazz.getName());

			Loader.runPostProcessing(uri,ins,ret); //执行后处理程序
			
			return true;
		} catch (Exception e) {
			Log.e("run action method error: "+uri);
			e.printStackTrace();
		}
		return false;
		
	}
	@SuppressWarnings("rawtypes")
	public static void addCache(String uri,Class clazz,MethodAccess access,String methodName,Controller single,Method m){
		CallCache cache = new CallCache();
		cache.clazz = clazz;
		cache.access = access;
		cache.methodName = methodName; 
		cache.single = single;
		cache.m=m;
		
		map.put(uri, cache);
	}

}
