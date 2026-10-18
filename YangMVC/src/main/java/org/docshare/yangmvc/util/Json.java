package org.docshare.yangmvc.util;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;

/**
 * JSON 输出的统一入口（fastjson2）。
 *
 * <p>从 fastjson 1.x 迁到 fastjson2 有两个默认行为变化，会破坏已有接口契约：
 * <ul>
 *   <li>{@code java.util.Date}/{@code Timestamp}：1.x 默认输出**毫秒数**，2.x 默认输出字符串；</li>
 *   <li>{@code LocalDateTime}：1.x 输出 {@code 2026-10-18T13:47:22}，2.x 输出空格分隔。</li>
 * </ul>
 * 这里统一保持 1.x 的口径：
 * <ul>
 *   <li>Date 用 {@link JSONWriter.Feature#WriterUtilDateAsMillis}（毫秒数）；</li>
 *   <li>LocalDateTime 注册一个输出 {@code yyyy-MM-dd'T'HH:mm:ss[.SSS]} 的 writer。</li>
 * </ul>
 * 业务代码请优先用 {@link #toJson(Object)} / {@link #toJson(Object, boolean)}，
 * 不要直接调 {@code JSON.toJSONString}，否则日期格式会变。
 */
public final class Json {

	private static final DateTimeFormatter LDT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	static {
		JSONFactory.getDefaultObjectWriterProvider().register(LocalDateTime.class, new ObjectWriter<LocalDateTime>() {
			@Override
			public void write(JSONWriter writer, Object object, Object fieldName, Type fieldType, long features) {
				if (object == null) {
					writer.writeNull();
					return;
				}
				LocalDateTime ldt = (LocalDateTime) object;
				String s = LDT_FMT.format(ldt);
				int nano = ldt.getNano();
				if (nano != 0) {
					s = s + "." + String.format("%03d", nano / 1000000);
				}
				writer.writeString(s);
			}
		});
	}

	private Json() {
	}

	/** 与 fastjson 1.x 默认一致：Date 输出毫秒数。 */
	public static String toJson(Object obj) {
		return JSON.toJSONString(obj, JSONWriter.Feature.WriterUtilDateAsMillis);
	}

	public static String toJson(Object obj, boolean pretty) {
		if (!pretty) {
			return toJson(obj);
		}
		return JSON.toJSONString(obj, JSONWriter.Feature.WriterUtilDateAsMillis, JSONWriter.Feature.PrettyFormat);
	}

	/** 指定日期格式（等价于 fastjson 1.x 的 {@code WriteDateUseDateFormat}）。 */
	public static String toJsonWithDateFormat(Object obj, String dateFormat, boolean pretty) {
		if (pretty) {
			return JSON.toJSONString(obj, dateFormat, JSONWriter.Feature.PrettyFormat);
		}
		return JSON.toJSONString(obj, dateFormat);
	}
}
