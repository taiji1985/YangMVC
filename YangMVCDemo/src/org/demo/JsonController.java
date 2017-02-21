package org.demo;

import org.docshare.mvc.Controller;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

class Ret{
	public String result="ok";
	public String msg="";
}
public class JsonController extends Controller {
	public void index(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all().limit(30);
		outputJSON(list);
	}
	
	public void ding(){
		int bookid = paramWithDefaultInt("bookid", 1);
		int uid = paramWithDefaultInt("uid", 1);
		int ct = paramWithDefaultInt("ct", 1);
		

		DBTool tool = Model.tool("dingcan");
		Model m = tool.create();
		m.put("book_id",bookid);
		m.put("uid"	, uid);
		m.put("ct", ct);
		
		tool.save(m);
		outputJSON(new Ret());
	}
}
