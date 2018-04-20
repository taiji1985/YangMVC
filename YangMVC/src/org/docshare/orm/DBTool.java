package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.docshare.log.Log;
import org.docshare.util.BeanUtil;

import com.alibaba.fastjson.JSON;

/**
 * ORM类库最核心的类。 负责数据库表的查询。所有数据库查询均需先创建此类。 <br />
 * 创建方法: <br />
 * DBTool tool = DBTool.getIns("book"); //book为数据库表名<br />
 * LasyList list = tool.all().limit(10); //查询数据库的前10行， all()并非获取了所有列，当添加完参数，真正读取时才进行查询。它实际上是一个SQL语句的Builder
 * 得到LasyList可以使用像ArrayList一样使用了。
 * 
 * @author Tongfeng Yang
 *
 */



public class DBTool {
	private String tname;
	private HashMap<String,Object> columns; //所有列的列名,及其值（值都为空）
	public Map<String, ColumnDesc> c_to_remarks;
	String key ;
	IDBDelegate delegate = DelegateFactory.getIns("mysql");
	//DBHelper helper = DBHelper.getIns();
	public IDBDelegate getDelegate(){
		return delegate;
	}
	private DBTool(String tname){
		Log.d("create a DBTool of "+tname);
		this.tname = tname;
		if("rawsql".equals(tname)){
			c_to_remarks = new HashMap<String, ColumnDesc>();
		}else{
			c_to_remarks = delegate.listColumn(tname,true);
		}
		//Log.map(c_to_remarks);
		columns = new HashMap<String, Object>();
		for(String s:c_to_remarks.keySet()){
			columns.put(s, null);
		}
		key = delegate.keyColumn(tname);
	}
	/**
	 * 获取列的注释
	 * @param column 列名
	 * @return 指定列的列名
	 */
	public String getColumnRemark(String column){
		return c_to_remarks.get(column).remark;
	}
	/**
	 * 获取列的类型（int类型）
	 * @param column
	 * @return 类型
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
			c_to_remarks = delegate.listColumn(tname,false);
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
	 * @return 所有列名的集合
	 */
	public Set<String> listColumns(){
		return columns.keySet();
	}
	/**
	 * 根据用户自定义的sql来获取list
	 * 如果需要使用原生的SQL，请使用LasyList.fromRawSQL实现
	 * 
	 * @param sql
	 * @return 返回一个包含了该SQL结果的LasyList（并没有真正查询，在你读取数据时真正查询
	 */
	@Deprecated
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
	 * @return 找到列column等于id的对象
	 */
	public Model get(String column, Object id) {
		ResultSet rs;
		try {
			rs = delegate.resultById(tname, column, id);
			Model tb=null;
			if(rs.next()){
				tb = db2Table(rs);
			}
			rs.close();
			return tb;
		} catch (SQLException e) {
			Log.e(e);
		}
		
		return null;
		
	}
	
	public Model create(){
		Model model =new Model(tname, columns);
		model.isCreated = true;
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
		int r =  save(m,isInsert);
		if(r!=0){ //将获得的主键返回
			String key  = m.key();
			BeanUtil.set(obj, key, m.get(key));
		}
		return r;
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
		if(m == null){
			Log.e("DBTool save a null model "+ m +", tname is "+tname);
			return 0;
		}
		int r =  delegate.save(this, m, key, isInsert);
		m.isCreated = false;
		return r;
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
	public Model db2Table(ResultSet rs,Map<String,?> c){
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
			tb.innerPut(key, v);
		}
		
		return tb;
	}
	
	private Model db2Table(ResultSet rs){
		return db2Table(rs,columns);
	}
	/**
	 * 根据表的主键删除表格的一行
	 * @param id
	 * @return 影响的数据库的行数
	 */
	public int del(Object id) {
		return delegate.delete(tname, key, id);
	}
	
	/**
	 * 根据Model对象删除数据表的一行
	 * @param m
	 */
	public int del(Model m){
		Object kv = m.get(key);
		if(kv !=null){
			return del(kv);
		}
		return 0;
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
	 * @return 获取column列描述信息
	 */
	public ColumnDesc getColumnDesc(String column) {
		return c_to_remarks.get(column);
	}

	public int run(String sql,Object...objects){
		Log.i("DBTool run :" +sql +"  param=["+ArrayTool.join(",", objects)+"]" );
		return DBHelper.getIns().update(sql,objects);
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
