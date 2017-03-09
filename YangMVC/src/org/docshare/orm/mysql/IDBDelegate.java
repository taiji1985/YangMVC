package org.docshare.orm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;

public interface IDBDelegate {
	ResultSet resultById(DBHelper helper, String tname, String column, Object id) throws SQLException;

	int save(DBTool tool, DBHelper helper, Model m, String key, boolean isInsert);

	int delete(DBHelper helper, String tname, String key, Object id);
}
