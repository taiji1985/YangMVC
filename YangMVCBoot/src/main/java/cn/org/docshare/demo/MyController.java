package cn.org.docshare.demo;

import java.util.Date;

public class MyController extends IndexController {
	public String ret(){
		return "ret ok";
	}
	public String pp(int a,String b){
		return "a = "+a+", b=  "+b;
	}

	public String set(){
		sess("hello",new Date());
		return session().getId();

	}
	public String get(){
		return sess("hello")+"";
	}
}
