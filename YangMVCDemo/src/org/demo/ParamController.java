package org.demo;

import org.docshare.mvc.Controller;
import org.docshare.mvc.anno.Param;

public class ParamController extends Controller {
	public void index(){
		String s = urlParam("p");
		if(s == null){
			render();
			return;
		}
		output(s);
		
		
	}
	
	public void ann(@Param("a")String a){
		output("a = "+a);
	}
	public void ann2(@Param("a")String a,@Param("b")String b){
		output("a = "+a+",b = "+b);
	}
	public void annInt(@Param("a")int a,@Param("b")int b){
		output("a = "+a+",b = "+b);
	}
	public void haha(){
		output("haha");
	}

}
