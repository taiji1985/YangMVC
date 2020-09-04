package org.docshare.orm.mysql;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.mvc.except.MVCException;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;
import org.docshare.orm.StatementPool;
import org.docshare.util.TextTool;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

public class MySQLDBHelper extends DBHelper {

	//防止用户自己调用构造函数
	public MySQLDBHelper(){
		
	}
	@Override
	public void conn() {
		try {
			if(con !=null && ! con.isClosed() && con.isValid(5))return;
			statementPool.clear();//清空语句池
			
			Log.d("MySQLDBHelper connect to server");
			Class.forName("com.mysql.jdbc.Driver");
			//Class.forName("com.mysql.cj.jdbc.Driver");
//			String uri = String.format(
//					"jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&useSSL="+Config.useSSL,
//					Config.dbhost,Config.dbport, Config.dbname);
			String uri = TextTool.concat("jdbc:mysql://",Config.dbhost,":",Config.dbport,"/",Config.dbname,"?characterEncoding=utf-8&useSSL=",Config.useSSL,"&serverTimezone=Asia/Shanghai").toString();
//			String uri = String.format(
//					"jdbc:mysql://%s:%s/%s?characterEncoding=utf-8&serverTimezone=GMT+8&useSSL="+Config.useSSL,
//					Config.dbhost,Config.dbport, Config.dbname);
			con = DriverManager.getConnection(uri, Config.dbusr, Config.dbpwd);
			
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
	/**
	 * 执行sql语句。 该函数会发现连接断开错误并自动重试3次，如果重试三次都无法成功则抛出SQLException
	 */
	public ResultSet getRS(String sql, List<Object> params) throws SQLException {
		printParams(sql,params);
		PreparedStatement s ;
		if(con==null || con.isClosed())conn();
		int retry =0;
		while(retry<3){
			try {
				s= statementPool.get(con, sql);   //con.prepareStatement(sql);
				for(int i=0;i<params.size();i++){
					s.setObject(i+1, params.get(i));
				}
				return s.executeQuery();
			} catch (CommunicationsException e) {
				retry++;
				Log.i("reconnect db retry="+retry);
				conn();
			}
		}
		throw new SQLException("DB Connection is lost ,but can not reconnect ! ");
	}
	
	@Override
	public int updateWithArray(String sql, Object[] objs) {
		if(con==null )conn();
		int retry =0;
		while(retry<3){
			try{
				last_id = -1;
				PreparedStatement s = statementPool.get(con, sql);
				if(objs!=null)for(int i=0;i<objs.length;i++){
					s.setObject(i+1, objs[i]);
				}
				
				int ret  =  s.executeUpdate();
				ResultSet last = s.getGeneratedKeys();
				
				//ResultSet last = getRS("SELECT LAST_INSERT_ID()");
				if(last !=null && last.next()){
					last_id = last.getObject(1);
				}else{
					last_id = -1;
				}
				last.close();
				return ret;
			}catch(CommunicationsException e){
				retry++;
				Log.i("reconnect db retry="+retry);
				conn();
			}catch(SQLException e){
				throw new MVCException(e);
			}
		}
		return -1;
	}
	
	
	StatementPool statementPool =new StatementPool();
}
