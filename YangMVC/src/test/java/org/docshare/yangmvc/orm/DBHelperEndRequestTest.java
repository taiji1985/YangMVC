package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.Connection;

import org.docshare.yangmvc.mvc.Config;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * P0-5：请求结束时必须兜住"开了事务却没提交"的连接，
 * 否则同一线程后续请求的写入都不会提交（偶发丢写/锁表）。
 */
public class DBHelperEndRequestTest {

	private static DBHelper helperWith(Connection con) {
		DBHelper h = new DBHelper() {
			@Override
			public void conn() {
				// no-op：测试里不连数据库
			}
		};
		h.con = con;
		return h;
	}

	@After
	public void cleanup() {
		DBHelper.removeThreadLocal();
		Config.db_close_per_request = false;
	}

	@Test
	public void uncommittedTransactionIsRolledBack() throws Exception {
		Connection con = Mockito.mock(Connection.class);
		Mockito.when(con.isClosed()).thenReturn(false);
		Mockito.when(con.getAutoCommit()).thenReturn(false);
		DBHelper h = helperWith(con);
		DBHelper.locals.set(h);

		DBHelper.endRequest();

		Mockito.verify(con).rollback();
		Mockito.verify(con).setAutoCommit(true);
		// 默认不关闭连接：保持"线程内复用连接"的旧行为
		Mockito.verify(con, Mockito.never()).close();
		assertSame(h, DBHelper.locals.get());
	}

	@Test
	public void normalRequestDoesNotRollback() throws Exception {
		Connection con = Mockito.mock(Connection.class);
		Mockito.when(con.isClosed()).thenReturn(false);
		Mockito.when(con.getAutoCommit()).thenReturn(true);
		DBHelper.locals.set(helperWith(con));

		DBHelper.endRequest();

		Mockito.verify(con, Mockito.never()).rollback();
		Mockito.verify(con, Mockito.never()).close();
	}

	@Test
	public void closePerRequestReturnsConnection() throws Exception {
		Connection con = Mockito.mock(Connection.class);
		Mockito.when(con.isClosed()).thenReturn(false);
		Mockito.when(con.getAutoCommit()).thenReturn(true);
		DBHelper.locals.set(helperWith(con));

		Config.db_close_per_request = true;
		DBHelper.endRequest();

		Mockito.verify(con).close();
		assertNull(DBHelper.locals.get());
	}

	@Test
	public void noDbUsedIsNoOp() {
		DBHelper.endRequest(); // 不应抛异常
		assertNull(DBHelper.locals.get());
	}

	@Test
	public void commitAndRollbackWithoutConnectionDoNotThrow() {
		DBHelper h = new DBHelper() {
			@Override
			public void conn() {
				// no-op
			}
		};
		h.commit();
		h.rollback();
	}
}
