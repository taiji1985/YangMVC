package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * {@link DBHelper#getPrepareRS(String, Object, Object)} 旧实现是
 * {@code twoArray.clear(); set(0,a); set(0,b);} —— clear 之后 set(0) 直接抛
 * IndexOutOfBoundsException，而且下标写错（第二个参数被丢掉）。
 */
public class DBHelperPreparedArgsTest {

	private Connection con;
	private PreparedStatement ps;

	@After
	public void cleanup() {
		DBHelper.removeThreadLocal();
	}

	@Test
	public void twoArgsAreBoundToPositionsOneAndTwo() throws Exception {
		con = Mockito.mock(Connection.class);
		ps = Mockito.mock(PreparedStatement.class);
		Mockito.when(con.prepareStatement(Mockito.anyString())).thenReturn(ps);

		DBHelper helper = new DBHelper() {
			@Override
			public void conn() {
				// 不真连数据库
			}
		};
		helper.con = con;

		assertNull(helper.getPrepareRS("select ? , ?", "a", "b"));

		Mockito.verify(ps).setObject(1, "a");
		Mockito.verify(ps).setObject(2, "b");
	}
}
