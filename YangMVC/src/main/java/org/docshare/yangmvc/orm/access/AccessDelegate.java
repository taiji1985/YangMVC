package org.docshare.yangmvc.orm.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.except.MVCException;
import org.docshare.yangmvc.orm.ArrayTool;
import org.docshare.yangmvc.orm.ColumnDesc;
import org.docshare.yangmvc.orm.DBHelper;
import org.docshare.yangmvc.orm.DBTool;
import org.docshare.yangmvc.orm.IDBDelegate;
import org.docshare.yangmvc.orm.Ident;
import org.docshare.yangmvc.orm.Model;
import org.docshare.yangmvc.orm.SQLConstains;
import org.docshare.yangmvc.util.FileTool;
import org.docshare.yangmvc.util.TextTool;

public class AccessDelegate implements IDBDelegate {
	private static final Logger logger = LoggerFactory.getLogger(AccessDelegate.class);

	public Map<String, ColumnDesc> c_to_remarks;
	@Override
	public ResultSet resultById(String tname,String column,Object id) throws SQLException {
		Ident.assertSafe(column);
		ResultSet rs = DBHelper.getIns().getPrepareRS(String.format("select * from [%s] where [%s] = ? limit 0,1",tname,column),id);
		return rs;
	}

	@Override
	public int save(DBTool tool, Model m, String key, boolean forceInsert ){
		if(m == null){
			logger.error("can not save a null object");
			return 0;
		}
		Object id = m.get(key);
		String sql = "";
		
		ArrayList<Object> plist = new ArrayList<Object>(); //参数列表
		if(forceInsert || m.isCreated || id == null || (id instanceof Integer && (Integer)id <= 0 ) ){
			//This is an insert
			StringBuilder ks= new StringBuilder();
			StringBuilder vs2=new StringBuilder();
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
					ks.append(',');
					vs2.append(',');
				}
				
				ks.append('[').append(k).append(']');
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(null, v,type);
				vs2.append('?');
				plist.add(v);
				first = false;
			}
			sql = String.format("insert into [%s](%s) values(%s)", m.getTableName(),ks.toString(),vs2.toString());
		}else{
			ArrayList<String> sa=new ArrayList<String>();
			
			for(String k: m.changeColumns()){
				if(k != null && k.equals(key))continue;
				Object v = m.get(k);
				if(v == null || v.toString().length() == 0){
					continue;
				}
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(k, m.get(k),type);
				//sa.add(s);
				sa.add("["+k+"]=?");
				plist.add(m.get(k));
			}
			String ss = ArrayTool.join(",", sa);
			sql=String.format("update [%s] set %s where %s", m.getTableName(),ss,ArrayTool.valueWrapper(key, id,tool.getColumnTypeName("id")) );
			if(m.changeColumns().size() == 0){
				logger.info("no change data for update "+sql);
				return 0;
			}
		}
		logger.debug("DBTool run sql: "+sql+"  params=["+ArrayTool.joinWithLengthLimit(",", plist,20)+"]");
		Object[] objs = plist.toArray();
		DBHelper helper = DBHelper.getIns("mysql");
		
		int d = helper.updateWithArray(sql,objs);
		logger.debug("return "+d);
		if(d != 0 &&(id == null|| forceInsert)){
			id = helper.getLastId();			
			m.put(key, id);
		}
		return d;//helper.getLastId();
	}
	
	@Override
	public int delete(String tname,String key,Object id){
		Ident.assertSafe(key);
		String sql = String.format("delete from [%s] where [%s] = ?", tname,key);
		logger.debug("DBTool run sql: "+sql +" ,param  = "+id);
		return DBHelper.getIns().update(sql,id);
	}

