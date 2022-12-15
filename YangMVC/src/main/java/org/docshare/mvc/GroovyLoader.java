package org.docshare.mvc;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;
import org.docshare.mvc.except.MVCException;

import com.esotericsoftware.reflectasm.MethodAccess;

import groovy.lang.GroovyObject;

public class GroovyLoader extends Loader {

	public  boolean call(String uri,String cname,String method,HttpServletRequest req,HttpServletResponse resp){
		Log.d("Call " + cname + ", method = " + method);
		
		//load class
		Class<?> cz=null;
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
			if(e instanceof MVCException ) 
				throw (MVCException)e;
			else 
				throw new MVCException(cause);
		}
		
		//call the method
		MethodAccess access = null;
		Method m;
		try {
			
			Controller ins = (Controller) obj;
			ins.setReq(req, resp);
			//如果方法名为login则不做检验。
			if( ! "login".equals(method) &&  ! ins.checkRequire() ){ //如果未通过检测
				ins.actionRequire(false);
				return true;
			}
			Object ret = null;//返回值
			
			//首先处理groovyobject
			if( ins instanceof GroovyObject){ //如果是groovy
				if(!runInterceptors(uri,ins,access,method,null)){
					return true;
				}
				//ret = runMethod(ins,access,null,req,cname);
				GroovyObject gobj = (GroovyObject)ins;
				ret = gobj.invokeMethod(method, null);
				
				runPostProcessing(uri,ins,ret); //执行后处理程序
				return true;
			}
			
			m = MethodAccessCacher.getMethod(cname,method);
			
			
			//添加控制器的继承支持。
			if(m == null){ //当前类没找到这个方法，找他的父类，如果他父类是Controller类的子类（不是Controller本身）就尝试查找
				Class<?> parentCz = cz.getSuperclass();
				String parentClass =  parentCz.getName();
				//如果父类不是Controller
				if(! parentClass.equals("org.docshare.mvc.Controller")){
					m = MethodAccessCacher.getMethod(parentClass,method);					
					if(m!=null) {
						cname = parentClass;
						cz = parentCz;
					}
				}				
			}
			
			
			
			if(m == null){
				ret = "no such method : " + method +" , in class "+cname;
				ins.response.setStatus(500);
				runPostProcessing(uri,ins,ret); //执行后处理程序
				return true;
			}
			access = MethodAccessCacher.getMethodAccess(cname);
			if(!runInterceptors(uri,ins,access,method,m)){
				return true;
			}
			
			
			ret = runMethod(ins,access,m,req,cname);
			runPostProcessing(uri,ins,ret); //执行后处理程序
			
			if(ins.isSingle()){
				singleMap.put(cname, ins);
				CallCacheMap.addCache(uri, cz, access,method, ins,m);
			}else{
				CallCacheMap.addCache(uri, cz, access,method, null,m);
			}
			return true;
		}catch (Exception e) {
			Throwable cause = e.getCause();
			if(cause == null){
				cause=e;
			}
			throw new MVCException(cause);
		}
	}
}
