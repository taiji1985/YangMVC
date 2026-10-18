package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import org.docshare.yangmvc.mvc.Config;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 语句池：旧实现无上限、永不 close；现在按 {@link Config#db_statement_cache_size} 做 LRU 并关闭淘汰项。
 */
public class StatementPoolTest {

	private final int oldSize = Config.db_statement_cache_size;

	@After
	public void restore() {
		Config.db_statement_cache_size = oldSize;
	}

	@Test
	public void evictsAndClosesWhenOverLimit() throws Exception {
		Config.db_statement_cache_size = 2;

		Connection con = Mockito.mock(Connection.class);
		final Map<String, PreparedStatement> created = new HashMap<String, PreparedStatement>();
		Mockito.when(con.prepareStatement(Mockito.anyString(), Mockito.anyInt())).thenAnswer(inv -> {
			String sql = (String) inv.getArgument(0);
			PreparedStatement ps = Mockito.mock(PreparedStatement.class);
			created.put(sql, ps);
			return ps;
		});

		StatementPool pool = new StatementPool();
		assertEquals(created.size(), 0);

		pool.get(con, "sql-a");
		pool.get(con, "sql-b");
		assertEquals(2, pool.size());

		pool.get(con, "sql-c"); // 超限，淘汰最久未用的 sql-a
		assertEquals(2, pool.size());
		Mockito.verify(created.get("sql-a")).close();
		Mockito.verify(created.get("sql-b"), Mockito.never()).close();
		Mockito.verify(created.get("sql-c"), Mockito.never()).close();

		pool.clear();
		assertEquals(0, pool.size());
		Mockito.verify(created.get("sql-b")).close();
		Mockito.verify(created.get("sql-c")).close();
	}
}
