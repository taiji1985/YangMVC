package org.docshare.yangmvc.orm.mysql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.mvc.except.MVCException;
import org.docshare.yangmvc.orm.DBHelper;
import org.docshare.yangmvc.orm.StatementPool;
import org.docshare.yangmvc.util.FileTool;

public class MySQLDBHelper extends DBHelper {
	private static final Logger logger = LoggerFactory.getLogger(MySQLDBHelper.class);


	//防止用户自己调用构造函数
	public MySQLDBHelper(){
		
	}
	IConnectionPool pool = null;
	/** 上次探活成功的时间，用于 db_valid_interval_ms 时间窗 */
	private long lastValidAt = 0;

	@Override
	public void conn() {
		String uri = null;
		try {
			long now = System.currentTimeMillis();
			if (con != null) {
				boolean closed;
				try {
					closed = con.isClosed();
				} catch (SQLException e) {
					closed = true;
				}
				if (!closed) {
					// 时间窗内直接复用：避免每条 SQL 前都 ping 一次（一次网络往返）
					if (now - lastValidAt < Config.db_valid_interval_ms) {
						return;
					}
					if (con.isValid(5)) {
						lastValidAt = now;
						return;
					}
				}
				// 连接已断/已关：先关掉再重建，避免泄漏底层 socket
				try {
					con.close();
				} catch (SQLException ignored) {
				}
				con = null;
			}
			statementPool.clear();//清空语句池（会 close 旧语句）
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
			lastValidAt = System.currentTimeMillis();

			logger.debug("MySQLDBHelper connect to server "+ pool.getClass().getName());
			update("set names "+Config.dbEncoding);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.error("", e);
		} catch (SQLException e) {
			logger.error("", e);
			logger.error(Config.str());
			throw new MVCException("connect to url fail: "+uri,e);
		}
	}

	/**
	 * 丢弃当前连接（通信异常时调用），下一次 conn() 会重建。
	 */
	private void invalidateConnection() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (Exception ignored) {
		}
		con = null;
		lastValidAt = 0;
		statementPool.clear();
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
				logger.info("reconnect db retry="+retry);
				invalidateConnection();
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
				logger.info("reconnect db retry="+retry);
				invalidateConnection();
				conn();
			}catch(SQLException e){
				throw new MVCException(e);
			}finally {
				FileTool.safelyClose(last);
			}
		}
		return -1;
	}

	/**
	 * 关闭连接：
	 * <ul>
	 *   <li>用了内置/自定义连接池且实现了 {@link IConnectionPool2} 时，归还给池；</li>
	 *   <li>否则直接 close。</li>
	 * </ul>
	 */
	@Override
	public void close() {
		Connection c = con;
		con = null;
		lastValidAt = 0;
		statementPool.clear();
		if (c == null) {
			return;
		}
		try {
			if (pool instanceof IConnectionPool2) {
				((IConnectionPool2) pool).release(c);
			} else {
				c.close();
			}
		} catch (Exception e) {
			logger.debug("close connection failed: {}", e.toString());
		}
	}

	StatementPool statementPool =new StatementPool();
}
