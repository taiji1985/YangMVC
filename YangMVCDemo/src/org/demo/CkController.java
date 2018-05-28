package org.demo;

import org.docshare.mvc.Controller;

public class CkController extends Controller {
	public void set(){
		cookie("a",12,1000*60*2); 
		output("ok");
	}
	public void get(){
		output(cookie("a"));
	}
}
