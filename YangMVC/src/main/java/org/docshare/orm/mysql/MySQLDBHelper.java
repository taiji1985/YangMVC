package org.docshare.orm.mysql;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.mvc.except.MVCException;
import org.docshare.orm.DBHelper;
import org.docshare.orm.StatementPool;
import org.docshare.util.FileTool;

public class MySQLDBHelper extends DBHelper {

	//防止用户自己调用构造函数
	public MySQLDBHelper(){
		
	}
	IConnectionPool pool = null;
	@Override
	public void conn() {
		String uri = null;
		try {
			if(con !=null && ! con.isClosed() && con.isValid(5))return;
			statementPool.clear();//清空语句池
			String connPool =Config.getProperty("conn_pool", null);
			if(pool == null){
				if(connPool == null || connPool.trim().isEmpty()){
					pool = new ConnectionPool();
				}else{
					Class<?> clazz = Class.forName(connPool);
					pool = (IConnectionPool)clazz.newInstance();
				}
			}
			
			con = pool.getConnection();

			Log.d("MySQLDBHelper connect to server "+ pool.getClass().getName());
			update("set names "+Config.dbEncoding);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			Log.e(e);
		} catch (SQLException e) {
			Log.e(e);
			Log.e(Config.str());
			throw new MVCException("connect to url fail: "+uri,e);
		}
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
		ResultSet last= null;
		while(retry<3){
			try{
				last_id = -1;
				PreparedStatement s = statementPool.get(con, sql);
				if(objs!=null)for(int i=0;i<objs.length;i++){
					s.setObject(i+1, objs[i]);
				}
				
				int ret  =  s.executeUpdate();
				last = s.getGeneratedKeys();
				
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
			}finally {
				FileTool.safelyClose(last);
			}
		}
		return -1;
	}
	
	
	StatementPool statementPool =new StatementPool();
}
