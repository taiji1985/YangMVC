package org.docshare.orm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.docshare.log.Log;
import org.docshare.orm.ArrayTool;
import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;

public class MySQLDelegate implements IDBDelegate {

	@Override
	public ResultSet resultById(DBHelper helper,String tname,String column,Object id) throws SQLException {
		ResultSet rs = helper.getPrepareRS(String.format("select * from `%s` where `%s` = ? limit 0,1",tname,column),id);
		return rs;
	}

	@Override
	public int save(DBTool tool,DBHelper helper,Model m,String key,boolean isInsert ){
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
				String type = tool.getColumnTypeName(k);
				vs+= ArrayTool.valueWrapper(k, v,type);
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
				String type = tool.getColumnTypeName(k);
				String s = k+"="+ArrayTool.valueWrapper(k, m.get(k),type);
				sa.add(s);
			}
			String ss = ArrayTool.join(",", sa);
			sql=String.format("update `%s` set %s where %s", m.getTableName(),ss,ArrayTool.valueWrapper(key, id,tool.getColumnTypeName("id")) );
		}
		Log.d("DBTool run sql: "+sql);
		int d = helper.update(sql);
		Log.d("return "+d);
		return helper.getLastId();
	}
	
	@Override
	public int delete(DBHelper helper,String tname,String key,Object id){
		String sql = String.format("delete from `%s` where `%s` = ?", tname,key);
		Log.d("DBTool run sql: "+sql);
		return helper.update(sql,id);
	}
	
	public String buildSQL(){
		
	}
}
