package org.docshare.yangmvc.orm.postgres;

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
import org.docshare.yangmvc.util.TextTool;


public class PostgresDelegate implements IDBDelegate {
	private static final Logger logger = LoggerFactory.getLogger(PostgresDelegate.class);

	public Map<String, ColumnDesc> c_to_remarks;
	@Override
	public ResultSet resultById(String tableName,String column,Object id) throws SQLException {
		Ident.assertSafe(column);
        return DBHelper.getIns().getPrepareRS(String.format("select * from \"%s\" where \"%s\" = ? limit 1",tableName,column),id);
	}

	@Override
	public int save(DBTool tool, Model m, String key, boolean forceInsert) {
		if (m == null) {
			logger.error("can not save a null object");
			return 0;
		}
		Object id = m.get(key);
		String sql;

		ArrayList<Object> plist = new ArrayList<>(); // 参数列表
		if (forceInsert || m.isCreated || id == null || (id instanceof Integer && (Integer)id <= 0)) {
			// This is an insert
			StringBuilder ks = new StringBuilder();
			StringBuilder vs2 = new StringBuilder();

			boolean first = true;
			for (String k : m.keySet()) {
				Object v = m.get(k);
				if (v == null || v.toString().isEmpty()) {
					continue;
				}
				if (!first) {
					ks.append(',');
					vs2.append(',');
				}

				ks.append("\"").append(k).append("\"");
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(null, v, type);
				vs2.append("?");
				plist.add(v);
				first = false;
			}
			sql = String.format("insert into \"%s\"(%s) values(%s)", m.getTableName(), ks, vs2);
		} else {
			ArrayList<String> sa = new ArrayList<>();

			for (String k : m.changeColumns()) {
				Object v = m.get(k);
				if (v == null || v.toString().isEmpty()) {
					continue;
				}
				String type = tool.getColumnTypeName(k);
				ArrayTool.valueWrapper(k, m.get(k), type);
				sa.add("\"" + k + "\"=?");
				plist.add(m.get(k));
			}

			if (sa.isEmpty()) {
				sql = String.format("update \"%s\" set 1=1 where %s",
						m.getTableName(),
						ArrayTool.valueWrapper(key, id, tool.getColumnTypeName("id"))
				);
				logger.info("no change data for update " + sql);
				return 0;
			}

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < sa.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(sa.get(i));
			}

			sql = String.format("update \"%s\" set %s where %s",
					m.getTableName(),
					sb,
					ArrayTool.valueWrapper(key, id, tool.getColumnTypeName("id"))
			);
		}

		logger.debug("DBTool run sql: " + sql + "  params=[" + ArrayTool.joinWithLengthLimit(",", plist, 20) + "]");
		Object[] objs = plist.toArray();
		DBHelper helper = DBHelper.getIns(DBHelper.DB_POSTGRES);

		int d = helper.updateWithArray(sql, objs);
		logger.debug("return " + d);
		if (d != 0 && (id == null || forceInsert)) {
			id = helper.getLastId();
			m.put(key, id);
		}
		return d;
	}
	
	@Override
	public int delete(String tableName,String key,Object id){
		Ident.assertSafe(key);
		String sql = String.format("delete from \"%s\" where \"%s\" = ?", tableName,key);
		logger.debug("DBTool run sql: "+sql +" ,param  = "+id);
		return DBHelper.getIns().update(sql,id);
	}

	public ResultSet runSQL(List<SQLConstains> cons,DBTool tool,String tbName){
		return runSQL(cons,null,null, tool, tbName,"*",null);
	}

	@Override
	public long size(List<SQLConstains> cons, DBTool tool, String tbName, Object[] myParams) {
		ResultSet rs;
		try {
			rs = runSQL(cons,null,null, tool, tbName,"count(*) as CT",myParams);
			if(rs.next()){
				long id = rs.getLong("CT");
				rs.close();
				return id;
			}
			rs.close();
		} catch (SQLException e) {
			throw new MVCException("get size error: ", e);
		}
		return 0;
	}
	public ResultSet runSQL(List<SQLConstains> cons,List<SQLConstains> order_cons,SQLConstains limitc,DBTool tool,String tbName,String prefix,Object[] args){
		if(tbName == null) return null ;//参数检查，表名不能为空
		
		ArrayList<String> sa = new ArrayList<>();
		ArrayList<Object> params = new ArrayList<>();
		if(args != null){
			params.addAll(Arrays.asList( args));
		}
		final String[] fh = {"","=",">","<",">=","<=","<>"};
		for(SQLConstains c: cons){
			if(c.type<fh.length){
				sa.add(String.format("\"%s\" %s ?", c.column,fh[c.type]));
				params.add(c.value);
				
				continue;
			}
			switch(c.type){
				case SQLConstains.TYPE_ISNULL:
					sa.add(String.format(" \"%s\" is NULL ", c.column));
					break;
				case SQLConstains.TYPE_ISNOTNULL:
					sa.add(String.format(" \"%s\" is not NULL ", c.column));
					break;
				case SQLConstains.TYPE_LIKE:
					String w = String.format("  \"%s\" like ? ", c.column);
					sa.add(w);
					params.add("%"+c.value+"%");
					
					break;
				case SQLConstains.TYPE_MLIKE:
					String[] ca = c.column.split(",");
					String t  = "(" +String.format("\"%s\" like ?",ca[0]);
					params.add("%"+c.value+"%");
					for(int i=1;i<ca.length;i++){
						t+=" or "+String.format("\"%s\" like ?",ca[i]);
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
				tail += String.format("\"%s\" %s", orderc.column , (Boolean)orderc.value?"asc":"desc");
			}
		}
		if( limitc!=null){
			// PostgreSQL使用 LIMIT count OFFSET start 语法
			tail += " limit ? offset ?";
			params.add(limitc.value2); // count (限制数量)
			params.add(limitc.value);  // offset (偏移量)
		}
		
		DBHelper helper = DBHelper.getIns(DBHelper.DB_POSTGRES);
		String c=  TextTool.join2(sa, " and ") +  tail;
		if(c.trim().isEmpty()){ //如果没有任何条件，则直接查询
			String sql = "select "+prefix+" from \"" + tbName+"\"";
			try {
				return helper.getRS(sql);
			} catch (SQLException e) {
				throw new MVCException("runSQL error: [ "+sql+" ]", e);
			}
		}else{
			c = c.trim();
			if(!c.startsWith("limit") && ! c.startsWith("order")){
				c  = " where " +c ;
			}else c = " " +c;
			String sql = "select "+prefix+" from \""+tbName +"\" "+c;
			try {
				return helper.getRS(sql,params);
			} catch (SQLException e) {
				throw new MVCException("runSQL error: [ "+sql+" ]", e);
			}
		}
		
	}


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
