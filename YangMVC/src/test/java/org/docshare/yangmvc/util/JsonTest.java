package org.docshare.yangmvc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * fastjson 1.x -> 2.x 的输出口径必须保持不变：
 * <ul>
 *   <li>{@code java.util.Date} 仍然输出毫秒数（前端/客户端按数字解析 update_tm）；</li>
 *   <li>{@code LocalDateTime} 仍然输出 {@code yyyy-MM-dd'T'HH:mm:ss}。</li>
 * </ul>
 */
public class JsonTest {

	@Test
	public void dateIsMillis() {
		assertEquals("1792201495123", Json.toJson(new Date(1792201495123L)));
	}

	@Test
	public void localDateTimeKeepsIsoT() {
		LocalDateTime ldt = LocalDateTime.of(2026, 10, 18, 13, 47, 22);
		assertEquals("\"2026-10-18T13:47:22\"", Json.toJson(ldt));
	}

	@Test
	public void localDateTimeKeepsMillis() {
		LocalDateTime ldt = LocalDateTime.of(2026, 10, 18, 13, 47, 22, 123000000);
		assertEquals("\"2026-10-18T13:47:22.123\"", Json.toJson(ldt));
	}

	@Test
	public void mixedMap() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		m.put("dt", new Date(1792201495123L));
		m.put("ldt", LocalDateTime.of(2026, 10, 18, 13, 47, 22));
		m.put("n", null);
		m.put("s", "x");

		String json = Json.toJson(m);
		assertTrue(json, json.contains("\"dt\":1792201495123"));
		assertTrue(json, json.contains("\"ldt\":\"2026-10-18T13:47:22\""));
		assertTrue(json, json.contains("\"s\":\"x\""));
		// fastjson 默认不输出 null 字段
		assertTrue(json, !json.contains("\"n\""));
	}

	@Test
	public void prettyFormat() {
		assertTrue(Json.toJson(new String[] { "a", "b" }, true).contains("\n"));
	}
}
