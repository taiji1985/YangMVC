package org.demo;

import org.docshare.mvc.Controller;

public class PmController extends Controller {
	public PmController(){
		super();
		require("uid", null, "../", "errs");  //��ת�����ҳ��
		//require("uid", null, null, "errs"); //��ʾ������Ϣ
	}
	
	public void index(){
		output("you will see this if you have permisstion");
	}
}
