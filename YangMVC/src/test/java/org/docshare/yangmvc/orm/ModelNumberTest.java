package org.docshare.yangmvc.orm;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Model 的数值取值：线上有 double / decimal / bigint 列（rate、price、amount、id...），
 * 旧实现一律 `Integer.parseInt(value+"")`，遇到 "95.0" 或超 int 的 bigint 就抛 NumberFormatException。
 */
public class ModelNumberTest {

	@SuppressWarnings("unchecked")
	private static Model model(String... cols) throws Exception {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		for (String c : cols) {
			m.put(c, null);
		}
		Constructor<Model> ctor = Model.class.getDeclaredConstructor(String.class, Map.class);
		ctor.setAccessible(true);
		return ctor.newInstance("t", m);
	}

	@Test
	public void getIntTruncatesDouble() throws Exception {
		Model m = model("rate");
		m.put("rate", Double.valueOf(95.9));
		assertEquals(Integer.valueOf(95), m.getInt("rate"));
	}

	@Test
	public void getIntAcceptsDecimalString() throws Exception {
		Model m = model("rate");
		m.put("rate", "95.0");
		assertEquals(Integer.valueOf(95), m.getInt("rate"));
	}

	@Test
	public void getIntAcceptsBigDecimal() throws Exception {
		Model m = model("score");
		m.put("score", new BigDecimal("12.7"));
		assertEquals(Integer.valueOf(12), m.getInt("score"));
	}

	@Test(expected = ArithmeticException.class)
	public void getIntRejectsOutOfRangeBigint() throws Exception {
		Model m = model("id");
		m.put("id", Long.valueOf(3000000000L)); // bigint 超过 int 范围：不能悄悄截断成负数
		m.getInt("id");
	}

	@Test
	public void getLongHandlesDoubleAndString() throws Exception {
		Model m = model("a", "b");
		m.put("a", Double.valueOf(3000000000.9));
		m.put("b", "3000000000");
		assertEquals(Long.valueOf(3000000000L), m.getLong("a"));
		assertEquals(Long.valueOf(3000000000L), m.getLong("b"));
	}

	@Test
	public void getDoubleHandlesBigDecimalAndString() throws Exception {
		Model m = model("a", "b");
		m.put("a", new BigDecimal("30.00"));
		m.put("b", "12.5");
		assertEquals(Double.valueOf(30.0), m.getDouble("a"));
		assertEquals(Double.valueOf(12.5), m.getDouble("b"));
	}

	@Test(expected = NumberFormatException.class)
	public void nonNumberStillThrows() throws Exception {
		Model m = model("a");
		m.put("a", "abc");
		m.getInt("a");
	}
}
