package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.docshare.log.Log;




public class DBTool {
	DBHelper helper = DBHelper.getIns();
	private String tname;
	private Map<String,Object> columns; //所有列的列名,及其值（值都为空）
	public Map<String, ColumnDesc> c_to_remarks;
	String key ;
	public DBTool(String tname){
		
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
	
	public String getColumnRemark(String column){
		return c_to_remarks.get(column).remark;
	}
	public int getColumnType(String column){
		return c_to_remarks.get(column).type;
		
	}
	public String getColumnTypeName(String column){
		ColumnDesc d = c_to_remarks.get(column);
		if(d == null){
			Log.d("getColumnTypeName find null "+ column);
			return "varchar";
		}
		return c_to_remarks.get(column).typeName;
		
	}
	public Set<String> listColumns(){
		return columns.keySet();
	}
	/**
	 * 根据用户自定义的sql来获取list
	 * @param sql
	 * @return 返回一个包含了该SQL结果的LasyList（并没有真正查询，在你读取数据时真正查询
	 */
	public LasyList fromSQL(String sql){
		return new LasyList(sql, this);
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
			rs = helper.getPrepareRS(String.format("select * from %s where %s = ? limit 0,1",tname,column),id);
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
	
	private String valueWrapper(String c,Object v){
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
		String s = v.toString();
		s = s.replace("'", "''");
		return "'"+s+"'";
	}

	/**
	 * 插入或保存数据。当m的主键为非空时，则为更新，主键为空是为插入。
	 * @param m
	 */
	public int save(Model m){
		return save(m,false);
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
			Log.e("can not save a null object");
			return -1;
		}
		Object id = m.get(key);
		String sql = "";
		if(id == null|| isInsert){
			//This is an insert
			String ks="";
			String vs="";
			boolean first=true;
			for(String k: m.keySet()){
				if(k.equals(key)){
					continue;
				}
				Object v = m.get(k);
				if(v == null || v.toString().length() == 0){
					continue;
				}
				if(!first){
					ks+=',';
					vs+=',';
				}
				
				ks+= "`"+k+"`";
				vs+= valueWrapper(k, v);
				first = false;
			}
			sql = String.format("insert into `%s`(%s) values(%s)", m.getTableName(),ks,vs);
		}else{
			ArrayList<String> sa=new ArrayList<String>();
			for(String k: m.keySet()){
				if(k == key)continue;
				Object v = m.get(k);
				if(v == null || v.toString().length() == 0){
					continue;
				}
				String s = k+"="+valueWrapper(k, m.get(k));
				sa.add(s);
			}
			String ss = ArrayTool.join(",", sa);
			sql=String.format("update `%s` set %s where %s", m.getTableName(),ss,ArrayTool.valueWrapper(key, id,getColumnTypeName("id")) );
		}
		Log.d("DBTool run sql: "+sql);
		int d = helper.update(sql);
		Log.d("return "+d);
		return helper.getLastId();
	}
	
	public LasyList all(){
		LasyList list = new LasyList("from `"+tname+"`", this);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public Model db2Table(ResultSet rs,Map<String,?> c){
		if(c == null){
			c = columns;
		}
		String key2 = null;
		Model tb = new Model(tname,(Map<String, Object>) c);
		tb.joined_tool = this;
		for(String key : c.keySet()){
			Object v=null;
			key2 = key;
			try {
				Object object = c.get(key);
				if(c!=columns && object instanceof ColumnDesc){
					ColumnDesc desc = (ColumnDesc) object;
					if(desc.tb!=null && ! desc.tb.equals(tname) && !tname.equals("rawsql")){
						key2 = desc.tb +"."+key;
						Log.e(key);
					}
				}
				v = rs.getObject(key);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				Log.e(e);
			}
			tb.put(key, v);
		}
		
		return tb;
	}
	
	public Model db2Table(ResultSet rs){
		return db2Table(rs,columns);
	}

	public void del(Object id) {
		String sql = String.format("delete from %s where %s = ?", tname,key);
		Log.d("DBTool run sql: "+sql);
		helper.update(sql,id);		
	}
	
	public void del(Model m){
		Object kv = m.get(key);		
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

	public ColumnDesc getColumnDesc(String column) {
		return c_to_remarks.get(column);
	}

	public int run(String sql,Object...objects){
		return helper.update(sql,objects);
	}


}
