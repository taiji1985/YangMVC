package org.docshare.yangmvc.orm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.except.MVCException;
import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.orm.ArrayTool;
import org.docshare.yangmvc.orm.ColumnDesc;
import org.docshare.yangmvc.orm.DBHelper;
import org.docshare.yangmvc.orm.DBTool;
import org.docshare.yangmvc.orm.IDBDelegate;
import org.docshare.yangmvc.orm.Ident;
import org.docshare.yangmvc.orm.Model;
import org.docshare.yangmvc.orm.SQLConstains;
import org.docshare.yangmvc.util.TextTool;


public class MySQLDelegate implements IDBDelegate {
	private static final Logger logger = LoggerFactory.getLogger(MySQLDelegate.class);

	public Map<String, ColumnDesc> c_to_remarks;
	@Override
	public ResultSet resultById(String tname,String column,Object id) throws SQLException {
		Ident.assertSafe(column);
		//String.format("select * from `%s` where `%s` = ? limit 0,1",tname,column)
		StringBuilder sBuffer =new StringBuilder(64);
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
	public int save(DBTool tool, Model m, String key, boolean forceInsert ){
		if(m == null){
			logger.error("can not save a null object");
			return 0;
		}
		Object id = m.get(key);
		String sql = "";
		
		ArrayList<Object> plist = new ArrayList<Object>(8); //参数列表
		if(forceInsert || m.isCreated || id == null || (id instanceof Integer && (Integer)id <= 0 ) ){
			sql = buildInsertSql(m, plist);
		}else{
			sql = buildUpdateSql(m, key, id, plist);
			if(sql == null){
				logger.info("no effective change for update, table={} ,key={}", m.getTableName(), id);
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

	/**
	 * 构建 INSERT。
	 *
	 * <p>规则：
	 * <ul>
	 *   <li>{@code Model.putNull(col)} 标记的列写成字面量 {@code NULL}（显式要求空值）；</li>
	 *   <li>其余值为 null 的列直接跳过（让数据库用列默认值，这是旧行为）。</li>
	 * </ul>
	 */
	static String buildInsertSql(Model m, List<Object> plist) {
		StringBuilder ks = new StringBuilder(64);
		StringBuilder vs2 = new StringBuilder(64);
		boolean first = true;
		for (String k : m.keySet()) {
			boolean explicitNull = m.isNullColumn(k);
			Object v = m.get(k);
			if (!explicitNull && v == null) {
				continue;
			}
			if (!first) {
				ks.append(',');
				vs2.append(',');
			}
			ks.append('`').append(k).append('`');
			if (explicitNull) {
				vs2.append("NULL");
			} else {
				vs2.append('?');
				plist.add(v);
			}
			first = false;
		}
		StringBuilder sqlb = new StringBuilder(128);
		sqlb.append("insert into `").append(m.getTableName()).append("`(")
				.append(ks).append(") values(").append(vs2).append(")");
		return sqlb.toString();
	}

	/**
	 * 构建 UPDATE；没有可更新的列时返回 null。
	 *
	 * <p>主键值走 {@code ?} 参数（旧实现是用 {@link ArrayTool#valueWrapper} 拼字面量，
	 * 字符串主键里带引号就会破坏 SQL）。
	 *
	 * <p>{@code putNull} 显式置空；普通 null 值默认跳过，
	 * {@link Config#db_update_null}=true 时也写成 {@code NULL}。
	 */
	static String buildUpdateSql(Model m, String key, Object id, List<Object> plist) {
		Set<String> changed = m.changeColumns();
		if (changed == null || changed.isEmpty()) {
			return null;
		}
		ArrayList<String> sa = new ArrayList<String>(changed.size());
		for (String k : changed) {
			if (k.equals(key)) {
				continue;
			}
			if (!m.hasColumn(k)) {
				continue; // extra 里的附加字段不参与更新
			}
			if (m.isNullColumn(k)) {
				sa.add("`" + k + "`=NULL");
				continue;
			}
			Object v = m.get(k);
			if (v == null) {
				if (Config.db_update_null) {
					sa.add("`" + k + "`=NULL");
				}
				continue;
			}
			sa.add("`" + k + "`=?");
			plist.add(v);
		}
		if (sa.isEmpty()) {
			return null;
		}
		StringBuilder sqlb = new StringBuilder(128);
		sqlb.append("update `").append(m.getTableName()).append("` set ")
				.append(ArrayTool.join(",", sa))
				.append(" where `").append(key).append("` = ?");
		plist.add(id);
		return sqlb.toString();
	}
	
	@Override
	public int delete(String tname,String key,Object id){
		Ident.assertSafe(key);
		//String sql = String.format("delete from `%s` where `%s` = ?", tname,key);
		StringBuilder sBuffer =new StringBuilder(64);
		sBuffer.append("delete from `");
		sBuffer.append(tname).append("` where `").append(key).append("` = ?");
		String sql = sBuffer.toString();
		logger.debug("DBTool run sql: "+ sql +" ,param  = "+id);
		return DBHelper.getIns().update(sql,id);
	}

//	public ResultSet runSQL(List<SQLConstains> cons,SQLConstains limit_con,SQLConstains order_con,DBTool tool,String tbName){
//		return runSQL(cons,limit_con,order_con, tool, tbName,"*");
//	}

	@Override
	public long size(List<SQLConstains> cons, DBTool tool, String tbName, Object[] myParams) {
		ResultSet rs;
		try {
			rs = runSQL(cons, null,null,tool, tbName,"count(*) as CT",myParams);
			if(rs!=null){
				if(rs.next()){
					long id = rs.getLong("CT");
					rs.close();
					return id;
				}
				rs.close();
			}
		} catch (SQLException e) {
			//e.printStackTrace();
			throw new MVCException("size: get size of query error", e);
		}
		return 0;
	}
	@Override
	public ResultSet runSQL(List<SQLConstains> cons,List<SQLConstains> order_cons,SQLConstains limit_con,DBTool tool,String tbName,String prefix,Object[] myParams){

		StringBuilder sb=new StringBuilder(128);
		sb.append("select ").append(prefix).append(" from ");
		if(tbName.contains("(")){
			sb.append(tbName);
		}else{
			sb.append("`").append(tbName).append("` ");
		}
		final String[] fh = {"","=",">","<",">=","<=","<>"};
		ArrayList<Object> params = new ArrayList<Object>();
		if(myParams!=null){
            params.addAll(Arrays.asList(myParams));
		}
		if(!cons.isEmpty()){
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
				case SQLConstains.TYPE_ISNOTNULL:
					sb.append(" `").append(c.column).append("` is not NULL ");
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
					break;
				default:
					logger.error("unsupport type"+c.type);
				}
			}
		}
		if(order_cons!=null && !order_cons.isEmpty()){
			sb.append(" order by ");
			boolean first = true;
			for(SQLConstains order_con : order_cons){
				if(!first){
					sb.append(", ");
				}
				first = false;
				sb.append("`").append(order_con.column).append("` ").append((Boolean)order_con.value?"asc":"desc");
			}
			sb.append(" ");
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
			throw new MVCException("runSQL error: [ "+sql+" ] ,params="+TextTool.join(params, ",") +"err = "+e.getMessage(), e);

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
//		StringBuilder sb=new StringBuilder();
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
