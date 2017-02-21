package org.demo;

import java.util.ArrayList;
import java.util.List;

import org.demo.easyui.TreeRet;
import org.docshare.mvc.Controller;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

public class CatController extends Controller {
	public void tree(){
		render();
	}
	
	public void tree_data(){
		int id = paramWithDefaultInt("id", 0);
		DBTool tool = Model.tool("cat");
		LasyList list = tool.all().eq("parent", id);
		List<TreeRet> ret= new ArrayList<TreeRet>();
		for(Model m: list){
			TreeRet ret2 = new TreeRet();
			ret2.id = m.get("id")+"";
			ret2.text = (String) m.get("name");
			int hasChild  = (Integer) m.get("hasChild");
			ret2.state = hasChild== 1? "closed":"open";
			ret.add(ret2);
		}
		
		outputJSON(ret);
	}
}
