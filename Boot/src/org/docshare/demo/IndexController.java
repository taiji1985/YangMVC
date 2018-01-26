package org.docshare.demo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.docshare.mvc.Controller;
import org.docshare.orm.Model;
public class IndexController extends Controller {
	public void index(){
		renderFreeMarker("/index.html");
		//output("看到这个界面，你的YangMVC服务器已经跑起来了。When you see this page, the YangMVC server is stared ! ");
	}
	public void json(){
		outputJSON(Book.createSimple()) ;
	}
	
	public void fm(){
		put("v","this is a val");
		String[] haha  = {"haha1","haha2","haha3"};
		put("arr",haha);
		Book b  = new Book();
		b.name = "haha";
		b.price = 12;  
		b.next = new Book();
		b.next.name = "next_book";
		
		put("book",b);
		Model m = Model.tool("book").all().one();
		put("m", m);   
		HashMap<String, String> mm  = new HashMap<String, String>();
		mm.put("name","haha ");
		put("m2",mm);
		put("mm",mm.toString());
		renderFreeMarker("/haha.html");
	}
	public void setsess(){
		String t =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()); 
		sess("a",t);
		output("a is set to "+t+", sessionId="+session.getId());
	}
	public void getsess(){
		output("a is "+sess("a")+" , sessionId = "+session.getId());
	}public void delsess(){
		removeSession("a");
		output("session is removed , sessionId = "+session.getId());
	}
	public void pm(){
		output(param("p"));
	}
}
