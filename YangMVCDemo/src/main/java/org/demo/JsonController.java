package org.demo;

import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.LasyList;
import org.docshare.yangmvc.orm.Model;

class Ret{
	public String result="ok";
	public String msg="";
}
public class JsonController extends Controller {
	public void index(){
		LasyList list = Model.tool("book").all().limit(30);
		outputJSON(list);
	}
	
	public void ding(){
		int bookid = paramInt("bookid", 1);
		int uid = paramInt("uid", 1);
		int ct = paramInt("ct", 1);
		

		Model.tool("dingcan")
			.create()
			.put("book_id", bookid)
			.put("uid", uid)
			.put("ct", ct)
			.saveAndRet();
		
		outputJSON(new Ret());
	}
}
