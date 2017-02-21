package org.demo.easyui;

public class ChangeRet {
	public String succ;
	public String msg;
	
	public static ChangeRet getOKMsg(String m){
		ChangeRet  ret =new ChangeRet();
		ret.succ = "ok";
		ret.msg = m;
		return ret;
	}
	
	public static ChangeRet getFailMsg(String m){
		ChangeRet  ret =new ChangeRet();
		ret.succ = "fail";
		ret.msg = m;
		return ret;
	}
}
