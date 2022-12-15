package org.demo;

import org.docshare.mvc.Controller;

public class IntController extends Controller {
	public void index(){
		output("nice to meet you ! 你给的haha的值为 "+param("haha") +",试试不加参数<a href='?'>这里</a>");
	}
}
