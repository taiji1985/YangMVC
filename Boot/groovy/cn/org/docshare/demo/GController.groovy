package cn.org.docshare.demo;
import org.docshare.mvc.Controller

class GController extends Controller {
	def hello(){
		return "hello"
	}
	public void index(){
		output("www")
	}
}
