package org.demo;

import org.docshare.yangmvc.mvc.Controller;

public class IntController extends Controller {
	public void index(){
		output("nice to meet you ! The value you gave for haha is "+param("haha") +",try without parameter<a href='?'>here</a>");
	}
}
