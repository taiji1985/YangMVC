package org.docshare.orm;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.mvc.MVCFilter;
import org.docshare.mvc.except.MVCException;
import org.docshare.orm.ColumnDesc.ExportTo;
import org.docshare.orm.access.AccessDBHelper;
import org.docshare.orm.mysql.MySQLDBHelper;
import org.docshare.orm.postgres.PostgresDBHelper;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;

import com.alibaba.fastjson.JSON;

public abstract class DBHelper {

	public static final String DB_MYSQL = "mysql";
	public static final String DB_POSTGRES = "postgres";
	
	
	static ThreadLocal<DBHelper> locals = new ThreadLocal<DBHelper>();

	/**
	 * 使用该函数获取DBHelper对象。 本类的设计为同一个线程公用一个DBHelper
	 * @return DBHelper对象
	 */
	public static DBHelper getIns(String type) {
		DBHelper ins = locals.get();
		if(ins == null || ! useCache){
			if(type == null  || type.equals("mysql")){
				ins = new MySQLDBHelper();	
			}else if(type.equals("postgres")){
				ins = new PostgresDBHelper();
			}else if(type.equals("access")){
				ins = new AccessDBHelper();
			}
			locals.set(ins);
		}
		return ins;
	}
	public static DBHelper getIns(){
		return getIns(Config.dbtype);
	}

	public static boolean useCache = true;

	public static void disableCache() { //用于Bae这种变态的环境。
		useCache = false;
	}

	public static void removeThreadLocal() {
		try{
			locals.remove();
		}catch (Exception e) {
			//Log.e(e);
		}
	}

	protected Connection con = null;
	protected Object last_id;

	/**
	 * 连接数据库
	 */
	public abstract void conn();

	/**
	 * 获取该表的外键信息
	 * @param table 表名
	 * @param map
	 * @throws SQLException
	 */
	public void getFKey(String table, HashMap<String,ColumnDesc> map) throws SQLException {
			conn();
			
			
			DatabaseMetaData dbmd = con.getMetaData();
			
			//导入键   --- 我的键是别人的主键
			ResultSet rs = dbmd.getImportedKeys(Config.dbname, null, table);
			
	//		ResultSetMetaData meta = rs.getMetaData();
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
			rs.close();
			//导出键值--- 我的主键是别人的外键。
			rs = dbmd.getExportedKeys(Config.dbname, null, table);
			while(rs.next()){
				ExportTo eTo = new ExportTo();
                String pkTableName = rs.getString("PKTABLE_NAME");//主键表名   
                String pkColumnName = rs.getString("PKCOLUMN_NAME");//主键列名    
                String fkTableName = rs.getString("FKTABLE_NAME");//外键表名  
                String fkColumnName = rs.getString("FKCOLUMN_NAME"); //外键列名    
                eTo.table = fkTableName;
                eTo.column = fkColumnName;
                ColumnDesc desc = map.get(pkColumnName);
				if(desc == null) continue;
				if(desc.exportKey==null){
					desc.exportKey = new ArrayList<ColumnDesc.ExportTo>();
				}
				desc.exportKey.add(eTo);
			}
			rs.close();
		}

	/**
	 * 根据sql和参数进行查询 
	 * @param sql SQL语句
	 * @param obj 参数
	 * @return
	 * @throws SQLException
	 */
	public ResultSet getPrepareRS(String sql, Object obj) throws SQLException {
		PreparedStatement s ;
		conn();
		Log.d("DBHelper"+" Exec : "+sql +",param1 = "+obj);
		s= con.prepareStatement(sql);
		s.setObject(1, obj);
		return s.executeQuery();
	}

	public ResultSet getPrepareRS(String sql, Object a, Object b)
			throws SQLException {
				PreparedStatement s ;
				conn();
				Log.d("DBHelper"+" Exec : "+sql +"param1 = "+a+",param2="+b);
				s= con.prepareStatement(sql);
				s.setObject(1, a);
				s.setObject(2, b);
				return s.executeQuery();
	}

	public ResultSet getRS(String sql) throws SQLException {
		Statement s;
		conn();
		s = con.createStatement();
		Log.d("DBHelper"+" Exec : "+sql);
		return s.executeQuery(sql);
	}
	
	public class TableInfo{
		public String name;
		public String schema;
		public String type;
	}
	public List<TableInfo> getTables(){
		conn();
		ArrayList<TableInfo> ret = new ArrayList<DBHelper.TableInfo>();
		try {
			ResultSet rs = con.getMetaData().getTables(Config.dbname, Config.dbschema, null, new String[]{"TABLE","VIEW"});
		
			while(rs.next()){
				TableInfo tInfo = new TableInfo();
				tInfo.name = rs.getString("TABLE_NAME");
				tInfo.schema = rs.getString("TABLE_SCHEM");
				tInfo.type = rs.getString("TABLE_TYPE");
				ret.add(tInfo);
			}
			rs.close();
		} catch (SQLException e) {
			throw new MVCException("Fail to get  meta data of tables", e);
		}
		return ret;
	}
	/**
	 * 列举sql返回的所有的行
	 * @param sql
	 * @param rs
	 * @return
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
				throw new MVCException("Fail to get  meta data of sql: "+sql, e);
			}
			return ret;
		}

	private Map<String, HashMap<String,ColumnDesc>> desc_cached = new HashMap<String, HashMap<String,ColumnDesc>>();

	public Object getLastId() {
			return last_id;
	}

	public int updateWithArray(String sql, Object[] objs) {
		try {
			conn();
			last_id = -1;
			PreparedStatement s = con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
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
			
		} catch (SQLException e) {
			//new MVCException();
			String msg = "exec sql fail "+ sql + " ,param = " +TextTool.join(objs, ",") ;
			throw new MVCException(msg,e);
		}
	}

	public int update(String sql, Object... objs) {
		return updateWithArray(sql,objs);
	}

	public void close() {
		try {
			if(con!=null){
				con.close();
			}
			con = null;
		} catch (SQLException e) {
		}
	}

	/**
	 *  是否需要尝试读取源文件根目录下的tbconfig目录下的表格式文件。
	 *  生成方法： 使用 java -jar yangmvc-1.9-all.jar 直接生成此目录，将tbconfig整个目录拷贝到src目录下即可。
	 *  如果不需要尝试读取ConfigFile可以将这个变量置为false
	 */
	public static boolean useConfigFile = true;

