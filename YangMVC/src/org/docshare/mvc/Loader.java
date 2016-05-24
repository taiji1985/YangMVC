package org.docshare.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;


public class Loader {

	@SuppressWarnings("rawtypes")
	public static Class load(String p){
		try {
			return Class.forName(p);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return null;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean call(String cname,String method,HttpServletRequest req,HttpServletResponse resp){
		if(cname == null || method == null){
			return false;
		}
		
		if(! cname.endsWith("Controller")){
			int p = cname.lastIndexOf(".");
			String lastname = cname.substring(p+1);
			String first = lastname.substring(0, 1).toUpperCase();
			lastname = first + lastname.substring(1)+"Controller";
			cname = cname.substring(0, p+1)+lastname;
		}
		Log.i("Call "+cname +", method = "+method);
		
		Class cz = load(cname);
		if(cz == null){
			return false;
		}
		
		Method m;
		try {
			Object obj = cz.newInstance();
			if(! (obj instanceof Controller)){
				Log.e("error : The Class is not subclass of Controller: "+cname);
				return false;
			}
			Controller ins = (Controller) obj;
			ins.setReq(req, resp);
			m = cz.getMethod(method);
			m.invoke(ins);
			
			Log.i("call success");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			String msg = Log.getErrMsg(e);
			PrintWriter pw;
			try {
				pw = resp.getWriter();
				pw.print(msg.replace("\n", "\n<br>").replace("\t","&nbsp;&nbsp;&nbsp;&nbsp;"));
				pw.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			return false;
		}
		
	}
}
