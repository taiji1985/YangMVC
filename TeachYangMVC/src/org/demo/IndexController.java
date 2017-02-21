package org.demo;

import org.docshare.mvc.Controller;
/**
 * 使用IndexController来处理根目录下的请求
 *   / 代表IndexController的 index方法
 *   /test  对应IndexController下的test方法
 *   /test/ 对应的是TestController下的index方法
 *   /book/ 对应于BookController的index方法
 *   如果是mvc包下面的包，就把包的路径带上。
 *   org.demo.dd.WaterController 对应的就是 /dd/water
 *   我们可以使用没有参数的render方法，它与test函数使用对应关系
 *   我们也可以使用带参数的render方法，它会指定一个jsp文件作为他的视图。
 * @author Administrator
 *
 */
public class IndexController extends Controller {
	//默认的入口页
	public void index(){
		output("hello");
	}
	/**
	 * 一个控制器可以包含多个方法
	 * 如何区分？ 在url中给出不同的url就可以区分他们。
	 */
	public void test(){
		render("/tt.jsp");
	}
	class Ret{
		public String s="sss";
		public String d="fff";
	}
	public void json(){
		
		outputJSON(new Ret());
	}
	
	public void testp(){
		String a = param("a");
		output(a);
	}
}
