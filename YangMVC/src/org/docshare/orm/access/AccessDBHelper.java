package org.docshare.orm.access;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;

public class AccessDBHelper extends DBHelper {
	/**
	 * 列举sql返回的所有的行
	 * @param sql sql语句
	 * @param rs 查询结果集
	 * @return 字段描述
	 */
	public HashMap<String,ColumnDesc> columeOfRs(String sql, ResultSet rs) {
			//对SQL语句，根据SQL作为key进行缓存
			HashMap<String, ColumnDesc> ret ;
	//		String sqlKey = sql.hashCode()+"";
	//		if(useCache && desc_cached!= null && desc_cached.containsKey(sqlKey)){
	//			ret =  desc_cached.get(sqlKey);
	//			if(ret.size() > 0){
	//				return ret;
	//			}
	//		}
			ret = new HashMap<String, ColumnDesc>();
			
			ResultSetMetaData m;
			try {
				m = rs.getMetaData();
				int c = m.getColumnCount();
				for(int i=1;i<=c;i++){
					String name =m.getColumnLabel(i);
					//Log.d("name = " +m.getColumnName(i) +", label = "+ name);
					//Object val = m.get
					String tb = m.getTableName(i);
					ColumnDesc cd = new ColumnDesc(name, m.getColumnType(i), m.getColumnLabel(i),tb);
					ret.put(name, cd);
				}
			} catch (SQLException e) {
				Log.e(e);
			}
			return ret;
	}
	@Override
	public void conn() {
		try {
		    Class.forName("com.hxtt.sql.access.AccessDriver").newInstance();    
		    String url = "jdbc:Access:///"+Config.dbname;
		    //建立连接
		    con = DriverManager.getConnection(url);
		    System.out.println("AccessDB opened "+Config.dbname);
		} catch (Exception e) {
		    Log.e(e);
		}
	}

}
