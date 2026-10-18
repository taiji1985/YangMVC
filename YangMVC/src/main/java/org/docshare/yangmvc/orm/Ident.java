package org.docshare.yangmvc.orm;

import java.util.Set;

import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.mvc.except.MVCException;

/**
 * SQL 标识符（列名/排序列）校验。
 *
 * <p>ORM 里列名是直接拼进 SQL 的（`... where \`列名\` = ?`），而值才是参数化的。
 * 因此只要列名来自请求参数（例如通用列表接口把 {@code s_xxx} 的 xxx 当列名），
 * 攻击者就能用反引号/引号/分号把任意 SQL 拼进去。
 *
 * <p>这里用的是"拦危险字符"而不是"只允许字母数字"：中文列名、带下划线等正常列名都能通过，
 * 含 {@code ` ' " ; ( ) 空格 注释符} 的一律拒绝。{@link #assertColumn} 额外支持
 * {@link Config#ident_strict}（要求列名必须存在于表元数据中）。
 */
public final class Ident {

	private static final char[] FORBIDDEN = { '`', '\'', '"', ';', '(', ')', ' ', '\t', '\r', '\n', '/', '\\' };

	private Ident() {
	}

	/**
	 * 校验单个列名，非法时抛 {@link MVCException}。
	 */
	public static void assertSafe(String column) {
		if (column == null || column.trim().isEmpty()) {
			throw new MVCException("empty column name");
		}
		String c = column.trim();
		if (c.contains("--") || c.contains("/*") || c.contains("*/") || c.contains("#")) {
			throw new MVCException("illegal column name: " + column);
		}
		for (char f : FORBIDDEN) {
			if (c.indexOf(f) >= 0) {
				throw new MVCException("illegal column name: " + column);
			}
		}
	}

	/**
	 * 校验逗号分隔的多个列名（{@code LasyList.mlike} 用）。
	 */
	public static void assertSafeList(String columns) {
		if (columns == null) {
			throw new MVCException("empty column list");
		}
		for (String c : columns.split(",")) {
			assertSafe(c);
		}
	}

	/**
	 * 校验列名，并按 {@link Config#ident_strict} 决定是否要求它存在于表元数据中。
	 *
	 * @param tool   对应的表工具，可为 null（rawSql 场景）
	 * @param column 列名
	 */
	public static void assertColumn(DBTool tool, String column) {
		assertSafe(column);
		if (!Config.ident_strict || tool == null) {
			return;
		}
		Set<String> cols = tool.listColumns();
		if (cols == null || cols.isEmpty()) {
			// fromRawSql 之类的场景没有表元数据，跳过存在性校验
			return;
		}
		if (!cols.contains(column.trim())) {
			throw new MVCException("unknown column: " + column);
		}
	}

	/**
	 * 校验逗号分隔的多个列名（{@code LasyList.mlike} 用，含 strict 校验）。
	 */
	public static void assertColumnList(DBTool tool, String columns) {
		if (columns == null) {
			throw new MVCException("empty column list");
		}
		for (String c : columns.split(",")) {
			assertColumn(tool, c);
		}
	}
}
