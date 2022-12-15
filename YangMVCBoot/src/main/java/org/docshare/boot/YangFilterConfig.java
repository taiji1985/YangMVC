package org.docshare.boot;
import java.util.Enumeration;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;


public class YangFilterConfig  implements FilterConfig {

	@Override
	public String getFilterName() {
		return null;
	}

	@Override
	public String getInitParameter(String key) {
		
		return getServletContext().getInitParameter(key);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return getServletContext().getInitParameterNames();
	}

	@Override
	public ServletContext getServletContext() {
		return YangServerContext.getIns();
	}

}
