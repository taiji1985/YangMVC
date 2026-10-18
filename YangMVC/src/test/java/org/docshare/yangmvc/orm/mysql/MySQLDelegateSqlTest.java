package org.docshare.yangmvc.orm.mysql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.orm.Model;
import org.junit.After;
import org.junit.Test;

/**
 * 直接测 SQL 构建（不需要数据库）：
 * <ul>
 *   <li>UPDATE 的 WHERE 主键走 {@code ?} 参数，不再拼字面量（字符串主键带引号也不会坏）；</li>
 *   <li>{@code putNull(col)} 在 INSERT / UPDATE 里都写 NULL（与 db_update_null 无关）；</li>
 *   <li>普通 {@code put(col, null)} 在 INSERT 里跳过（用默认值），UPDATE 里默认跳过、开关打开时写 NULL。</li>
 * </ul>
 */
public class MySQLDelegateSqlTest {

	private final boolean oldUpdateNull = Config.db_update_null;

	@After
	public void restore() {
		Config.db_update_null = oldUpdateNull;
	}

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
	public void insertWritesExplicitNullAndSkipsPlainNull() throws Exception {
		Model m = model("question", "id", "video_url", "title");
		m.put("video_url", null);
		m.putNull("video_url");       // 显式置空
		m.put("title", null);         // 普通 null：跳过（用默认值）

		List<Object> params = new ArrayList<Object>();
		String sql = MySQLDelegate.buildInsertSql(m, params);

		assertTrue("应该包含 video_url: " + sql, sql.contains("`video_url`"));
		assertTrue("显式置空应该写 NULL: " + sql, sql.contains("NULL"));
		assertFalse("普通 null 的列不应该出现: " + sql, sql.contains("`title`"));
		assertEquals("NULL 不占参数位", 0, params.size());
	}

	@Test
	public void insertWithValueUsesPlaceholder() throws Exception {
		Model m = model("question", "id", "title");
		m.put("title", "hello");

		List<Object> params = new ArrayList<Object>();
		String sql = MySQLDelegate.buildInsertSql(m, params);

		assertTrue(sql.contains("`title`"));
		assertTrue(sql.contains("?"));
		assertEquals(1, params.size());
		assertEquals("hello", params.get(0));
	}

	@Test
	public void updateParameterizesPrimaryKey() throws Exception {
		Model m = model("trade", "id", "amount");
		m.put("id", "20261018000001");
		m.put("amount", "30.00");

		List<Object> params = new ArrayList<Object>();
		String sql = MySQLDelegate.buildUpdateSql(m, "id", "20261018000001", params);

		assertNotNull(sql);
		assertTrue("WHERE 必须是占位符: " + sql, sql.contains("where `id` = ?"));
		assertFalse("不能再拼字面量: " + sql, sql.contains("'20261018000001'"));
		assertEquals("先 set 参数，最后是主键", 2, params.size());
		assertEquals("30.00", params.get(0));
		assertEquals("20261018000001", params.get(1));
	}

	@Test
	public void updateWithAllNullReturnsNull() throws Exception {
		Model m = model("question", "id", "title");
		m.put("id", 9);
		m.put("title", null);

		List<Object> params = new ArrayList<Object>();
		assertNull("没有可更新列时返回 null（调用方返回 0）", MySQLDelegate.buildUpdateSql(m, "id", 9, params));
	}

	@Test
	public void putNullWorksEvenWhenDbUpdateNullIsOff() throws Exception {
		Config.db_update_null = false;
		Model m = model("question", "id", "video_url");
		m.put("id", 9);
		m.putNull("video_url");

		List<Object> params = new ArrayList<Object>();
		String sql = MySQLDelegate.buildUpdateSql(m, "id", 9, params);

		assertNotNull(sql);
		assertTrue("putNull 是显式意图，应该直接写 NULL: " + sql, sql.contains("`video_url`=NULL"));
		assertEquals(1, params.size()); // 只有主键
		assertEquals(9, params.get(0));
	}

	@Test
	public void plainNullBecomesNullOnlyWhenSwitchIsOn() throws Exception {
		Model m = model("question", "id", "video_url");
		m.put("id", 9);
		m.put("video_url", null);

		List<Object> params = new ArrayList<Object>();
		Config.db_update_null = false;
		assertNull("开关关着时，普通 null 不产生更新", MySQLDelegate.buildUpdateSql(m, "id", 9, params));

		Config.db_update_null = true;
		params = new ArrayList<Object>();
		String sql = MySQLDelegate.buildUpdateSql(m, "id", 9, params);
		assertNotNull(sql);
		assertTrue("开关打开后普通 null 写 NULL: " + sql, sql.contains("`video_url`=NULL"));
	}

	@Test
	public void extraFieldsAreIgnoredInUpdate() throws Exception {
		Model m = model("usr", "id", "name");
		m.put("id", 1);
		m.put("not_a_column", "x"); // 进 extra

		List<Object> params = new ArrayList<Object>();
		assertNull("extra 字段不应产生 SQL", MySQLDelegate.buildUpdateSql(m, "id", 1, params));
	}
}
