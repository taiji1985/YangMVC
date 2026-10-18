package org.docshare.yangmvc.orm.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docshare.yangmvc.orm.Model;
import org.junit.Test;

/**
 * PostgreSQL 的 INSERT 构建：putNull 写 NULL，普通 null/空串跳过。
 */
public class PostgresInsertSqlTest {

	@SuppressWarnings("unchecked")
	private static Model model(String table, String... cols) throws Exception {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		for (String c : cols) {
			m.put(c, null);
		}
		Constructor<Model> ctor = Model.class.getDeclaredConstructor(String.class, Map.class);
		ctor.setAccessible(true);
		return ctor.newInstance(table, m);
	}

	@Test
	public void putNullBecomesLiteralNull() throws Exception {
		Model m = model("t", "id", "a", "b");
		m.putNull("a");
		m.put("b", null);

		List<Object> params = new ArrayList<Object>();
		String sql = PostgresDelegate.buildInsertSql(m, params);

		assertTrue(sql, sql.contains("\"a\""));
		assertTrue(sql, sql.contains("NULL"));
		assertFalse("普通 null 跳过", sql.contains("\"b\""));
		assertEquals(0, params.size());
	}

	@Test
	public void valueUsesPlaceholder() throws Exception {
		Model m = model("t", "a");
		m.put("a", "x");

		List<Object> params = new ArrayList<Object>();
		String sql = PostgresDelegate.buildInsertSql(m, params);

		assertTrue(sql, sql.contains("?"));
		assertEquals(1, params.size());
		assertEquals("x", params.get(0));
	}
}
