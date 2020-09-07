package org.demo;

import java.lang.reflect.Method;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.mvc.Controller;
import org.docshare.mvc.Interceptor;

import com.esotericsoftware.reflectasm.MethodAccess;

public class Init {
	/**
	 * 在此可以进行初始化。比如如果不愿意在web.xml中写死DB数据，可以在此直接修改Config.dbname等属性
	 */
	public Init(){ 
		Log.e("I can init something in Init class");
		Config.addInterceptor(new Interceptor(){

			@Override
			public boolean intercept(String uri, Controller c,MethodAccess access,String methodName,Method m) {
				if(! uri.startsWith("/int")){ //只拦截这个
					return true;
				}
				if(null == c.param("haha"))
				{
					c.output("{这个页面被DemoIntercept拦截了，看Init类。 它要求必须有haha参数， <a href='?haha=true'>点这里</a> }");
					return false;
				}
				return true;
			}
			
			@Override
			public String name() {
				return "DemoIntercept";
			}
		});
	}
	
}
