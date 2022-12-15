package org.demo;

import org.docshare.mvc.Controller;

public class FmController extends Controller {
	public void index(){
		put("name","tomcat");
		renderFreeMarker("/aa.html");
	}
}
