package org.demo;



import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.mvc.except.NullParamException;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;



public class BookController extends Controller {
	public void index(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all().orderby("id", false);
		put("books", page(list));
		
		render();
	}
	public void listWithLimit(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all().limit(20);
		put("books", list);
		render("/book/index.jsp");
	}
	
	public void customQuery(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.fromSQL("select * from book where id>23").limit(20);
		put("books", list);
		render("/book/index.jsp");
		
	}
	public void edit() throws NullParamException{

		DBTool tool = Model.tool("book");
		//处理提交数据
		if(isPost()){ //isPost
			Model m = tool.get(paramInt("id"));
			Log.d(m);
			paramToModel(m);
			tool.save(m);
			put("msg","修改成功");
		}

		//显示数据
		Integer id = paramInt("id");
		checkNull("id", id);
		renderForm(tool.get(id));

	}
	
	public void add(){
		DBTool tool = Model.tool("book");
		//处理提交数据
		if(isPost()){ //isPost
			Model m = tool.create(); //创建新的
			Log.d(m);
			paramToModel(m);
			tool.save(m);
			put("msg","添加成功");
		}

		//显示数据
		renderForm(tool.create());
	}
	
	public void del(){
		Integer id = paramInt("id");
		Model.tool("book").del(id);
		jump("index");
		
		
	}
	
	public void testtable(){

		DBTool tool = Model.tool("book");
		LasyList list = tool.all().orderby("id", false);
		LasyList plist = page(list);
		
		
		this.putListTable("mylist", plist);
		render();
	}
	
	public void testdbjson(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all().orderby("id", false);
		LasyList plist = page(list);
		this.outputJSON(plist);
	}
}
