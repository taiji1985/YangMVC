package org.docshare.orm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.TextTool;
import org.docshare.orm.ArrayTool;
import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;
import org.docshare.orm.SQLConstains;


public class MySQLDelegate implements IDBDelegate {

	@Override
	public ResultSet resultById(DBHelper helper,String tname,String column,Object id) throws SQLException {
		ResultSet rs = helper.getPrepareRS(String.format("select * from `%s` where `%s` = ? limit 0,1",tname,column),id);
		return rs;
	}

	@Override
	public int save(DBTool tool,DBHelper helper,Model m,String key,boolean forceInsert ){
		if(m == null){
			Log.e("can not save a null object");
			return 0;
		}
		Object id = m.get(key);
		String sql = "";

		ArrayList<Object> plist = new ArrayList<Object>(); //参数列表
		if(id == null|| forceInsert){
			//This is an insert
			String ks="";
			String vs2="";
			boolean first=true;
			for(String k: m.keySet()){
				if(k.equals(key)){ //这里不再跳过主键字段
					//continue;    //不跳过主键字段了
				}
				Object v = m.get(k);
				if(v == null || v.toString().length() == 0){
					continue;
				}
				if(!first){
					ks+=',';
					vs2+=",";
				}
				
				ks+= "`"+k+"`";
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(null, v,type);
				vs2+="?";
				plist.add(v);
				first = false;
			}
			sql = String.format("insert into `%s`(%s) values(%s)", m.getTableName(),ks,vs2);
		}else{
			ArrayList<String> sa=new ArrayList<String>();
			for(String k: m.keySet()){
				if(k == key)continue;
				Object v = m.get(k);
				if(v == null || v.toString().length() == 0){
					continue;
				}
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(k, m.get(k),type);
				//sa.add(s);
				sa.add(k+"=?");
				plist.add(m.get(k));
			}
			String ss = ArrayTool.join(",", sa);
			sql=String.format("update `%s` set %s where %s", m.getTableName(),ss,ArrayTool.valueWrapper(key, id,tool.getColumnTypeName("id")) );
		}
		Log.d("DBTool run sql: "+sql+"  params=["+ArrayTool.join(",", plist)+"]");
		Object[] objs = plist.toArray();
		int d = helper.updateWithArray(sql,objs);
		Log.d("return "+d);
		if(d != 0 &&(id == null|| forceInsert)){
			id = helper.getLastId();			
			m.put(key, id);
		}
		return d;//helper.getLastId();
	}
	
	@Override
	public int delete(DBHelper helper,String tname,String key,Object id){
		String sql = String.format("delete from `%s` where `%s` = ?", tname,key);
		Log.d("DBTool run sql: "+sql +" ,param  = "+id);
		return helper.update(sql,id);
	}

	public ResultSet runSQL(List<SQLConstains> cons,DBTool tool,String tbName){
		return runSQL(cons, tool, tbName,"*");
	}

