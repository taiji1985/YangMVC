package org.docshare.yangmvc.orm.mysql;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionPool {
    public Connection getConnection() throws SQLException;
}
