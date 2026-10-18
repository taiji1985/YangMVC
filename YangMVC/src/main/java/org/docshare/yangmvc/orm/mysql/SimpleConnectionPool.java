package org.docshare.yangmvc.orm.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.docshare.yangmvc.mvc.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 内置的轻量连接池（opt-in）。
 *
 * <p>用法（web.properties）：
 * <pre>
 * conn_pool=org.docshare.yangmvc.orm.mysql.SimpleConnectionPool
 * db_close_per_request=true
 * db_pool_size=16
 * db_pool_timeout_ms=5000
 * </pre>
 *
 * <p>为什么需要：{@code DBHelper} 是"一个线程一条连接"，Jetty 线程数（~200）可能超过
 * MySQL 的 max_connections（默认 151）。开启请求级借还后，连接数被压到池大小。
 *
 * <p>不做的事：不处理事务重放、不做分布式、不保证公平性——够用即可。
 */
public class SimpleConnectionPool implements IConnectionPool2 {
	private static final Logger logger = LoggerFactory.getLogger(SimpleConnectionPool.class);

	private final LinkedBlockingDeque<Idle> idle = new LinkedBlockingDeque<Idle>();
	private final AtomicInteger total = new AtomicInteger(0);

	private static final class Idle {
		final Connection con;
		final long since;

		Idle(Connection con, long since) {
			this.con = con;
			this.since = since;
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		long deadline = System.currentTimeMillis() + Math.max(1, Config.db_pool_timeout_ms);
		int max = Math.max(1, Config.db_pool_size);

		while (true) {
			Idle i = idle.pollFirst();
			if (i != null) {
				if (isUsable(i)) {
					return i.con;
				}
				discard(i.con);
				continue;
			}
			if (total.get() < max) {
				int now = total.incrementAndGet();
				if (now <= max) {
					try {
						return createConnection();
					} catch (SQLException e) {
						total.decrementAndGet();
						throw e;
					}
				}
				total.decrementAndGet(); // 并发下别人先占了名额
			}
			long wait = deadline - System.currentTimeMillis();
			if (wait <= 0) {
				throw new SQLException("connection pool exhausted (size=" + max
						+ ", timeout=" + Config.db_pool_timeout_ms + "ms)");
			}
			try {
				i = idle.pollFirst(wait, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new SQLException("interrupted while waiting for a connection", e);
			}
			if (i == null) {
				throw new SQLException("connection pool exhausted (size=" + max
						+ ", timeout=" + Config.db_pool_timeout_ms + "ms)");
			}
			if (isUsable(i)) {
				return i.con;
			}
			discard(i.con);
		}
	}

	@Override
	public void release(Connection con) {
		if (con == null) {
			return;
		}
		try {
			if (con.isClosed() || total.get() > Math.max(1, Config.db_pool_size)) {
				discard(con);
				return;
			}
			if (!idle.offerFirst(new Idle(con, System.currentTimeMillis()))) {
				discard(con);
			}
		} catch (Exception e) {
			discard(con);
		}
	}

	/** 池里空闲连接数（测试/监控用） */
	public int idleCount() {
		return idle.size();
	}

	/** 当前已创建的连接数（测试/监控用） */
	public int totalCount() {
		return total.get();
	}

	private boolean isUsable(Idle i) {
		try {
			if (i.con.isClosed()) {
				return false;
			}
			long idleMs = System.currentTimeMillis() - i.since;
			// 空闲太久直接丢弃（让数据库回收）
			if (idleMs > Math.max(0, Config.db_pool_idle_ms)) {
				return false;
			}
			// 空闲超过探活时间窗才 ping，避免每次借出都多一个往返
			if (idleMs > Config.db_valid_interval_ms) {
				return i.con.isValid(2);
			}
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	/**
	 * 关闭所有空闲连接（应用停止/测试用）。
	 */
	public void close() {
		Idle i;
		while ((i = idle.pollFirst()) != null) {
			discard(i.con);
		}
	}

	private void discard(Connection con) {
		total.decrementAndGet();
		try {
			con.close();
		} catch (Exception ignored) {
		}
	}

	/**
	 * 真正创建连接；测试里可以覆盖它返回 mock。
	 */
	protected Connection createConnection() throws SQLException {
		Connection c = DriverManager.getConnection(ConnectionPool.buildUrl(), Config.dbusr, Config.dbpwd);
		logger.debug("SimpleConnectionPool: created connection, total={}", total.get());
		return c;
	}
}
