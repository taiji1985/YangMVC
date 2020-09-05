package org.docshare.mvc;

import java.lang.reflect.Method;

public abstract class Interceptor {
	/**
	 * 拦截器最核心的函数
	 * @param uri 目标url
	 * @param c 目标控制器
	 * @param method  对应方法
	 * @return 不拦截返回true 
	 */
	public boolean intercept(String uri,Controller c, Method method){
		return true;
	}
	/**
	 * 后处理程序
	 * @param uri 目标url
	 * @param ret 控制器返回的输出对象或者其他层返回的输出对象
	 * @param c 目标控制器
	 * @return 经过这个函数处理后的值
	 */
	public Object postProcess(String uri,Controller c,Object ret){
		return ret;
	}
	public String name() {
		return getClass().getName();
	}

}
