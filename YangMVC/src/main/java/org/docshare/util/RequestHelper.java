package org.docshare.util;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

public class RequestHelper {
	public static String params(HttpServletRequest request){
		Enumeration<String> enu=request.getParameterNames();
		StringBuilder sb =new StringBuilder();
		boolean isFirst = true;
		while(enu.hasMoreElements()){
			String key=(String)enu.nextElement();
			String val = request.getParameter(key);
			if(val.length()>20) val = val.substring(0,20)+"...";
			if(isFirst){
				isFirst =false;
			}else{
				sb.append(",");
			}
			sb.append(key+"="+val);
		} 
		return sb.toString();
	}
}
