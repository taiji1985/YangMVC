package org.demo;

import org.docshare.mvc.Controller;

public class PmController extends Controller {
	public PmController(){
		super();
		require("uid", null, "../", "errs");
		//require("uid", null, null, "errs");
	}
	
	public void index(){
		output("you will see this if you have permisstion");
	}
}
