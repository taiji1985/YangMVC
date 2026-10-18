package org.docshare.yangmvc.orm.mysql;

import java.sql.Connection;

/**
 * 可归还连接的连接池扩展接口。
 *
 * <p>{@link org.docshare.yangmvc.orm.DBHelper#close()} 在关闭连接时，
 * 如果底层池实现了本接口，就调用 {@link #release(Connection)} 归还连接，而不是直接 close。
 *
 * <p>这样 {@code MySQLDBHelper.close()} 在 {@code db_close_per_request=true} 时
 * 才能把连接还给池（请求级借还）。
 */
public interface IConnectionPool2 extends IConnectionPool {

	/**
	 * 归还一个连接（实现方负责校验/关闭/复用）。
	 */
	void release(Connection con);
}
