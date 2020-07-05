package org.demo;

import org.docshare.mvc.Controller;

public class InjController extends Controller {
	public String index(int a,int b){
		return "a= "+a+" b = "+b;
	}
}
