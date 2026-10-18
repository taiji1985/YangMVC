package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docshare.yangmvc.mvc.Config;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 第三批：
 * <ul>
 *   <li>{@code columnFilter} 现在真的只让 Model 持有选中的列（JSON 里不再有一堆 null）；</li>
 *   <li>{@code Model.entrySet()} 改成活视图（不再每次分配 HashSet）；</li>
 *   <li>{@code Model.get()} 的外键查询遇到"元数据缺失/没有 joined tool"时返回 null，不再 NPE。</li>
 * </ul>
 */
public class OrmMiscTest {

	private final boolean oldFk = Config.model_fk_lookup;

	@After
	public void restore() {
		Config.model_fk_lookup = oldFk;
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
	public void columnFilterOnlyKeepsSelectedColumns() throws Exception {
		DBTool tool = Mockito.mock(DBTool.class, Mockito.CALLS_REAL_METHODS);
		ResultSet rs = Mockito.mock(ResultSet.class);
		Mockito.when(rs.getObject("file")).thenReturn("a.docx");

		Map<String, Object> all = new LinkedHashMap<String, Object>();
		all.put("id", null);
		all.put("file", null);
		all.put("ans", null);

		Set<String> filter = new LinkedHashSet<String>(Arrays.asList("file"));

		Model m = tool.db2Table(rs, all, filter);

		assertEquals(1, m.keySet().size());
		assertTrue(m.containsKey("file"));
		assertFalse("未选中的列不应该出现在 Model 里", m.containsKey("id"));
		assertFalse(m.containsKey("ans"));
		assertEquals("a.docx", m.get("file"));
	}

	@Test
	public void entrySetIsLiveAndContainsExtras() throws Exception {
		Model m = model("t", "a", "b");
		m.put("a", 1);

		Set<Map.Entry<String, Object>> es = m.entrySet();
		assertEquals(2, es.size());

		m.put("c", 3); // extra
		assertEquals(3, m.entrySet().size());

		List<String> keys = new ArrayList<String>();
		for (Map.Entry<String, Object> e : m.entrySet()) {
			keys.add(e.getKey());
		}
		assertTrue(keys.contains("a"));
		assertTrue(keys.contains("b"));
		assertTrue(keys.contains("c"));
	}

	@Test
	public void containsValueWorks() throws Exception {
		Model m = model("t", "a", "b");
		m.put("a", "hello");
		assertTrue(m.containsValue("hello"));
		assertFalse(m.containsValue("nope"));
	}

	@Test
	public void modifyingValuesWhileIteratingEntrySetIsAllowed() throws Exception {
		Model m = model("t", "a", "b");
		m.put("a", "1");
		m.put("b", "2");

		// 正常操作：边迭代边改 Model 的值，不应该抛 ConcurrentModificationException
		for (Map.Entry<String, Object> e : m.entrySet()) {
			m.put(e.getKey(), "v-" + e.getKey());
		}

		assertEquals("v-a", m.getStr("a"));
		assertEquals("v-b", m.getStr("b"));
	}

	@Test
	public void addingExtraWhileIteratingIsAllowed() throws Exception {
		Model m = model("t", "a");
		m.put("a", 1);

		for (Map.Entry<String, Object> e : m.entrySet()) {
			m.put("extra", "x"); // 进 extra，也不应该影响本次迭代
		}

		assertEquals("x", m.getStr("extra"));
	}

	@Test
	public void fastjsonSerializationStillWorks() throws Exception {
		Model m = model("t", "a", "b");
		m.put("a", "hello");
		m.put("b", 2);
		m.put("c", "extra");

		String json = com.alibaba.fastjson2.JSON.toJSONString(m);
		assertTrue(json, json.contains("\"a\":\"hello\""));
		assertTrue(json, json.contains("\"b\":2"));
		assertTrue(json, json.contains("\"c\":\"extra\""));
	}

	@Test
	public void foreignKeyLookupDoesNotNpeWithoutMetadata() throws Exception {
		Model m = model("t", "id", "user_id");
		m.put("user_id", 7);
		// 反射构造的 Model 没有 joined_tool：旧实现会 NPE
		assertNull(m.get("user"));
	}

	@Test
	public void foreignKeyLookupCanBeDisabled() throws Exception {
		Config.model_fk_lookup = false;
		Model m = model("t", "id", "user_id");
		m.put("user_id", 7);
		assertNull(m.get("user"));
	}
}