//	public ResultSet runSQL(List<SQLConstains> cons,DBTool tool,String tbName){
//		return runSQL(cons, tool, tbName,"*");
//	}

	@Override
	public long size(List<SQLConstains> cons, DBTool tool, String tbName, Object[] myParams) {
		ResultSet rs=null;
		try {
			rs = runSQL(cons,null,null, tool, tbName,"count(*) as CT",myParams);
			if(rs.next()){
				long id = rs.getLong("CT");
				rs.close();
				return id;
			}
		} catch (SQLException e) {
			throw new MVCException("get size error", e);
		}finally {
			FileTool.safelyClose(rs);
		}
		return 0;
	}
	public ResultSet runSQL(List<SQLConstains> cons,List<SQLConstains> order_cons,SQLConstains limitc,DBTool tool,String tbName,String prefix,Object[] args){
		if(tbName == null) return null ;//参数检查，表名不能为空
		
		ArrayList<String> sa = new ArrayList<String>();
		ArrayList<Object> params = new ArrayList<Object>();
		if(args!=null){
            params.addAll(Arrays.asList(args));
		}
		final String[] fh = {"","=",">","<",">=","<=","<>"};
//		SQLConstains limitc=null;
//		SQLConstains orderc=null;
		for(SQLConstains c: cons){
			if(c.type<fh.length){
				//String w = ArrayTool.valueWrapper(null, c.value, tool.getColumnTypeName(c.column));
				//sa.add(String.format("[%s] %s %s", c.column,fh[c.type],w));
				sa.add(String.format("[%s] %s ?", c.column,fh[c.type]));
				params.add(c.value);
				
				continue;
			}
			switch(c.type){
			case SQLConstains.TYPE_ISNULL:
				sa.add(String.format(" [%s] is NULL ", c.column));
				break;
			case SQLConstains.TYPE_ISNOTNULL:
				sa.add(String.format(" [%s] is not NULL ", c.column));
				break;
			case SQLConstains.TYPE_LIKE:
				//String w = String.format("  [%s] like '$%s$' ", c.column, c.value).replace("$","%");
				//sa.add(w);
				String w = String.format("  [%s] like ? ", c.column);
				sa.add(w);
				params.add("%"+c.value+"%");
				
				break;
			case SQLConstains.TYPE_MLIKE:
				String[] ca = c.column.split(",");
				String t  = "(" +String.format("[%s] like ?",ca[0]);
				params.add("%"+c.value+"%");
				for(int i=1;i<ca.length;i++){
					t+=" or "+String.format("[%s] like ?",ca[i]);
					params.add("%"+c.value+"%");
				}
				t+=")";
				sa.add(t);
				break;
			case SQLConstains.TYPE_LIMIT:
				limitc = c;
				break;
			case SQLConstains.TYPE_ORDER:
				order_cons.add(c);
				break;
			case SQLConstains.TYPE_CUSTOM:
				sa.add(c.column);
				break;
			default:
				logger.error("unsupport type"+c.type);
			}
			
		}
		
		String tail ="";
		if(order_cons!=null && !order_cons.isEmpty()){
			tail += " order by ";
			boolean first = true;
			for(SQLConstains orderc : order_cons){
				if(!first){
					tail += ", ";
				}
				first = false;
				tail += String.format("[%s] %s", orderc.column , (Boolean)orderc.value?"asc":"desc");
			}
		}
		// Access数据库不支持LIMIT语法，需要使用TOP语法
		String topClause = "";
		if( limitc!=null){
			// Access使用TOP语法，但只支持TOP n，不支持OFFSET
			// 这里只能实现简单的TOP功能
			int count = (Integer)limitc.value2;
			topClause = " TOP " + count;
		}
		
		DBHelper helper = DBHelper.getIns("access");
		String c=  TextTool.join2(sa, " and ") +  tail;
		if(c.trim().length() == 0){ //如果没有任何条件，则直接查询
			
			try {
				return helper.getRS("select"+topClause+" "+prefix+" from [" + tbName+"]");
			} catch (SQLException e) {
				logger.error("", e);
				return null;
			}
		}else{
			c = c.trim();
			if(!c.startsWith("limit") && ! c.startsWith("order")){
				c  = " where " +c ;
			}else c = " " +c;
			// 移除limit相关的条件，因为Access使用TOP
			if(c.contains("limit")){
				c = c.replaceAll("\\s*limit\\s*\\?\\s*,\\s*\\?\\s*", "");
			}
			String sql = "select"+topClause+" "+prefix+" from ["+tbName +"] "+c;
			try {
				return helper.getRS(sql,params);
			} catch (SQLException e) {
				throw new MVCException("runSQL error: [ "+sql+" ]", e);
			}
		}
		
	}
//	@Deprecated
//	public String buildSQLWithoutLimit(List<SQLConstains> cons,DBTool tool){
//		return buildSQL(cons, tool,false,null);
//	}
//	public String buildSQL(List<SQLConstains> cons,DBTool tool,String sqlfrom){
//		return buildSQL(cons, tool,true,sqlfrom);
//	}
	
	@Deprecated
//	public String buildSQL(List<SQLConstains> cons,DBTool tool,boolean withLimit,String sqlfrom){
//		ArrayList<String> sa = new ArrayList<String>();
//		final String[] fh = {"","=",">","<",">=","<=","<>"};
//		SQLConstains limitc=null;
//		SQLConstains orderc=null;
//		for(SQLConstains c: cons){
//			if(c.type<fh.length){
//				String w = ArrayTool.valueWrapper(null, c.value, tool.getColumnTypeName(c.column));
//				sa.add(String.format("[%s] %s %s", c.column,fh[c.type],w));
//				continue;
//			}
//			switch(c.type){
//			case SQLConstains.TYPE_LIKE:
//				String w = String.format("  [%s] like '$%s$' ", c.column, c.value).replace("$","%");
//				sa.add(w);
//				break;
//			case SQLConstains.TYPE_LIMIT:
//				limitc = c;
//				break;
//			case SQLConstains.TYPE_ORDER:
//				orderc = c;
//				break;
//			}
//		}
//		
//		String tail ="";
//		if(orderc!=null){
//			tail += String.format(" order by %s %s", orderc.column,(Boolean)orderc.value?"asc":"desc");
//		}
//		if(withLimit && limitc!=null){
//			tail += String.format(" limit %d,%d", limitc.value,limitc.value2);
//		}
//		
//		
//		String c=  TextTool.join2(sa, " and ") +  tail;
//		if(sqlfrom == null) return c;
//		sqlfrom = sqlfrom+ " ";
//		if(c.trim().length() == 0){
//			return sqlfrom;
//		}else{
//			c = c.trim();
//			if(sqlfrom.contains("where")){
//				if(c.startsWith("limit") || c.startsWith("order")){
//					return sqlfrom + c;
//				}else return  sqlfrom +" and " + c;
//			}else{
//				if(c.startsWith("limit")|| c.startsWith("order")){
//					return sqlfrom + c ;
//				}else {
//					return  sqlfrom+ " where " + c;
//				}
//			}
//		}
//		
//		
//	}

	@Override
	public ResultSet runSQL(String rawSql) throws SQLException {
		return DBHelper.getIns().getRS(rawSql);
	}

	@Override
	public Map<String, ColumnDesc> columnOfRs(String sql,ResultSet rs) {
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
