package org.docshare.yangmvc.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.docshare.yangmvc.mvc.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 语句池（按 SQL 复用 PreparedStatement）。
 *
 * <p>旧实现是普通 HashMap，<b>无上限、永不 close</b>：动态拼 SQL 的场景（dashboard/后台列表）
 * 每个线程的缓存会一直长，而且语句也一直不释放。现在改为：
 * <ul>
 *   <li>LRU，上限 {@link Config#db_statement_cache_size}（默认 128）；</li>
 *   <li>淘汰/清空时 close 语句；</li>
 *   <li>连接重建前由 {@code MySQLDBHelper.conn()} 调 {@link #clear()}，语句随旧连接一起释放。</li>
 * </ul>
 */
public class StatementPool {
	private static final Logger logger = LoggerFactory.getLogger(StatementPool.class);

	private final Map<String, PreparedStatement> cache = new java.util.LinkedHashMap<String, PreparedStatement>(16, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, PreparedStatement> eldest) {
			if (size() > maxSize()) {
				closeQuietly(eldest.getValue());
				return true;
			}
			return false;
		}
	};

	private static int maxSize() {
		return Math.max(1, Config.db_statement_cache_size);
	}

	public PreparedStatement get(Connection con, String sql) throws SQLException {
		PreparedStatement p = cache.get(sql);
		if (p != null) {
			logger.trace("StatementPool: return cached PreparedStatement");
			return p;
		}
		p = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		cache.put(sql, p);
		return p;
	}

	/**
	 * 清空并关闭所有缓存的语句（换连接时调用）。
	 */
	public void clear() {
		for (PreparedStatement p : cache.values()) {
			closeQuietly(p);
		}
		cache.clear();
	}

	public int size() {
		return cache.size();
	}

	private static void closeQuietly(PreparedStatement p) {
		if (p == null) {
			return;
		}
		try {
			p.close();
		} catch (Exception ignored) {
		}
	}
}
