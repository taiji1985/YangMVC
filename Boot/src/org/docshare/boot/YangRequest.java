package org.docshare.boot;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Request;

public class YangRequest extends Request {
	@Override
	public ServletContext getServletContext() {
		return YangServerContext.getIns();
	}
}
