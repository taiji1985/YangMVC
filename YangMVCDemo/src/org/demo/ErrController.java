package org.demo;

import org.docshare.mvc.Controller;

public class ErrController extends Controller {
	public String zero(){
		int a = 0;
		int b = 12/a;
		return "ok?";
	}
}
