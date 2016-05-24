package test;


import java.util.List;

import junit.framework.TestCase;

import org.docshare.log.Log;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

public class TestDBTool  extends TestCase
{  
	public void testAll(){
		DBTool tool = new DBTool("book");
		List<Model> list =   tool.all();
		Log.i(list.size());
		/*for(Model m :list){
			//System.out.println(m.get("name"));
		}*/
		for(int i=0;i<100;i++){
			Model m = list.get(i);
			assertNotNull(m);
		}
		Model m = list.get(100);
		assertNotNull(m);
		 m = list.get(1);
		assertNotNull(m);
	}
	
	public void testFilters(){
		Log.i("testFilters TEST");
		DBTool tool = new DBTool("book");
		LasyList list =   tool.all().gt("id", 30).lt("id", 40);
		for(Model m :list){
			System.out.println(m.get("id")+","+m.get("name"));
		}
	}
	
	public void testGet(){
		Log.i("testGet TEST");
		DBTool tool = new DBTool("book");
		Model model = tool.get(12);
		Log.i(model);
		
	}
	
	@SuppressWarnings("unused")
	public void testThreadLocal(){
		DBTool tool = new DBTool("book");
		DBTool captool = new DBTool("cap");
		
	}
	
}  