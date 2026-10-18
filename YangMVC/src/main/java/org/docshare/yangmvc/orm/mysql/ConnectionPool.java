package org.docshare.yangmvc.orm.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.mvc.except.MVCException;
import org.docshare.yangmvc.util.TextTool;

public class ConnectionPool implements IConnectionPool {

	/**
	 * 构建 JDBC URL（{@link SimpleConnectionPool} 也会用它）。
	 */
	public static String buildUrl() {
		String encoding = Config.dbEncoding;
		if (encoding.contains("utf8")) encoding = "utf8";
		return TextTool.concat("jdbc:mysql://", Config.dbhost, ":", Config.dbport, "/", Config.dbname,
				"?characterEncoding=", encoding, "&useSSL=", Config.useSSL, "&serverTimezone=Asia/Shanghai").toString();
	}

	public Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection(buildUrl(), Config.dbusr, Config.dbpwd);
		} catch (ClassNotFoundException e) {
			throw new MVCException(e);
		}

	}

}
