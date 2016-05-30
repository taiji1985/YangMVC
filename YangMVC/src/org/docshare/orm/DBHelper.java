package org.docshare.orm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.docshare.log.Log;
import org.docshare.mvc.Config;




public class DBHelper {

	static ThreadLocal<DBHelper> locals=new ThreadLocal<DBHelper>();
	/**
	 * 使用该函数获取DBHelper对象。 本类的设计为同一个线程公用一个DBHelper
	 * @return DBHelper对象
	 */
	public static DBHelper getIns(){
		DBHelper ins = locals.get();
		if(ins == null){
			ins = new DBHelper();
			locals.set(ins);
		}
		return ins;
	}
	
	public static void removeThreadLocal(){
		try{
			locals.remove();
		}catch (Exception e) {
			//e.printStackTrace();
		}
	}
	//防止用户自己调用构造函数
	private DBHelper(){
		
	}
	
	Connection con = null;

	public void conn() {
		try {
			if(con !=null && ! con.isClosed())return;
			
			Class.forName("com.mysql.jdbc.Driver");

			String uri = String.format(
					"jdbc:mysql://%s/%s?characterEncoding=utf-8",
					Config.dbhost, Config.dbname);
			String user = Config.dbusr;
			String password = Config.dbpwd;
			con = DriverManager.getConnection(uri, user, password);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

	}
	/**
	 * 获取该表的外键信息
	 * @param table
	 * @param map
	 * @throws SQLException
	 */
	private void getFKey(String table ,HashMap<String,ColumnDesc> map) throws SQLException{
		if(con==null){
			conn();
		}
		
		DatabaseMetaData dbmd = con.getMetaData();
		ResultSet rs = dbmd.getImportedKeys(Config.dbname, "%", table);
		
		ResultSetMetaData meta = rs.getMetaData();
//		Log.d("column count "+meta.getColumnCount());
//		for(int i=1;i<=meta.getColumnCount();i++){
//			Log.d(meta.getColumnName(i));
//		}
		while(rs.next()){
			String pk_table = rs.getString("PKTABLE_NAME");
			String pk_column = rs.getString("PKCOLUMN_NAME");
			String my_column = rs.getString("FKCOLUMN_NAME");
			ColumnDesc desc = map.get(my_column);
			if(desc == null) continue;
			
			desc.pk_column = pk_column;
			desc.pk_table = pk_table;
		}
		
	}
	public ResultSet getPrepareRS(String sql,Object obj) throws SQLException{
		PreparedStatement s ;
		conn();
		Log.d("DBHelper"+this.hashCode()+" exec sql : "+sql +",param1 = "+obj);
		s= con.prepareStatement(sql);
		s.setObject(1, obj);
		return s.executeQuery();
	}
	public ResultSet getPrepareRS(String sql,Object a,Object b) throws SQLException{
		PreparedStatement s ;
		conn();
		Log.d("DBHelper"+this.hashCode()+" exec sql : "+sql +"param1 = "+a+",param2="+b);
		s= con.prepareStatement(sql);
		s.setObject(1, a);
		s.setObject(2, b);
		return s.executeQuery();
	}
	
	
	public ResultSet getRS(String sql) throws SQLException {
		Statement s;
		conn();
		s = con.createStatement();
		Log.d("DBHelper"+this.hashCode()+" exec sql : "+sql);
		return s.executeQuery(sql);
	}

	public int update(String sql) {
		Statement s;
		try {
			conn();
			s = con.createStatement();
			return s.executeUpdate(sql);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}
	public int update(String sql,Object obj) {
		
		try {
			conn();
			PreparedStatement s = con.prepareStatement(sql);
			s.setObject(1, obj);
			return s.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}
	public void close() {
		try {
			if(con!=null){
				con.close();
			}
			con = null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	Map<String, HashMap<String,ColumnDesc>> desc_cached = new HashMap<String, HashMap<String,ColumnDesc>>();
	/**
	 * 返回表格中所有的列，以及列的注释（这个注释可以用于显示）
	 * @param tb 表格名称
	 * @return  列名->注释 的映射表
	 */
	public HashMap<String,ColumnDesc> listColumn(String tb) {
		if(desc_cached.containsKey(tb)){
			return desc_cached.get(tb);
		}
		
		conn();
		HashMap<String,ColumnDesc> ret = new HashMap<String,ColumnDesc>();
		try {
			ResultSet rs = con.getMetaData().getColumns(null, "%", tb, "%");
			while (rs.next()) {
				ColumnDesc desc=new ColumnDesc();
				
				desc.name = rs.getString("COLUMN_NAME");
				desc.remark = rs.getString("REMARKS");//列描述  
				desc.type = rs.getInt("DATA_TYPE");//列描述  
				desc.typeName = rs.getString("TYPE_NAME");
				if(desc.remark!=null&&desc.remark.length()==0){
					desc.remark = null;
				}
				//System.out.println("type "+desc.type +","+desc.name);
				ret.put(desc.name, desc);
			}
			getFKey(tb,ret);
			desc_cached.put(tb, ret);
		
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}

	public String keyColumn(String tb) {
		conn();
		ResultSet rs;
		String ret = "ss";
		try {
			rs = con.getMetaData().getIndexInfo(Config.dbname, null, tb, true,
					false);
			while (rs.next()) {
				String name = rs.getString("INDEX_NAME");
				if (name.equals("PRIMARY")) {
					ret = rs.getString("COLUMN_NAME");
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public int getVal(String sql, String column) {
		ResultSet rs;
		int ret = 0;
		try {
			rs = getRS(sql);
			if (rs.next()) {
				ret = rs.getInt(column);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public static void main(String[] args) {

		DBHelper dh = new DBHelper();
		dh.conn();
		// dh.listColumn("book");
		System.out.println(dh.keyColumn("book"));
	}
	
	@Override
	protected void finalize() throws Throwable {
		Log.d("DBHelper cleared");
		close();
		
		super.finalize();
	}
}
