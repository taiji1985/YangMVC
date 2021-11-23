package cn.org.docshare.demo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletContext;

import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.orm.Model;
import org.docshare.util.MapHelper;

import cn.org.docshare.test.Demo;
public class IndexController extends Controller {
	public void index(){
		renderFreeMarker("/index.html");
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
	public void app(){
		output("context = "+application.hashCode());
	}
	public void app2(){
		ServletContext context = application;
		output("context = "+context +",haha="+request.getSession()+",session servletContext="+request.getSession().getServletContext());
		
	}
	public void fmParam(){
		if(param("haha") == null ) {
			jump("fmParam?haha=123");
			return;
		}
		renderFreeMarker("/fm.html");
	}
	public void error(){
		renderFreeMarker("/err.html");
	}
	public void err(){
		int a = 0;
		int b = 12/a;
	}
	public String retstr(){
		return "hello";
	}
	public Object retjson(){
		return MapHelper.toMap("yang",123,"wang",444);
	}
	public Object retfm(){
		return freemarker("/index.html");
	}
	public Object putMore(){
		put("yang",12,"zhang",33);
		return "ok";
	}
	public String tryreload(){
		return ""+Demo.hello();
	}
	public Object demo_ws(){
		return freemarker("/ws.html");
	}
	
	public String log(){
		Log.i("hello");
		return "ok";
	}
}
