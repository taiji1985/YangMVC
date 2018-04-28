package org.docshare.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;


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
		public Object single =null;//如果是单例模式，则保存对应的对象，否则保存null
	}
	static HashMap<String, CallCache> map=new HashMap<String, CallCache>();
	
	public static boolean runCallCache(String uri,HttpServletRequest req,HttpServletResponse resp){
		if(!map.containsKey(uri))return false;
		
		try {
			CallCache cach = map.get(uri);
			Object obj=null;
			if(cach.single!=null){ //单例
				obj = cach.single;
			}else{
				obj = cach.clazz.newInstance();
			}
			Log.d(String.format("Call  %s.%s ", cach.clazz.getName(),cach.m.getName()));
			Controller ins = (Controller) obj;
			ins.setReq(req, resp);
			
			if( ! "login".equals(cach.m.getName()) &&  ! ins.checkRequire()){ //如果未通过检测
				ins.actionRequire(false);
				return true;
			}
			ins.clearOutFlag(); //清空输出标志，说明还未输出
			cach.m.invoke(obj);
			
			return true;
		} catch (Exception e) {
			Log.e("run action method error: "+uri);
			e.printStackTrace();
		}
		return false;
		
	}
	@SuppressWarnings("rawtypes")
	public static void addCache(String uri,Class clazz,Method m,Object single){
		CallCache cache = new CallCache();
		cache.clazz = clazz;
		cache.m  = m;
		cache.single = single;
		
		map.put(uri, cache);
	}

}
