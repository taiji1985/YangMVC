package cn.org.docshare.demo;

public class MyController extends IndexController {
	public String ret(){
		return "ret ok";
	}
	public String pp(int a,String b){
		return "a = "+a+", b=  "+b;
	}
}
