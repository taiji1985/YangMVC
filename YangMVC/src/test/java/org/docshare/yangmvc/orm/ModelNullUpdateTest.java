package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

import org.docshare.yangmvc.orm.mysql.MySQLDelegate;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * P0-3：更新时"改动的列全是 null"不能再拼出 {@code update t set  where id=?} 这种非法 SQL，
 * 并且要能显式把列置为 NULL。
 */
public class ModelNullUpdateTest {

	@SuppressWarnings("unchecked")
	private static Model newModel() throws Exception {
		Constructor<Model> c = Model.class.getDeclaredConstructor(String.class, Map.class);
		c.setAccessible(true);
		Map<String, Object> cols = new LinkedHashMap<String, Object>();
		cols.put("id", null);
		cols.put("video_url", null);
		cols.put("title", null);
		return c.newInstance("question", cols);
	}

	@Test
	public void putNullMarksColumn() throws Exception {
		Model m = newModel();
		m.putNull("video_url");

		assertTrue(m.hasColumn("video_url"));
		assertTrue(m.isNullColumn("video_url"));
		assertTrue(m.changeColumns().contains("video_url"));
	}

	@Test
	public void puttingARealValueClearsNullMark() throws Exception {
		Model m = newModel();
		m.putNull("video_url");
		m.put("video_url", "http://x/v.mp4");

		assertFalse(m.isNullColumn("video_url"));
	}

	@Test
	public void putNullOnNonColumnGoesToExtra() throws Exception {
		Model m = newModel();
		m.putNull("not_a_column");

		assertFalse(m.hasColumn("not_a_column"));
		assertEquals(null, m.get("not_a_column"));
	}

	@Test
	public void allChangedValuesAreNullUpdateIsNoOp() throws Exception {
		Model m = newModel();
		m.put("id", 9);          // 主键有值 → 走 update 分支
		m.put("title", null);    // 唯一的改动是 null

		DBTool tool = Mockito.mock(DBTool.class);
		MySQLDelegate delegate = new MySQLDelegate();

		// 旧实现会走到 "update `question` set  where id=9" 然后抛 SQL 异常；
		// 新实现应当直接返回 0（不碰数据库）。
		int ret = delegate.save(tool, m, "id", false);
		assertEquals(0, ret);
	}

	@Test
	public void putNullWithoutDbUpdateNullIsSkipped() throws Exception {
		Model m = newModel();
		m.put("id", 9);
		m.putNull("video_url");

		boolean old = org.docshare.yangmvc.mvc.Config.db_update_null;
		org.docshare.yangmvc.mvc.Config.db_update_null = false;
		try {
			DBTool tool = Mockito.mock(DBTool.class);
			MySQLDelegate delegate = new MySQLDelegate();
			assertEquals("默认行为：置空请求被忽略（保持旧行为）", 0, delegate.save(tool, m, "id", false));
		} finally {
			org.docshare.yangmvc.mvc.Config.db_update_null = old;
		}
	}
}
