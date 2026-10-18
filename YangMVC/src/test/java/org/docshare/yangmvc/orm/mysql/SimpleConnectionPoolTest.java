package org.docshare.yangmvc.orm.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.docshare.yangmvc.mvc.Config;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 内置连接池：借出/归还/超时。用覆盖 createConnection() 的方式避免真连数据库。
 */
public class SimpleConnectionPoolTest {

	private final int oldSize = Config.db_pool_size;
	private final int oldTimeout = Config.db_pool_timeout_ms;
	private final int oldValid = Config.db_valid_interval_ms;
	private final int oldIdle = Config.db_pool_idle_ms;

	@After
	public void restore() {
		Config.db_pool_size = oldSize;
		Config.db_pool_timeout_ms = oldTimeout;
		Config.db_valid_interval_ms = oldValid;
		Config.db_pool_idle_ms = oldIdle;
	}

	@Test
	public void idleConnectionIsDiscardedAfterIdleTimeout() throws Exception {
		Config.db_pool_size = 1;
		Config.db_pool_timeout_ms = 1000;
		Config.db_valid_interval_ms = 30000;
		Config.db_pool_idle_ms = 1;

		final AtomicInteger created = new AtomicInteger();
		SimpleConnectionPool pool = new SimpleConnectionPool() {
			@Override
			protected Connection createConnection() throws SQLException {
				created.incrementAndGet();
				Connection c = Mockito.mock(Connection.class);
				Mockito.when(c.isClosed()).thenReturn(false);
				return c;
			}
		};

		Connection c1 = pool.getConnection();
		pool.release(c1);
		Thread.sleep(10); // > db_pool_idle_ms

		Connection c2 = pool.getConnection();
		assertNotSame("空闲超时的连接应该被丢弃，重新创建", c1, c2);
		assertEquals(2, created.get());
		pool.release(c2);
		pool.close();
	}

	@Test
	public void reuseAfterReleaseAndTimeoutWhenExhausted() throws Exception {
		Config.db_pool_size = 1;
		Config.db_pool_timeout_ms = 150;
		Config.db_valid_interval_ms = 30000;

		final AtomicInteger created = new AtomicInteger();
		SimpleConnectionPool pool = new SimpleConnectionPool() {
			@Override
			protected Connection createConnection() throws SQLException {
				created.incrementAndGet();
				Connection c = Mockito.mock(Connection.class);
				Mockito.when(c.isClosed()).thenReturn(false);
				return c;
			}
		};

		Connection c1 = pool.getConnection();

		try {
			pool.getConnection(); // 池大小=1 且没有空闲 → 超时
			fail("should be exhausted");
		} catch (SQLException expected) {
			// ok
		}

		pool.release(c1);
		Connection c2 = pool.getConnection();
		assertSame("归还后应该复用同一个连接", c1, c2);
		assertEquals(1, created.get());
		assertEquals(1, pool.totalCount());

		pool.release(c2);
	}
}
