package org.docshare.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.demo.pojo.Book;
import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

import junit.framework.TestCase;
 
public class TestPGSQL extends TestCase{
	public void setUp(){
		Config.dbhost="localhost";
		Config.dbport="5432";
		Config.dbusr="bbb";
		Config.dbpwd="123456";
		Config.dbname = "postgres";
		Config.dbtype = "postgres";
		Config.dbschema = "public";
	}
	public void testDBHelper(){
		try {
			DBHelper helper = DBHelper.getIns(DBHelper.DB_POSTGRES);
			helper.conn();
			ResultSet rs = helper.getRS("select * from book");
			while(rs.next()){
				String s = rs.getString("name");
				System.out.println(s);
			}
			HashMap<String, ColumnDesc> cc = helper.columeOfRs("select * from book", rs);
			System.out.println(JSON.toJSONString(cc));
			
			cc = helper.listColumn("book");
			System.out.println(JSON.toJSONString(cc));
			
			String key = helper.keyColumn("book");
			System.out.println(key);

			cc = helper.listColumn("catalog");
			System.out.println(JSON.toJSONString(cc));
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void testSelect(){
		DBTool tool = Model.tool("book");
		LasyList list = tool.all();
		Log.i(JSON.toJSONString(list));
	}
	public void testTables(){
		Log.i(DBHelper.getIns().getTables());
	}

	public void testPOJO(){
		//获取
		Book b = Book.findByKey(1);
		Log.i(b);
		//更新
		b.name = "测试"+new Date().getSeconds();
		b.update();
		
		//插入
		Book b2= new Book();
		b2.name = "haha";
		
		b2.catalog_id =1;
		b2.insert();
		
		Book b3= new Book();
		b3.name = "wuwu";
		
		b3.catalog_id =1;
		b3.insert();
		
		
		//删除
		Book.delByKey(2);
		b3.remove();
	}
}
