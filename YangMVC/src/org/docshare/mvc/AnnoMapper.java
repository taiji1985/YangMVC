package org.docshare.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.HashAttributeSet;

import org.docshare.mvc.CallCacheMap.CallCache;
import org.docshare.util.PackageUtil;

/**
 * 用以处理url映射的类
 * @author 杨同峰
 *
 */
public class AnnoMapper {
	public HashMap<String, CallCache> annoMap =new HashMap<String,CallCache>();
	/**
	 * 扫描所有的类，并尝试获取注解
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	public void scan(){
		List<String> clsList = PackageUtil.getClassName(Config.ctr_base, true);
		for(String cls : clsList){
			try {
				Class c  = Class.forName(cls);
				Method[] mList = c.getMethods();
				for(Method m :mList){
					RequestMapping mapping = m.getAnnotation(RequestMapping.class);
						String url = mapping.url();
						CallCache cache = new CallCache();
						cache.clazz = c;
						cache.m = m;
					if(mapping!=null){
						cache.uri = mapping.url();
					}else{
						cache.uri = getPathByName(c.getCanonicalName(),m.getName());
					}
					annoMap.put(url, cache);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}
	private String getPathByName(String cname, String mName) {
		cname = cname.replace(Config.ctr_base+".",""); //org.demo.haha.BookController -> haha.BookController
		int point = cname.lastIndexOf(".");
		if(point < 0){ //没有点，说明是一个类名
			cname = "/"+ cname.replace("Controller", "")+"/"+mName;
			return cname;
		}else{
			
		}
		
		
		return null;
	}
}
