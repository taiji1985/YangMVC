package org.demo;

import org.docshare.mvc.Controller;
import org.docshare.util.MapHelper;

public class RetController extends Controller {
	public String index(){
		return "this will output this string";
	}
	public Object json(){
		return MapHelper.toMap("yang",12,"zhang",44,"ww","eeee");
	}
	public Object jj(){
		return jsp("/aa.html");
	}
	public Object ff(){
		return freemarker("/aa.html");
	}
	
}
