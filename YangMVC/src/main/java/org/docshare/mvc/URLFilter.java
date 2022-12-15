package org.docshare.mvc;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public interface URLFilter {
	public static final String DONE= "yangmvc-done";
	public String doFilter(String uri,HttpServletRequest req,ServletResponse resp);
}
