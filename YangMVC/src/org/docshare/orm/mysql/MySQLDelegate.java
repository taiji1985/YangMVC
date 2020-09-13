package org.docshare.orm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.docshare.log.Log;
import org.docshare.mvc.except.MVCException;
import org.docshare.orm.ArrayTool;
import org.docshare.orm.ColumnDesc;
import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.IDBDelegate;
import org.docshare.orm.Model;
import org.docshare.orm.SQLConstains;
import org.docshare.util.TextTool;


public class MySQLDelegate implements IDBDelegate {
	public Map<String, ColumnDesc> c_to_remarks;
	@Override
	public ResultSet resultById(String tname,String column,Object id) throws SQLException {
		//String.format("select * from `%s` where `%s` = ? limit 0,1",tname,column)
		StringBuffer sBuffer =new StringBuffer();
		sBuffer.append("select * from `");
		sBuffer.append(tname);
		sBuffer.append("` where `");
		sBuffer.append(column);
		sBuffer.append("` = ? limit 0,1");
		//"select * from `"+tname+"` where `"+column+"` = ? limit 0,1" 
		ResultSet rs = DBHelper.getIns().getPrepareRS(sBuffer.toString(),id);
		return rs;
	}

	@Override
	public int save(DBTool tool,Model m,String key,boolean forceInsert ){
		if(m == null){
			Log.e("can not save a null object");
			return 0;
		}
		Object id = m.get(key);
		String sql = "";
		
		ArrayList<Object> plist = new ArrayList<Object>(8); //参数列表
		if(forceInsert || m.isCreated || id == null || (id instanceof Integer && (Integer)id <= 0 ) ){
			//This is an insert
			StringBuffer ks=new StringBuffer();
			
			StringBuffer vs2= new StringBuffer();
			boolean first=true;
			for(String k: m.keySet()){
				if(k.equals(key)){ //这里不再跳过主键字段
					//continue;    //不跳过主键字段了
				}
				Object v = m.get(k);
				if(v == null ){
					continue;
				}
				if(!first){
					ks.append(',');
					vs2.append(',');
				}
				//  ks += "`"+k+"`"; 被优化
				ks.append('`');
				ks.append(k);
				ks.append('`');
				
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(null, v,type);
				vs2.append('?');
				plist.add(v);
				first = false;
			}
			//sql = String.format("insert into `%s`(%s) values(%s)", m.getTableName(),ks.toString(),vs2.toString());
			StringBuffer sqlb = new StringBuffer();
			sqlb.append("insert into `");
			sqlb.append(m.getTableName());
			sqlb.append("`(");
			sqlb.append(ks);
			sqlb.append(") values(");
			sqlb.append(vs2);
			sqlb.append(")");
			sql = sqlb.toString();
		}else{
			int csize = m.changeColumns().size();
			if(csize == 0){
				Log.i("no change data for update "+sql);
				return 0;
			}

			ArrayList<String> sa=new ArrayList<String>(csize);
			for(String k: m.changeColumns()){
				if(k == key)continue;
				Object v = m.get(k);
				if(v == null ){
					continue;
				}
				String type = tool.getColumnTypeName(k);
				//ArrayTool.valueWrapper(k, m.get(k),type);
				//sa.add(s);
				sa.add("`"+k+"`=?");
				plist.add(m.get(k));
			}
			String ss = ArrayTool.join(",", sa);
			//sql=String.format("update `%s` set %s where %s", m.getTableName(),ss,ArrayTool.valueWrapper(key, id,tool.getColumnTypeName(key)) );
			StringBuffer sqlb = new StringBuffer();
			sqlb.append("update `");
			sqlb.append(m.getTableName());
			sqlb.append("` set ");
			sqlb.append(ss);
			sqlb.append(" where ");
			sqlb.append(ArrayTool.valueWrapper(key, id,tool.getColumnTypeName(key)));
			sql = sqlb.toString();
		}
		Log.d("DBTool run sql: "+sql+"  params=["+ArrayTool.joinWithLengthLimit(",", plist,20)+"]");
		Object[] objs = plist.toArray();
		DBHelper helper = DBHelper.getIns("mysql");
		
		int d = helper.updateWithArray(sql,objs);
		Log.d("return "+d);
		if(d != 0 &&(id == null|| forceInsert)){
			id = helper.getLastId();			
			m.put(key, id);
		}
		return d;//helper.getLastId();
	}
	