	@Override
	public long size(List<SQLConstains> cons, DBTool tool, String tbName) {
		ResultSet rs;
		try {
			rs = runSQL(cons, tool, tbName,"count(*) as CT");
			if(rs.next()){
				long id = rs.getLong("CT");
				rs.close();
				return id;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
	public ResultSet runSQL(List<SQLConstains> cons,DBTool tool,String tbName,String prefix){
		if(tbName == null) return null ;//参数检查，表名不能为空
		
		ArrayList<String> sa = new ArrayList<String>();
		ArrayList<Object> params = new ArrayList<Object>();
		final String[] fh = {"","=",">","<",">=","<=","<>"};
		SQLConstains limitc=null;
		SQLConstains orderc=null;
		for(SQLConstains c: cons){
			if(c.type<fh.length){
				//String w = ArrayTool.valueWrapper(null, c.value, tool.getColumnTypeName(c.column));
				//sa.add(String.format("`%s` %s %s", c.column,fh[c.type],w));
				sa.add(String.format("`%s` %s ?", c.column,fh[c.type]));
				params.add(c.value);
				
				continue;
			}
			switch(c.type){
			case SQLConstains.TYPE_LIKE:
				//String w = String.format("  `%s` like '$%s$' ", c.column, c.value).replace("$","%");
				//sa.add(w);
				String w = String.format("  `%s` like '$%s$' ", c.column, "?").replace("$","%");
				sa.add(w);
				params.add(c.value);
				
				break;
			case SQLConstains.TYPE_LIMIT:
				limitc = c;
				break;
			case SQLConstains.TYPE_ORDER:
				orderc = c;
				break;
			}
		}
		
		String tail ="";
		if(orderc!=null){
			//tail += String.format(" order by %s %s", orderc.column,(Boolean)orderc.value?"asc":"desc");
			tail += String.format(" order by %s %s", orderc.column , (Boolean)orderc.value?"asc":"desc");
			//params.add((Boolean)orderc.value?"asc":"desc");
			
		}
		if( limitc!=null){
			//tail += String.format(" limit %d,%d", limitc.value,limitc.value2);
			tail += " limit ?,?";
			params.add(limitc.value);
			params.add(limitc.value2);
		}
		
		DBHelper helper = DBHelper.getIns();
		String c=  TextTool.join2(sa, " and ") +  tail;
		if(c.trim().length() == 0){ //如果没有任何条件，则直接查询
			
			try {
				return helper.getRS("select "+prefix+" from " + tbName);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}else{
			c = c.trim();
			if(!c.startsWith("limit") && ! c.startsWith("order")){
				c  = " where " +c ;
			}else c = " " +c;
			String sql = "select "+prefix+" from "+tbName +c;
			try {
				return helper.getRS(sql,params);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	@Deprecated
	public String buildSQLWithoutLimit(List<SQLConstains> cons,DBTool tool){
		return buildSQL(cons, tool,false,null);
	}
	@Deprecated
	@Override
	public String buildSQL(List<SQLConstains> cons,DBTool tool,String sqlfrom){
		return buildSQL(cons, tool,true,sqlfrom);
	}
	
	@Deprecated
	public String buildSQL(List<SQLConstains> cons,DBTool tool,boolean withLimit,String sqlfrom){
		ArrayList<String> sa = new ArrayList<String>();
		final String[] fh = {"","=",">","<",">=","<=","<>"};
		SQLConstains limitc=null;
		SQLConstains orderc=null;
		for(SQLConstains c: cons){
			if(c.type<fh.length){
				String w = ArrayTool.valueWrapper(null, c.value, tool.getColumnTypeName(c.column));
				sa.add(String.format("`%s` %s %s", c.column,fh[c.type],w));
				continue;
			}
			switch(c.type){
			case SQLConstains.TYPE_LIKE:
				String w = String.format("  `%s` like '$%s$' ", c.column, c.value).replace("$","%");
				sa.add(w);
				break;
			case SQLConstains.TYPE_LIMIT:
				limitc = c;
				break;
			case SQLConstains.TYPE_ORDER:
				orderc = c;
				break;
			}
		}
		
		String tail ="";
		if(orderc!=null){
			tail += String.format(" order by %s %s", orderc.column,(Boolean)orderc.value?"asc":"desc");
		}
		if(withLimit && limitc!=null){
			tail += String.format(" limit %d,%d", limitc.value,limitc.value2);
		}
		
		
		String c=  TextTool.join2(sa, " and ") +  tail;
		if(sqlfrom == null) return c;
		sqlfrom = sqlfrom+ " ";
		if(c.trim().length() == 0){
			return sqlfrom;
		}else{
			c = c.trim();
			if(sqlfrom.contains("where")){
				if(c.startsWith("limit") || c.startsWith("order")){
					return sqlfrom + c;
				}else return  sqlfrom +" and " + c;
			}else{
				if(c.startsWith("limit")|| c.startsWith("order")){
					return sqlfrom + c ;
				}else {
					return  sqlfrom+ " where " + c;
				}
			}
		}
		
		
	}

}
