package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public interface IDBDelegate {
	ResultSet resultById(String tname, String column, Object id) throws SQLException;

	int save(DBTool tool, Model m, String key, boolean isInsert);

	int delete(String tname, String key, Object id);

	String buildSQL(List<SQLConstains> cons, DBTool tool, String sqlfrom);
	ResultSet runSQL(List<SQLConstains> cons,DBTool tool,String tbName);
	long size(List<SQLConstains> cons,DBTool tool,String tbName);

	ResultSet runSQL(String rawSql) throws SQLException;

	Map<String, ?> columnOfRs(ResultSet rs);


	Map<String, ColumnDesc> listColumn(String tname, boolean useCache);

	String keyColumn(String tname);
}
