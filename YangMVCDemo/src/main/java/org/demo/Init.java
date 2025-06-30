package org.demo;

import java.lang.reflect.Method;

import org.docshare.yangmvc.log.Log;
import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.mvc.Interceptor;

import com.esotericsoftware.reflectasm.MethodAccess;

public class Init {

	public Init(){ 
		Log.e("I can init something in Init class");
		Config.addInterceptor(new Interceptor(){

			@Override
			public boolean intercept(String uri, Controller c,MethodAccess access,String methodName,Method m) {
				if(! uri.startsWith("/int")){
					return true;
				}
				if(null == c.param("haha"))
				{
					c.output("{DemoIntercept, See Init。 need haha param， <a href='?haha=true'>Click Here</a> }");
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
