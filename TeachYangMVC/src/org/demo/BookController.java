package org.demo;

import org.demo.easyui.ChangeRet;
import org.demo.easyui.ListRet;
import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

/**
 * BookController对应book/这个路径
 * @author Administrator
 *
 */
public class BookController extends Controller {
	/**
	 * DBTool 是数据库表的操作类，用于做各种查询等 ， 它相当其他框架的于dao类
	 * 它能和任意一个表进行关联。
	 * 使用put将数据塞入view之中
	 */
	public void index(){
		DBTool dbTool= Model.tool("book");
		LasyList list = dbTool.all().like("name", "本草").limit(10).orderby("id", false);
		put("books", list);
		render();
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
            jump("/book/");
        }

        //显示数据
        render("/book/add2.jsp");
        
        //renderForm(tool.create());
    }
    
    public void easyui_list(){
    	render();
    }
    /**
     * 专门提供easyui datagrid数据的方法
     */
    public void easyui_data(){
    	String sort = (String) paramWithDefault("sort","id");
    	String order = (String)paramWithDefault("order","asc");
    	
    	
		DBTool dbTool= Model.tool("book");
		LasyList list = dbTool.all().orderby(sort, order.equals("asc"));
    	ListRet listRet=  new ListRet();
    	listRet.total = dbTool.all().size(); //total是所有数据的个数，easyui计算页数（凭据）
    	
    	Integer rows = paramWithDefaultInt("rows",10);
    	Integer page = paramWithDefaultInt("page",1) - 1;
    	
    	list = list.limit(page*rows, rows);
    	listRet.rows = list;
    	
    	
    	outputJSON(listRet);
    }
    public void easyui_add(){
         DBTool tool = Model.tool("book");
    	 Model m = tool.create(); //创建新的
         Log.d(m);
         paramToModel(m);
         tool.save(m);
         outputJSON(ChangeRet.getOKMsg("添加成功"));
    }
    public void easyui_edit(){
        DBTool tool = Model.tool("book");
    	Model m = tool.get(paramInt("id"));
        Log.d(m);
        paramToModel(m);
        tool.save(m);
        outputJSON(ChangeRet.getOKMsg("修改成功"));
        
    }
    
    public void easyui_del(){
        DBTool tool = Model.tool("book");
    	//Model m = tool.get(paramInt("id"));
    	//tool.del(m);
    	tool.del(paramInt("id"));
        outputJSON(ChangeRet.getOKMsg("删除成功"));
    	
    }
}
