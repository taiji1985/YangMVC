package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;

import com.alibaba.fastjson.JSON;

public class ConfigCreator {
	static Scanner sc =new Scanner(System.in);
	static String read(String msg,String def){
		System.out.print(msg+"默认为: ["+def+"]: ");
		String line = sc.nextLine();
		if(line.length() == 0) return def;
		else return line;
	}
	public void testRead(){
		String s = FileTool.readResource("tbconfig/book.json","utf-8");
		Log.i(s);
	}
	public static void main(String[] args) throws SQLException {
		System.out.print("请输入数据库信息，如与中括号中相同，可以直接打回车");
		Config.dbhost = read("请输入服务器域名或ip","localhost");
		Config.dbname = read("请输入数据库名","mvc_demo");
		Config.dbport = read("请输入端口号","3306");
		Config.dbpwd = read("请输入密码", "123456");
		Config.dbusr = read("请输入用户名", "root");
		
		
		
		
		DBHelper helper = DBHelper.getIns();
		ResultSet rs = helper.getRS("show tables; ");
		ArrayList<String> list =new ArrayList<String>();
		while (rs.next()) {
		    System.out.println(rs.getString(1));
		    list.add(rs.getString(1));
		}
		rs.close();
		FileTool.makeDir("tbconfig");
		for(String tb: list){
			HashMap<String,ColumnDesc> listColumn  = helper.listColumn(tb);
			String json = JSON.toJSONString(new ColumnSave(tb, listColumn));
			String fname = "tbconfig/"+tb+".json";
			FileTool.writeUTF(fname, json);
			System.out.println("write " + fname);
		}
		System.out.print("请输入需要生成的类的包名:");
		String pkg = sc.next();
		if(pkg.length() == 0 ){
			System.out.println("您没有输入包名，取消生成");
			return;
		}
		FileTool.makeDir("pojo");
		for(String tb :list){
			HashMap<String,ColumnDesc> listColumn  = helper.listColumn(tb);
			String clsName = tb2Name(tb);
			StringBuffer sb = new StringBuffer();
			sb.append("package "+pkg+";\n");
			sb.append("public class "+clsName+"{\n");
			for(String cname : listColumn.keySet()){
				ColumnDesc d = listColumn.get(cname);
				sb.append("\tpublic "+ d.javaType() +" " + d.name+";\n" );
			}
			sb.append("\tpublic void update(){\n\t\tT(\""+tb+"\").update(this);\n\t}\n");
			sb.append("\tpublic void insert(){\n\t\tT(\""+tb+"\").insert(this);\n\t}\n");
			
			sb.append("}");
			String fname= "pojo/"+clsName+".java";
			System.out.println(fname);
			FileTool.writeUTF(fname,sb.toString());
		}
		
	}
	public static String tb2Name(String tb){
		String[] ta  = tb.split("_");
		for(int i=0;i<ta.length;i++){
			ta[i] = TextTool.firstUpper(ta[i]);
		}
		String cls = TextTool.join(ta, "");
		return cls;
	}
}
