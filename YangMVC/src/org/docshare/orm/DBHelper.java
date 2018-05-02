package org.docshare.orm;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;

import com.alibaba.fastjson.JSON;




public class DBHelper {

	static ThreadLocal<DBHelper> locals=new ThreadLocal<DBHelper>();
	/**
	 * 使用该函数获取DBHelper对象。 本类的设计为同一个线程公用一个DBHelper
	 * @return DBHelper对象
	 */
	public static DBHelper getIns(){
		DBHelper ins = locals.get();
		if(ins == null || ! useCache){
			ins = new DBHelper();
			locals.set(ins);
		}
		return ins;
	}
	public static boolean useCache = true; //默认使用缓存，如果不使用，设为false
	public static void disableCache(){ //用于Bae这种变态的环境。
		useCache = false;
	}
	
	public static void removeThreadLocal(){
		try{
			locals.remove();
		}catch (Exception e) {
			//Log.e(e);
		}
	}
	//防止用户自己调用构造函数
	private DBHelper(){
		
	}
	
	private Connection con = null;

	public void conn() {
		try {
			if(con !=null && ! con.isClosed() && con.isValid(200))return;
			
			Class.forName("com.mysql.jdbc.Driver");

			String uri = String.format(
					"jdbc:mysql://%s:%s/%s?characterEncoding=utf-8",
					Config.dbhost,Config.dbport, Config.dbname);
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
	/**
	 * 获取该表的外键信息
	 * @param table
	 * @param map
	 * @throws SQLException
	 */
	private void getFKey(String table ,HashMap<String,ColumnDesc> map) throws SQLException{
		conn();
		
		DatabaseMetaData dbmd = con.getMetaData();
		ResultSet rs = dbmd.getImportedKeys(Config.dbname, "%", table);
		
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
		
	}
	public ResultSet getPrepareRS(String sql,Object obj) throws SQLException{
		PreparedStatement s ;
		conn();
		Log.d("DBHelper"+" Exec : "+sql +",param1 = "+obj);
		s= con.prepareStatement(sql);
		s.setObject(1, obj);
		return s.executeQuery();
	}
	public ResultSet getPrepareRS(String sql,Object a,Object b) throws SQLException{
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
	public HashMap<String,ColumnDesc> columeOfRs(ResultSet rs){
		HashMap<String, ColumnDesc> ret = new HashMap<String, ColumnDesc>();
		
		ResultSetMetaData m;
		try {
			m = rs.getMetaData();
			int c = m.getColumnCount();
			for(int i=1;i<=c;i++){
				String name =m.getColumnName(i);
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
	private Object last_id;
	public Object getLastId(){
		return last_id;
	}
//	public int update(String sql) {
//		return update(sql,null);
//	}

	public int updateWithArray(String sql,Object[] objs){
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
			Log.e(msg);
			Log.e(e);
		}
		return 0;
	}
	public int update(String sql,Object... objs) {
		return updateWithArray(sql,objs);
	}
	public void close() {
		try {
			if(con!=null){
				con.close();
			}
			con = null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//Log.e(e);
		}
	}
	private Map<String, HashMap<String,ColumnDesc>> desc_cached = new HashMap<String, HashMap<String,ColumnDesc>>();

	public HashMap<String,ColumnDesc> listColumn(String tb) {
		return listColumn(tb,true);
	}
	public HashMap<String,ColumnDesc> readColumnDesc(String tb){
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
	 *  是否需要尝试读取源文件根目录下的tbconfig目录下的表格式文件。
	 *  生成方法： 使用 java -jar yangmvc-1.9-all.jar 直接生成此目录，将tbconfig整个目录拷贝到src目录下即可。
	 *  如果不需要尝试读取ConfigFile可以将这个变量置为false
	 */
	public static boolean useConfigFile = true;
	/**
	 * 返回表格中所有的列，以及列的注释（这个注释可以用于显示）
	 * @param tb 表格名称
	 * @return  列名->注释 的映射表
	 */
	public HashMap<String,ColumnDesc> listColumn(String tb,boolean useCache) {
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
			// TODO Auto-generated catch block
			Log.e(e);
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
			Log.e(e);
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
			Log.e(e);
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
	private void printParams(String sql,ArrayList<Object> params){
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
