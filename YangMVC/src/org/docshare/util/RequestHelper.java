package org.docshare.util;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

public class RequestHelper {
	public static String params(HttpServletRequest request){
		Enumeration<String> enu=request.getParameterNames();
		StringBuffer sb =new StringBuffer();
		boolean isFirst = true;
		while(enu.hasMoreElements()){
			String key=(String)enu.nextElement();
			String val = request.getParameter(key);
			if(val.length()>10) val = val.substring(0,10);
			if(isFirst){
				sb.append(",");
				isFirst =false;
			}
			sb.append(key+"="+val);
		} 
		return sb.toString();
	}
}
