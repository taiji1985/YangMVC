package org.docshare.yangmvc.spring.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.docshare.log.Log;
import org.docshare.orm.mysql.IConnectionPool;
import org.docshare.yangmvc.spring.util.SpringContextUtil;

public class SpringConnectionPool implements IConnectionPool {
    
    @Override
    public Connection getConnection() throws SQLException {
        Log.i("SpringConnectionPool getConnection");
        DataSource bean = SpringContextUtil.getBean(DataSource.class);
        return bean.getConnection();
    }
    
}
