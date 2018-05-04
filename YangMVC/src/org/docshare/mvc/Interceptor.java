package org.docshare.mvc;

import java.lang.reflect.Method;

public abstract class Interceptor {
	/**
	 * 拦截器最核心的函数
	 * @param uri 目标url
	 * @param c 目标控制器
	 * @param method 
	 * @return 不拦截返回true
	 */
	public abstract boolean intercept(String uri,Controller c, Method method);

	public String name() {
		return getClass().getName();
	}

}
