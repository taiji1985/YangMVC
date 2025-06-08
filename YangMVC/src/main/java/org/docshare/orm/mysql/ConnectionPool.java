package org.docshare.orm.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.docshare.mvc.Config;
import org.docshare.mvc.except.MVCException;
import org.docshare.util.TextTool;

public class ConnectionPool implements IConnectionPool {

	public Connection getConnection() throws SQLException {
		try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String encoding = Config.dbEncoding;
            if(encoding.contains("utf8")) encoding = "utf8";
            String uri = TextTool.concat("jdbc:mysql://",Config.dbhost,":",Config.dbport,"/",Config.dbname,"?characterEncoding=",encoding,"&useSSL=",Config.useSSL,"&serverTimezone=Asia/Shanghai").toString();
            Connection con = DriverManager.getConnection(uri, Config.dbusr, Config.dbpwd);
            
            return con;
        } catch (ClassNotFoundException e) {
            throw new MVCException(e);
        }
        
	}

}
