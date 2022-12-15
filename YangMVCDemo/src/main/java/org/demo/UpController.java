package org.demo;

import org.docshare.mvc.Controller;

public class UpController extends Controller {
	public void index(){
		if(isPost()){
			put("msg","param tt is "+param("tt")+"<br>param ff is "+param("ff"));
		}
		render();
	}
}
