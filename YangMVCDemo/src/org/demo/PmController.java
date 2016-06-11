package org.demo;

import org.docshare.mvc.Controller;

public class PmController extends Controller {
	public PmController(){
		super();
		require("uid", null, "../", "errs");  //跳转到别的页面
		//require("uid", null, null, "errs"); //显示错误信息
	}
	
	public void index(){
		output("you will see this if you have permisstion");
	}
}
