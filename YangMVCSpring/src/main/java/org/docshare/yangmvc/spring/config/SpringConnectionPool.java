package org.docshare.yangmvc.spring.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.orm.mysql.IConnectionPool;
import org.docshare.yangmvc.spring.util.SpringContextUtil;

public class SpringConnectionPool implements IConnectionPool {
	private static final Logger logger = LoggerFactory.getLogger(SpringConnectionPool.class);

    
    @Override
    public Connection getConnection() throws SQLException {
        logger.info("SpringConnectionPool getConnection");
        DataSource bean = SpringContextUtil.getBean(DataSource.class);
        return bean.getConnection();
    }
    
}
