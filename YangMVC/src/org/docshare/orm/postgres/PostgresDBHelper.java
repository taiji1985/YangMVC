package org.docshare.orm.postgres;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;

import com.alibaba.fastjson.JSON;







public class PostgresDBHelper extends DBHelper {

	//防止用户自己调用构造函数
	public PostgresDBHelper(){
		
	}
	@Override
	public void conn() {
		try {
			if(con !=null && ! con.isClosed() && con.isValid(200))return;
			
			Class.forName("org.postgresql.Driver");
	
			String uri = String.format(
					"jdbc:postgresql://%s:%s/%s?characterEncoding=utf-8&useSSL="+Config.useSSL,
					Config.dbhost,Config.dbport, Config.dbname);
			con = DriverManager.getConnection(uri, Config.dbusr, Config.dbpwd);
			
		} catch (ClassNotFoundException e) {
			Log.e(e);
		} catch (SQLException e) {
			Log.e(e);
			Log.e(Config.str());
		}
	
	}
	/**
	 * 获取该表的外键信息
	 * @param table 表名
	 * @param map
	 * @throws SQLException
	 */
//	public void getFKey(String table, HashMap<String,ColumnDesc> map) throws SQLException {
//			conn();
//			
//			DatabaseMetaData dbmd = con.getMetaData();
//			
//			ResultSet rs = dbmd.getImportedKeys(Config.dbname, null, table);
//			
//	//		ResultSetMetaData meta = rs.getMetaData();
//	//		Log.d("column count "+meta.getColumnCount());
//	//		for(int i=1;i<=meta.getColumnCount();i++){
//	//			Log.d(meta.getColumnName(i));
//	//		}
//			while(rs.next()){
//				String pk_table = rs.getString("PKTABLE_NAME");
//				String pk_column = rs.getString("PKCOLUMN_NAME");
//				String my_column = rs.getString("FKCOLUMN_NAME");
//				ColumnDesc desc = map.get(my_column);
//				if(desc == null) continue;
//				
//				desc.pk_column = pk_column;
//				desc.pk_table = pk_table;
//			}
//			
//		}

}
