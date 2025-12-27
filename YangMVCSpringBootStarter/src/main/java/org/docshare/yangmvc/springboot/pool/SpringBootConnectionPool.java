package org.docshare.yangmvc.springboot.pool;

import org.docshare.yangmvc.orm.mysql.IConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Spring Boot数据源连接池实现
 * <p>
 * 这个类实现了YangMVC的IConnectionPool接口，将YangMVC的数据库操作
 * 与Spring Boot的DataSource集成，使得YangMVC可以使用Spring Boot配置的数据源
 * <p>
 * 当Spring Boot应用配置了DataSource时，这个连接池会自动被注册和使用
 *
 * @author YangMVC
 */
public class SpringBootConnectionPool implements IConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootConnectionPool.class);

    private final DataSource dataSource;

    /**
     * 构造函数
     *
     * @param dataSource Spring Boot配置的数据源
     */
    public SpringBootConnectionPool(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource cannot be null");
        }
        this.dataSource = dataSource;
        logger.info("SpringBootConnectionPool initialized with DataSource: {}", dataSource.getClass().getName());
    }

    /**
     * 从Spring Boot数据源获取数据库连接
     *
     * @return 数据库连接
     * @throws SQLException 如果获取连接失败
     */
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        logger.debug("Connection obtained from Spring Boot DataSource");
        return connection;
    }

    /**
     * 获取底层的DataSource
     * <p>
     * 这个方法主要用于调试和监控目的
     *
     * @return DataSource实例
     */
    public DataSource getDataSource() {
        return dataSource;
    }
}
