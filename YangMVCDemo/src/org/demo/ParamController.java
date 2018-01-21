package org.demo;

import org.docshare.mvc.Controller;

public class ParamController extends Controller {
	public void index(){
		String s = urlParam("p");
		if(s == null){
			render();
			return;
		}
		output(s);
		
		
	}

}
