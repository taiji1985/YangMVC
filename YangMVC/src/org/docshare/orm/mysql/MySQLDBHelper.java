package org.docshare.orm.mysql;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;

import com.alibaba.fastjson.JSON;







public class MySQLDBHelper extends DBHelper {

	//防止用户自己调用构造函数
	public MySQLDBHelper(){
		
	}
	@Override
	public void conn() {
		try {
			if(con !=null && ! con.isClosed() && con.isValid(200))return;
			Log.d("MySQLDBHelper connect to server");
			Class.forName("com.mysql.jdbc.Driver");
			//Class.forName("com.mysql.cj.jdbc.Driver");
			String uri = String.format(
					"jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&useSSL="+Config.useSSL,
					Config.dbhost,Config.dbport, Config.dbname);
//			String uri = String.format(
//					"jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&serverTimezone=GMT+8&useSSL="+Config.useSSL,
//					Config.dbhost,Config.dbport, Config.dbname);
			String user = Config.dbusr;
			String password = Config.dbpwd;
			con = DriverManager.getConnection(uri, user, password);
			
		} catch (ClassNotFoundException e) {
			Log.e(e);
		} catch (SQLException e) {
			Log.e(e);
			Log.e(Config.str());
		}
	}

	public static void main(String[] args) throws SQLException {
		Config.dbport="3308";
		Config.dbname="mvc_demo";
		DBHelper helper = DBHelper.getIns();
		helper.conn();
		// dh.listColumn("book");
		System.out.println(helper.keyColumn("book"));
		
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
		
	}
}
