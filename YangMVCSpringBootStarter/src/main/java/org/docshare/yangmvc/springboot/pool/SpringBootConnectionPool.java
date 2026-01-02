package org.docshare.yangmvc.springboot.pool;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.orm.mysql.IConnectionPool;
import org.docshare.yangmvc.springboot.application.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spring Boot数据源连接池实现
 * <p>
 * 这个类实现了YangMVC的IConnectionPool接口，将YangMVC的数据库操作
 * 与Spring Boot的DataSource集成，使得YangMVC可以使用Spring Boot配置的数据源
 * <p>
 * 当Spring Boot应用配置了DataSource时，这个连接池会自动被注册和使用
 * <p>
 * 还会自动从DataSource提取数据库信息并设置到Config中，确保YangMVC的ORM功能正常工作
 *
 * @author YangMVC
 */
public class SpringBootConnectionPool implements IConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SpringBootConnectionPool.class);
    private static boolean configInitialized = false;

    private final DataSource dataSource;

    /**
     * 无参构造函数 - 用于MySQLDBHelper通过反射创建实例
     * <p>
     * 这个构造函数会从Spring ApplicationContext中获取DataSource Bean
     *
     * @throws IllegalStateException 如果在Spring环境中找不到DataSource
     */
    public SpringBootConnectionPool() {
        this.dataSource = lookupDataSource();
        if (this.dataSource == null) {
            throw new IllegalStateException("DataSource not found in Spring ApplicationContext");
        }
        logger.info("SpringBootConnectionPool initialized via ApplicationContext with DataSource: {}",
                this.dataSource.getClass().getName());
    }

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
     * 从Spring ApplicationContext查找DataSource
     *
     * @return DataSource实例，如果找不到返回null
     */
    private DataSource lookupDataSource() {
        try {
            if (ContextHolder.getApplicationContext() != null) {
                return ContextHolder.getApplicationContext().getBean(DataSource.class);
            }
        } catch (Exception e) {
            logger.warn("Failed to lookup DataSource from Spring ApplicationContext", e);
        }
        return null;
    }

    /**
     * 从Spring Boot数据源获取数据库连接
     * <p>
     * 首次获取连接时，会自动从连接中提取数据库信息并设置到Config中
     *
     * @return 数据库连接
     * @throws SQLException 如果获取连接失败
     */
    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();

        // 首次连接时，初始化Config的数据库配置
        if (!configInitialized) {
            initConfigFromConnection(connection);
            configInitialized = true;
        }

        logger.debug("Connection obtained from Spring Boot DataSource");
        return connection;
    }

    /**
     * 从数据库连接中提取信息并设置到Config
     * <p>
     * 这样可以确保YangMVC的ORM功能（如获取列信息）能够正常工作
     *
     * @param connection 数据库连接
     */
    private void initConfigFromConnection(Connection connection) {
        try {
            // 获取数据库URL
            String url = connection.getMetaData().getURL();
            logger.info("Extracting database config from URL: {}", url);

            // 解析MySQL JDBC URL
            // 格式: jdbc:mysql://host:port/database?params
            parseMySqlUrl(url);

            // 尝试从连接获取数据库名（更可靠）
            try {
                String catalog = connection.getCatalog();
                if (catalog != null && !catalog.isEmpty()) {
                    Config.dbname = catalog;
                    logger.info("Database name from connection catalog: {}", catalog);
                }
            } catch (Exception e) {
                logger.debug("Could not get catalog from connection: {}", e.getMessage());
            }

            // 记录配置信息
            logger.info("YangMVC Config updated - dbname: {}, dbhost: {}, dbport: {}",
                    Config.dbname, Config.dbhost, Config.dbport);

        } catch (SQLException e) {
            logger.warn("Failed to extract database info from connection: {}", e.getMessage());
        }
    }

    /**
     * 解析MySQL JDBC URL并设置到Config
     * <p>
     * 支持的URL格式：
     * - jdbc:mysql://host:port/database
     * - jdbc:mysql://host:port/database?params
     * - jdbc:mysql://host/database
     *
     * @param url JDBC URL
     */
    private void parseMySqlUrl(String url) {
        if (url == null || !url.startsWith("jdbc:mysql://")) {
            logger.debug("Not a MySQL URL or URL is null: {}", url);
            return;
        }

        try {
            // 移除 jdbc:mysql:// 前缀
            String remaining = url.substring(13); // "jdbc:mysql://"长度为13

            // 分离路径和参数
            String pathPart = remaining;
            int queryIndex = remaining.indexOf('?');
            if (queryIndex > 0) {
                pathPart = remaining.substring(0, queryIndex);
            }

            // 解析主机和端口
            // 格式可能是 host:port/database 或 host/database
            Pattern pattern = Pattern.compile("^([^:/]+)(?::(\\d+))?(?:/(.+))?$");
            Matcher matcher = pattern.matcher(pathPart);

            if (matcher.find()) {
                String host = matcher.group(1);
                String port = matcher.group(2);
                String database = matcher.group(3);

                if (host != null && !host.isEmpty()) {
                    Config.dbhost = host;
                    logger.debug("Extracted dbhost: {}", host);
                }

                if (port != null && !port.isEmpty()) {
                    Config.dbport = port;
                    logger.debug("Extracted dbport: {}", port);
                }

                if (database != null && !database.isEmpty()) {
                    Config.dbname = database;
                    logger.debug("Extracted dbname: {}", database);
                }
            }

        } catch (Exception e) {
            logger.warn("Failed to parse MySQL URL: {}", e.getMessage());
        }
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
