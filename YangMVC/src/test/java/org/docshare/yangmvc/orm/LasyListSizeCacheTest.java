package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * LasyList.size() 的结果缓存：同一组约束下多次调用只查一次 count(*)；
 * 加约束后必须失效重算。
 */
public class LasyListSizeCacheTest {

	@Test
	public void sizeIsCachedUntilConstraintChanges() {
		DBTool tool = Mockito.mock(DBTool.class);
		IDBDelegate delegate = Mockito.mock(IDBDelegate.class);
		Mockito.when(tool.getDelegate()).thenReturn(delegate);
		Mockito.when(delegate.size(Mockito.anyList(), Mockito.any(DBTool.class), Mockito.anyString(),
				Mockito.nullable(Object[].class))).thenReturn(5L);

		LasyList list = new LasyList(tool, "t");

		assertEquals(5, list.size());
		assertEquals(5, list.size());
		Mockito.verify(delegate, Mockito.times(1)).size(Mockito.anyList(), Mockito.any(DBTool.class),
				Mockito.anyString(), Mockito.nullable(Object[].class));

		list.eq("a", 1); // 加约束 → 缓存失效
		assertEquals(5, list.size());
		Mockito.verify(delegate, Mockito.times(2)).size(Mockito.anyList(), Mockito.any(DBTool.class),
				Mockito.anyString(), Mockito.nullable(Object[].class));
	}
}
