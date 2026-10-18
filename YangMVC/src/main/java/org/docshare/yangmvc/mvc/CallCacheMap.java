package org.docshare.yangmvc.mvc;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.esotericsoftware.reflectasm.MethodAccess;

class CallCacheMap {
	private static final Logger logger = LoggerFactory.getLogger(CallCacheMap.class);

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
	static Map<String, CallCache> url2CallCache=new ConcurrentHashMap<String, CallCache>();
	
	public static boolean runCallCache(String uri,HttpServletRequest req,HttpServletResponse resp){
		if(!url2CallCache.containsKey(uri))return false;
		
		CallCache cach = url2CallCache.get(uri);
		try {
			Controller ins=cach.single;
			if(ins == null){ //单例
				ins = (Controller) cach.clazz.newInstance();
			}
			logger.debug("CacheCall class=", cach.clazz.getName(),",method=",cach.methodName);
			ins.setReq(req, resp);
			
			if( ! "login".equals(cach.methodName) &&  ! ins.checkRequire()){ //如果未通过检测
				ins.actionRequire(false);
				return true;
			}

			if(!MVCFilter.loader.runInterceptors(uri,ins,cach.access,cach.methodName,cach.m)){
				return true;
			}
			//cach.m.invoke(obj);
			Object ret = MVCFilter.loader.runMethod(ins,cach.access,cach.m,req,cach.clazz.getName());

			MVCFilter.loader.runPostProcessing(uri,ins,ret); //执行后处理程序
			
			return true;
		} catch (Throwable e) {
			// 关键：这里必须"接管"请求。返回 false 会让 MVCFilter 走正常路径再执行一遍，
			// 造成同一次请求执行两次（重复插入/重复发码）。见 P0-1。
			logger.error("run action method error: "+uri);
			MVCFilter.renderError(uri, req, resp, e);
			return true;
		}
		
	}
	public static void addCache(String uri,Class<?> clazz,MethodAccess access,String methodName,Controller single,Method m){
		CallCache cache = new CallCache();
		cache.clazz = clazz;
		cache.access = access;
		cache.methodName = methodName; 
		cache.single = single;
		cache.m=m;
		
		url2CallCache.put(uri, cache);
	}

	/**
	 * 热加载换代时调用：清掉缓存的 Class/Method/Singleton，避免继续使用旧代的类。
	 */
	public static void clear(){
		url2CallCache.clear();
	}

}
