package org.demo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;


public class IndexController extends Controller {
	public IndexController(){
		/*
		 * ʹ������������øÿ�����Ϊ����ģʽ��
		 * ��������web�������иÿ�����ֻ����һ�������ұ�����ʹ�á�
		 * �����������ܣ���Լ��new ��free�Ĺ��̣���Լ��gc�ɱ���
		 * ��ҲҪ�����ٵ�ʹ�ó�Ա������ 
		 * ����ͬ���Ƿ��������⣬�����ۡ�
		 */
		this.setSingle(true); 
	}
	int ct = 0;
	public void count(){
		synchronized (this) {
			ct++;	
		}
		output("count is "+ct);
	}
	public void index(){
		Log.i("index called");
		
		output("Hello YangMVC");
	}
	
	public void paramDemo(){
		put("a", "sss");
		render("/testrd.jsp");
		
	}
	public void renderDemo(){
		request.setAttribute("a", "sss");
		render();
		
	}
	public void mapDemo(){
		put("a", "sss");
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name","This a property of map");
		put("obj", map);
		render();
	}
	public void jsonDemo(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", 12);
		map.put("name", "Yang MVC");
		map.put("addtm",new Date());
		
		outputJSON(map);
	}
	
	public void tt(){
		LasyList list = Model.tool("book").all().lt("id"	,13);
//		for(Model m : list){
//			System.out.println(m);
//		}
		outputJSON(list);
	}
	
	public void lang(){
		String lng = getLang();
		output(lng);
	}
	
	public void fr(){
		Model m = Model.tool("book").get(1);
		put("obj",m);
	}
	
	public void outTwoTimes(){
		output("first out");
		output("first second times");
		
	}
	
	public void down(){
		download("/a.jpg");
	}
}