	@Override
	public int delete(String tname,String key,Object id){
		//String sql = String.format("delete from `%s` where `%s` = ?", tname,key);
		StringBuffer sBuffer =new StringBuffer();
		sBuffer.append("delete from `");
		sBuffer.append(tname).append("` where `").append(key).append("` = ?");
		String sql = sBuffer.toString();
		Log.d("DBTool run sql: "+ sql +" ,param  = "+id);
		return DBHelper.getIns().update(sql,id);
	}

//	public ResultSet runSQL(List<SQLConstains> cons,SQLConstains limit_con,SQLConstains order_con,DBTool tool,String tbName){
//		return runSQL(cons,limit_con,order_con, tool, tbName,"*");
//	}

	@Override
	public long size(List<SQLConstains> cons, DBTool tool, String tbName) {
		ResultSet rs;
		try {
			rs = runSQL(cons, null,null,tool, tbName,"count(*) as CT");
			if(rs!=null){
				if(rs.next()){
					long id = rs.getLong("CT");
					rs.close();
					return id;
				}
				rs.close();
			}else{
				
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new MVCException("size: get size of query error", e);
		}
		return 0;
	}
	@Override

	public ResultSet runSQL(List<SQLConstains> cons,SQLConstains order_con,SQLConstains limit_con,DBTool tool,String tbName,String prefix){

		StringBuffer sb=new StringBuffer();
		sb.append("select ").append(prefix).append(" from `").append(tbName).append("` ");
		final String[] fh = {"","=",">","<",">=","<=","<>"};
		ArrayList<Object> params = new ArrayList<Object>();
		if(cons.size()>0){
			sb.append("where ");
			boolean first= true;

			for(SQLConstains c: cons){
				if(!first){ 
					sb.append(" and ");
				}
				first = false;
				
				if(c.type<fh.length){
					//sa.add(String.format("`%s` %s ?", c.column,fh[c.type]));
					sb.append("`").append(c.column).append("` ").append(fh[c.type]).append(" ?");
					params.add(c.value);
					continue;
				}
				switch(c.type){
				case SQLConstains.TYPE_ISNULL:
					//w = String.format(" `%s` is NULL ", c.column);
					sb.append(" `").append(c.column).append("` is NULL ");
					break;
				case SQLConstains.TYPE_LIKE:
					
					sb.append(" `").append(c.column).append("` like ? ");
					params.add("%"+c.value+"%");
					
					break;
				case SQLConstains.TYPE_MLIKE:
					String[] ca = c.column.split(",");
					//String t  = "(" +String.format("`%s` like ?",ca[0]);
					sb.append("(`").append(ca[0]).append("` like ?");
					params.add("%"+c.value+"%");
					for(int i=1;i<ca.length;i++){
						sb.append(" or `").append(ca[i]).append("` like ?");
						params.add("%"+c.value+"%");
					}
					sb.append(") ");
					break;
				case SQLConstains.TYPE_CUSTOM:
					sb.append(' ');
					sb.append(c.column);
					sb.append(' ');
				}
			}
		}
		if(order_con!=null){
			sb.append(" order by `").append(order_con.column).append("` ").append((Boolean)order_con.value?"asc ":"desc ");
		}
		if(limit_con!=null){
			sb.append(" limit ?,?");
			params.add(limit_con.value);
			params.add(limit_con.value2);
		}
		String sql = sb.toString();
		try {
			DBHelper helper = DBHelper.getIns("mysql");
			return helper.getRS(sql,params);
		} catch (SQLException e) {
			throw new MVCException("runSQL error: [ "+sql+" ]", e);
		}
	}
//	public ResultSet runSQLOld(List<SQLConstains> cons,SQLConstains limit_con,SQLConstains order_con,DBTool tool,String tbName,String prefix){
//		if(tbName == null) return null ;//参数检查，表名不能为空
//		
//		ArrayList<String> sa = new ArrayList<String>();
//		ArrayList<Object> params = new ArrayList<Object>();
//		final String[] fh = {"","=",">","<",">=","<=","<>"};
//		SQLConstains limitc=null;
//		SQLConstains orderc=null;
//		String w;
//		StringBuffer sb=new StringBuffer();
//		for(SQLConstains c: cons){
//			if(c.type<fh.length){
//				sb.setLength(0);
//				//sa.add(String.format("`%s` %s ?", c.column,fh[c.type]));
//				sb.append("`").append(c.column).append("` ").append(fh[c.type]).append(" ?");
//				sa.add(sb.toString());
//				params.add(c.value);
//				continue;
//			}
//			switch(c.type){
//			case SQLConstains.TYPE_ISNULL:
//				//w = String.format(" `%s` is NULL ", c.column);
//				sb.setLength(0);
//				sb.append(" `").append(c.column).append("` is NULL ");
//				sa.add(sb.toString());
//				break;
//			case SQLConstains.TYPE_LIKE:
//				//String w = String.format("  `%s` like '$%s$' ", c.column, c.value).replace("$","%");
//				//sa.add(w);
////				w = String.format("  `%s` like ? ", c.column);
////				sa.add(w);
//				sb.setLength(0);
//				sb.append(" `").append(c.column).append("` like ? ");
//				sa.add(sb.toString());
//				
//				params.add("%"+c.value+"%");
//				
//				break;
//			case SQLConstains.TYPE_MLIKE:
//				String[] ca = c.column.split(",");
//				//String t  = "(" +String.format("`%s` like ?",ca[0]);
//				sb.setLength(0);
//				sb.append("(`").append(ca[0]).append("` like ?");
//				params.add("%"+c.value+"%");
//				for(int i=1;i<ca.length;i++){
//					//t+=" or "+String.format("`%s` like ?",ca[i]);
//					sb.append(" or `").append(ca[i]).append("` like ?");
//					params.add("%"+c.value+"%");
//				}
//				//t+=")";
//				sb.append(")");
//				sa.add(sb.toString());
//				break;
//			case SQLConstains.TYPE_LIMIT:
//				limitc = c;
//				break;
//			case SQLConstains.TYPE_ORDER:
//				orderc = c;
//				break;
//			case SQLConstains.TYPE_CUSTOM:
//				sa.add(c.column);
//			}
//		}
//		
//		//String tail ="";
//		sb.setLength(0); //使用sb代替tail
//		if(orderc!=null){
//			//tail += String.format(" order by `%s` %s", orderc.column , (Boolean)orderc.value?"asc":"desc");
//			
//			sb.append(" order by `").append(orderc.column).append("` ").append((Boolean)orderc.value?"asc":"desc");
//		}
//		if( limitc!=null){
//			//tail += String.format(" limit %d,%d", limitc.value,limitc.value2);
//			//tail += " limit ?,?";
//			sb.append(" limit ?,?");
//			params.add(limitc.value);
//			params.add(limitc.value2);
//		}
//		
//		DBHelper helper = DBHelper.getIns("mysql");
//		String c=  TextTool.join2(sa, " and ") +  sb.toString();
//		if(c.trim().length() == 0){ //如果没有任何条件，则直接查询
//
//			String sql =TextTool.concat("select ",prefix," from `" ,tbName,"`").toString();
//			try {
//				return helper.getRS(sql);
//			} catch (SQLException e) {
//				throw new MVCException("runSQL error: [ "+sql+" ]", e);
//			}
//		}else{
//			c = c.trim();
//			if(!c.startsWith("limit") && ! c.startsWith("order")){
//				c  = " where " +c ;
//			}else c = " " +c;
//			String sql = TextTool.concat("select ",prefix," from `",tbName ,"` ",c).toString();
//			try {
//				return helper.getRS(sql,params);
//			} catch (SQLException e) {
//				throw new MVCException("runSQL error: [ "+sql+" ]", e);
//			}
//		}
//		
//	}


	@Override
	public ResultSet runSQL(String rawSql) throws SQLException {
		return DBHelper.getIns().getRS(rawSql);
	}

	@Override
	public Map<String, ?> columnOfRs(String sql,ResultSet rs) {
		return DBHelper.getIns().columeOfRs(sql,rs);
	}

	@Override
	public Map<String, ColumnDesc> listColumn(String tname,boolean useCache) {
		return DBHelper.getIns().listColumn(tname,useCache);
	}

	@Override
	public String keyColumn(String tname) {
		return DBHelper.getIns().keyColumn(tname);
	}

	@Override
	public void beginTransaction() {
		DBHelper.getIns().beginTransation();
	}

	@Override
	public void commit() {
		DBHelper.getIns().commit();
	}

	@Override
	public void rollback() {
		DBHelper.getIns().rollback();
	}



}
