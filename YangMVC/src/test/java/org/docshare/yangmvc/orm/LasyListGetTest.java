package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.sql.ResultSet;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * LasyList 的 List 契约：越界抛 IndexOutOfBoundsException；
 * 兼容旧行为用 getOrNull()；one() 空结果返回 null。
 */
public class LasyListGetTest {

	private static LasyList emptyList() throws Exception {
		DBTool tool = Mockito.mock(DBTool.class);
		IDBDelegate delegate = Mockito.mock(IDBDelegate.class);
		Mockito.when(tool.getDelegate()).thenReturn(delegate);

		ResultSet rs = Mockito.mock(ResultSet.class);
		Mockito.when(rs.next()).thenReturn(false);
		Mockito.when(delegate.runSQL(Mockito.anyList(), Mockito.anyList(),
				Mockito.nullable(SQLConstains.class), Mockito.any(DBTool.class), Mockito.anyString(),
				Mockito.anyString(), Mockito.nullable(Object[].class))).thenReturn(rs);

		return new LasyList(tool, "t");
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getOutOfRangeThrows() throws Exception {
		emptyList().get(0);
	}

	@Test
	public void getOrNullKeepsOldBehavior() throws Exception {
		assertNull(emptyList().getOrNull(0));
	}

	@Test
	public void oneReturnsNullOnEmptyResult() throws Exception {
		assertNull(emptyList().one());
	}

	@Test
	public void getNegativeThrows() throws Exception {
		try {
			emptyList().get(-1);
			fail("should throw");
		} catch (IndexOutOfBoundsException expected) {
			// ok
		}
	}
}