	/**
	 * 列举表中的所有字段，获取表结构
	 * @param tb
	 * @return
	 */
	public HashMap<String,ColumnDesc> listColumn(String tb) {
		return listColumn(tb,true);
	}

	/**
	 * 尝试从文件中存储预存的表结构 （仅应用与某些无法直接获取表结构的场景）
	 * @param tb
	 * @return
	 */
	public HashMap<String,ColumnDesc> readColumnDesc(String tb) {
	//		URL purl = getClass().getResource("/");
	//		if(purl == null){
	//			return null;
	//		}
	//		String path = purl.getPath()+"/tbconfig/"+tb+".json";
			String path = "/tbconfig/"+tb+".json";
			InputStream in = getClass().getResourceAsStream(path);
			
			String json = FileTool.readAll(in, "utf-8");
			if(json == null){
				Log.d("read columnSave not found "+path);
				return null;
			}
			Log.d("readed columnSave succ " + tb +",path="+path);
			ColumnSave save = JSON.parseObject(json, ColumnSave.class);
			return save.listColumn;
			
		}



	/**
	 * 返回表格中所有的列，以及列的注释（这个注释可以用于显示）
	 * @param tb 表格名称
	 * @return  列名->注释 的映射表
	 */
	public HashMap<String,ColumnDesc> listColumn(String tb, boolean useCache) {
		HashMap<String, ColumnDesc> ret ;
		if(useCache && desc_cached!= null && desc_cached.containsKey(tb)){
			ret =  desc_cached.get(tb);
			if(ret.size() > 0){
				return ret;
			}
		}
		
		if(useConfigFile){
			ret= readColumnDesc(tb);
			if(ret!=null ){
				desc_cached.put(tb, ret);
				return ret;
			}
		}
		
		conn();
		ret = new HashMap<String,ColumnDesc>();
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
			//Log.e(e);
			throw new MVCException("Fail to list columns of table "+tb,e);
		}
		
		return ret;
	}

	/**
	 * 获取表的主键
	 * @param tb
	 * @return
	 */
	public String keyColumn(String tb) {
		conn();
		ResultSet rs;
		String ret = null;

        try {
			rs = con.getMetaData().getPrimaryKeys(null, null, tb);
			if (rs.next()) {
				String columnName = rs.getString("COLUMN_NAME");
				ret = columnName;
				
			}
			rs.close();
			
		} catch (SQLException e) {
			Log.e(e);
			System.out.println("---------- 这个错误绝大多数情况不会影响程序的正常运行，错误原因：只是获取不到表"+tb+"的主键");
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
			throw new MVCException("Fail to getVal of column ["+column+"] from sql : "+ sql,e);
		}
	
		return ret;
	}

	public DBHelper() {
	}

	@Override
	protected void finalize() throws Throwable {
		Log.d("DBHelper cleared");
		close();
		
		super.finalize();
	}

	private void printParams(String sql, ArrayList<Object> params) {
		Log.d("PrintParams: "+sql +" params= {"+TextTool.join(params, ",")+"}");
		
	}

	public ResultSet getRS(String sql, ArrayList<Object> params) throws SQLException {
		printParams(sql,params);
		PreparedStatement s ;
		conn();
		
		s= con.prepareStatement(sql);
		for(int i=0;i<params.size();i++){
			s.setObject(i+1, params.get(i));
		}
		return s.executeQuery();
	}

	/**
	 * 启动事务 ， 所有提交在sql会在调用commit后被执行。
	 */
	public void beginTransation() {
		try {
			con.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

	public void commit() {
		try {
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		try {
			con.setAutoCommit(true);
		} catch (SQLException e) {
			throw new MVCException("Fail to commit ",e);
		}
	}

	public void rollback() {
			try {
				con.rollback();
				con.setAutoCommit(true);
			} catch (SQLException e) {
				throw new MVCException("Fail to rollback ",e);
			}
		}
	//	public void checkConn(){
	//		boolean valid = true;
	//		if(con == null){
	//			conn();
	//			return;
	//		}
	//		try {
	//			if(con.isClosed()){
	//				conn();
	//				return;
	//			}
	//			if(!con.isValid(200)){
	//				con.close();
	//				conn();
	//				return;
	//			}
	//		} catch (SQLException e) {
	//			Log.e(e);
	//			
	//		}
	//		
	//		
	//	}

}