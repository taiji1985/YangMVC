package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.docshare.log.Log;
import org.docshare.orm.mysql.IDBDelegate;
import com.alibaba.fastjson.JSON;




public class DBTool {
	DBHelper helper = DBHelper.getIns();
	private String tname;
	private Map<String,Object> columns; //所有列的列名,及其值（值都为空）
	public Map<String, ColumnDesc> c_to_remarks;
	String key ;
	IDBDelegate delegate = DelegateFactory.getIns("mysql");
	public IDBDelegate getDelegate(){
		return delegate;
	}
	public DBTool(String tname){
		Log.d("create a DBTool of "+tname);
		this.tname = tname;
		if("rawsql".equals(tname)){
			c_to_remarks = new HashMap<String, ColumnDesc>();
		}else{
			c_to_remarks = helper.listColumn(tname);
		}
		//Log.map(c_to_remarks);
		columns = new HashMap<String, Object>();
		for(String s:c_to_remarks.keySet()){
			columns.put(s, null);
		}
		key = helper.keyColumn(tname);
	}
	/**
	 * 获取列的注释
	 * @param column 列名
	 * @return
	 */
	public String getColumnRemark(String column){
		return c_to_remarks.get(column).remark;
	}
	/**
	 * 获取列的类型（int类型）
	 * @param column
	 * @return
	 */
	public int getColumnType(String column){
		return c_to_remarks.get(column).type;
		
	}
	/**
	 * 获取列的类型名
	 * @param column
	 * @return
	 */
	public String getColumnTypeName(String column){
		ColumnDesc d = c_to_remarks.get(column);
		if(d == null){
			Log.d("getColumnTypeName find null "+ column +" reload column info ");
			c_to_remarks = helper.listColumn(tname,false);
			Log.d("reload data = "+ JSON.toJSONString(c_to_remarks));
			d = c_to_remarks.get(column);
			if(d == null){
				Log.d("getColumnTypeName is null again ,return varchar" + column );
			}
			return "varchar";
		}
		return d.typeName;
		
	}
	/**
	 * 列举所有的列名
	 * @return
	 */
	public Set<String> listColumns(){
		return columns.keySet();
	}
	/**
	 * 根据用户自定义的sql来获取list
	 * @param sql
	 * @return 返回一个包含了该SQL结果的LasyList（并没有真正查询，在你读取数据时真正查询
	 */
	public LasyList fromSQL(String sql){
		return LasyList.fromRawSql(sql);
	}

	/**
	 * 获取key为id的记录并返回模型。注意必须有主键，且主键为整数
	 * @param id 主键的值
	 * @return 符合条件的模型
	 * @throws SQLException
	 */
	public Model get(int id){
		return get(key,id);
	}
	/**
	 * 获取column列等于id的所有集合的第一个
	 * @param column
	 * @param id
	 * @return
	 */
	public Model get(String column, Object id) {
		ResultSet rs;
		try {
			rs = delegate.resultById(helper, tname, column, id);
			Model tb=null;
			if(rs.next()){
				tb = db2Table(rs);
			}
			rs.close();
			return tb;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Log.e(e);
		}
		
		return null;
		
	}
	
	public Model create(){
		Model model =new Model(tname, columns);
		model.joined_tool = this;
		
		return model;
	}
	
//	private String valueWrapper(String c,Object v){
//		String type = getColumnTypeName(c);
//		String r ;
//		if(type.contains("VAR") || type.contains("TEXT") || type.contains("DATE") || type.contains("TIME")){
//			if(v == null) r=  "null";
//			
//			String vv= v.toString();
//			if(vv.contains("'")){
//				vv = vv.replace("'", "''");
//			}
//			
//			r= "'"+v+"'";
//		}else{
//			if(v == null){
//				r=0+"";
//			}else{
//				r= v.toString();
//			}
//		}
//		return r;
////		String s = v.toString();
////		s = s.replace("'", "''");
////		return "'"+s+"'";
//	}

	/**
	 * 插入或保存数据。当m的主键为非空时，则为更新，主键为空是为插入。
	 * @param m
	 */
	public int save(Model m){
		return save(m,false);
	}
	public int save(Object obj,boolean isInsert ){
		Model  m = Model.fromObject(tname, obj);
		return save(m,isInsert);
	}
	public int save(Object obj){
		return save(obj,false);
	}
	/**
	 * 更新或保存数据。
	 * 当主键为null 或isInsert为真时， 执行插入数据
	 * 否则，执行更新操作。
	 * @param m
	 * @param isInsert 是否强制为插入操作
	 */
	public int save(Model m,boolean isInsert){
		return delegate.save(this, helper, m, key, isInsert);
	}
	
	/**
	 * 执行一次插入操作
	 * @param m
	 * @return 插入影响的行数
	 */
	public int insert(Model m){
		return save(m,true);
	}
	
	/**
	 * 获取表格所有行，这里采用了延迟加载技术，并不会真的查询所有的行，
	 * 可以调用返回的LasyList的过滤器添加查询条件，在读取数据时才真正进行查询。
	 * 如: tool.all().eq("id",12).gt("no",333) ; 相当于
	 * select * from tablea where id = 12 and no>333 
	 * @return LasyList对象
	 */
	public LasyList all(){
		LasyList list = new LasyList(this,tname);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	protected Model db2Table(ResultSet rs,Map<String,?> c){
		if(c == null){
			c = columns;
		}
		Model tb = new Model(tname,(Map<String, Object>) c);
		tb.joined_tool = this;
		for(String key : c.keySet()){
			Object v=null;
			try {
				v = rs.getObject(key);
			} catch (SQLException e) {
				Log.e(e);
			}
			tb.put(key, v);
		}
		
		return tb;
	}
	
	protected Model db2Table(ResultSet rs){
		return db2Table(rs,columns);
	}
	/**
	 * 根据表的主键删除表格的一行
	 * @param id
	 * @return
	 */
	public int del(Object id) {
		return delegate.delete(helper, tname, key, id);
	}
	
	/**
	 * 根据Model对象删除数据表的一行
	 * @param m
	 */
	public void del(Model m){
		Object kv = m.get(key);
		if(kv !=null){
			del(kv);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		Log.d("dbtool finalize called");
		try{
			DBHelper.removeThreadLocal();
		}catch(Exception e){
			
		}
		super.finalize();
	}
	/**
	 * 获取当前数据表的某一行的信息
	 * @param column
	 * @return
	 */
	public ColumnDesc getColumnDesc(String column) {
		return c_to_remarks.get(column);
	}

	public int run(String sql,Object...objects){
		Log.i("DBTool run :" +sql +"  param=["+ArrayTool.join(",", objects)+"]" );
		return helper.update(sql,objects);
	}
	
	static HashMap<String, DBTool> toolCache = new HashMap<String, DBTool>();
	public static DBTool getIns(String tname) {
		if("rawsql".equals(tname)){
			return new DBTool(tname);
		}
		DBTool ret ;
		if(toolCache.containsKey(tname)){
			ret=  toolCache.get(tname);
		}else{
			ret = new DBTool(tname);
			toolCache.put(tname, ret);
		}
		return ret;
	}


}
