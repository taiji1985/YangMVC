package org.docshare.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.docshare.mvc.CallCacheMap.CallCache;
import org.docshare.mvc.anno.RequestMapping;
import org.docshare.util.PackageUtil;
import org.docshare.util.TextTool;

/**
 * 用以处理url映射的类
 * 
 * @author 杨同峰
 * 
 */
public class AnnoMapper {
	public HashMap<String, CallCache> annoMap = new HashMap<String, CallCache>();

	private HashSet<String> forbidMethod = new HashSet<String>();
	private void scanForbidMethod(){
		forbidMethod.clear();
		Method[] mlist = Controller.class.getMethods();
		for(Method m :mlist){
			forbidMethod.add(m.getName());
		}
	}
	/**
	 * 扫描所有的类，并尝试获取注解
	 */
	@SuppressWarnings({ "rawtypes" })
	public void scan() {
		scanForbidMethod();
		List<String> clsList = PackageUtil.getClassName(Config.controller, true);
		for (String cls : clsList) {
			try {
				Class c = Class.forName(cls);
				if (!(Controller.class.isAssignableFrom(c)))
					continue;

				Method[] mList = c.getMethods();
				for (Method m : mList) {
					if(forbidMethod.contains(m.getName())){
						//Log.d("skip method "+ m.getName());
						continue;
					}
					RequestMapping mapping = m
							.getAnnotation(RequestMapping.class);

					CallCache cache = new CallCache();
					cache.clazz = c;
					cache.m = m;
					if (mapping != null) {
						cache.uri = mapping.url();
					} else {
						cache.uri = getPathByName(c.getCanonicalName(),
								m.getName());
						if(m.getName().equals("index")){
							String uri = cache.uri.substring(0,cache.uri.length()-"index".length());
							annoMap.put(uri, cache);
						}
					}
					annoMap.put(cache.uri, cache);
					if(cache.uri.startsWith("/index")){
						String uri = cache.uri.substring("/index".length());
						annoMap.put(uri, cache);
						
						if(cache.uri.endsWith("index")){
							uri = uri.substring(0, uri.length()-"index".length());
							annoMap.put(uri, cache);
						}
					}
				}
				
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	private String getPathByName(String cname, String mName) {
		cname = cname.replace(Config.controller + ".", ""); // org.demo.haha.BookController
															// ->
															// haha.BookController
		int point = cname.lastIndexOf(".");
		if (point < 0) { // 没有点，说明是一个类名
			cname = "/" + TextTool.firstLower(cname).replace("Controller", "") + "/" + mName;
			return cname;
		} else {
			String before = cname.substring(0, point);
			String after = cname.substring(point + 1);
			before = before.replace(".", "/");
			after = "/" + TextTool.firstLower(after.replace("Controller", "")) + "/" + mName;
			return before + after;
		}

	}
}
