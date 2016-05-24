package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.docshare.log.Log;
import org.docshare.mvc.TextTool;

/**
 * 使用了延迟加载技术的List。其中的all()方法并非读取所有数据。
 * 数据只有在真正读取时（调用get函数或者被枚举）才会从数据库中读取出来。
 * 
 * @author Administrator
 * 
 */
public class LasyList extends ListAdapter {

	private String sqlfrom; // 主要是表格名称
	// private String sqllimit = "";
	private Integer sqlstart = null;
	private Integer sqllen = null;
	private String sqlorder = null;

	public ArrayList<String> sqlcons = new ArrayList<String>();

	private String join(ArrayList<String> s) {
		if (s.isEmpty())
			return "";

		StringBuffer sb = new StringBuffer();
		sb.append(s.get(0));
		for (int i = 1; i < s.size(); i++) {
			sb.append(" and " + s.get(i));
		}
		return sb.toString();
	}

	private String sqllimit() {
		if (sqlstart != null && sqllen != null) {
			return "limit " + sqlstart + " , " + sqllen;
		} else if (sqllen != null) {
			return "limit " + sqllen;
		}
		return "";
	}

	private String sql() {
		String r = sqlfrom;
		// 如果已经有了limit且sqllimit有值 ,那么去掉limit
		if (sqllen != null && r.contains("limit")) {
			int p = r.indexOf("limit");
			r.substring(p + "limit".length());
			r = r.substring(0, p);
		}

		String joined = join(sqlcons);
		if (r.contains("where") && joined.length() > 0) {
			r += " and " + joined;
		} else if (joined.length() > 0) {
			r += " where " + joined;
		}

		if (sqlorder != null) {
			r += " " + sqlorder;
		}

		if (sqlstart != null) {
			r += " " + sqllimit();
		}

		r = r.toLowerCase();

		return r;
		// return sqlfrom +" "+ join(sqlcons)+" "+ sqllimit;
	}

	private DBTool tool;
	private ResultSet rs = null;

	/**
	 * 根据一个类似 from book 这样的sql片段生成LasyList
	 * @param from 以from开头的sql语句片段，如from book
	 * @param tool 与之关联的工具类 
	 */
	protected LasyList(String from, DBTool tool) {
		from = from.toLowerCase();
		if (from.contains("limit")) {
			String limit_str = TextTool.getAfter(from, "limit")
					.replace(" ", "");
			if (limit_str.contains(",")) {
				String[] sa = limit_str.split(",");
				sqlstart = Integer.parseInt(sa[0]);
				sqllen = Integer.parseInt(sa[1]);
			} else {
				sqllen = Integer.parseInt(limit_str);
				sqlstart = 0;
			}
			from = TextTool.getBefore(from, "limit");
		}

		this.sqlfrom = from;

		this.tool = tool;
	}

	/**
	 * 返回列表的大小，使用select count(*)的方式进行查询获取。并根据limit进行修正
	 */
	@Override
	public int size() {
		Log.d("size() called");

		String s = sql();
		if (s.contains("select")) { // 去除select
			s = "from " + TextTool.getAfter(s, "from");
		}

		if (s.contains("limit")) {
			s = TextTool.getBefore(s, "limit");
		}

		String sql = "select count(*) as ct " + s;
		int sz = tool.helper.getVal(sql, "ct");

		if (sqllen != null) {
			if (sqllen > sz - sqlstart)
				return sz - sqlstart;
			else
				return sqllen;
		} else
			return sz;
	}

	/**判断列表是否为空
	 * 
	 */
	@Override
	public boolean isEmpty() {
		Log.d("isEmpty() called");
		return size() == 0;
	}
	/**
	 * 判断列表中是否存在某个对象，该方法<red>暂未实现</red>。
	 */
	@Override
	public boolean contains(Object o) {
		return false;
	}

	HashMap<Integer, Model> row_maps = new HashMap<Integer, Model>();

	/**
	 * 根据索引获取相应的对象
	 * @param index 索引值，以0开头
	 */
	@Override
	public Model get(int index) {
		Log.d("get called " + index);
		initRS();
		if (row_maps.containsKey(index)) {
			Log.d("Model from cache " + index);
			return row_maps.get(index);
		}

		try {
			int r = rs.getRow() - 1;

			while (r < index) {
				if (!rs.next()) {
					return null;
				}
				row_maps.put(r, tool.db2Table(rs));
				r++;
			}
			while (r > index) {
				if (!rs.previous()) {
					return null;
				}
				row_maps.put(r, tool.db2Table(rs));
				r--;
			}
			return tool.db2Table(rs);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private void initRS() {
		if (rs == null) {
			try {
				String r = sql();
				if (!r.contains("select")) {
					r = "select * " + r;
				}
				rs = tool.helper.getRS(r);

			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	/**
	 * 枚举器。程序可以使用for-each写法来访问这个list。
	 * JSTL可以使用&lt;c:forEach&gt;来访问
	 */
	@Override
	public Iterator<Model> iterator() {
		initRS();
		return new Iterator<Model>() {

			@Override
			public boolean hasNext() {
				if(rs == null)return false;
				try {
					return !rs.isLast();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
				
			}

			@Override
			public Model next() {
				Model ret = null;
				
				try {
					if (rs.isLast()) {
						return null;
					}
					if (rs.next()) {
						ret = tool.db2Table(rs);
					} else {
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				return ret;
			}

			@Override
			public void remove() {
				// DO NOTHING銆�ERE
			}
		};
	}

	/**
	 * 根据分页来截取数据
	 * @param pageno 分页索引，以1开始
	 * @param pageSize 每页大小
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	public LasyList page(int pageno, int pageSize) {
		if (pageno == 0) {
			pageno = 1;
		}
		int start = (pageno - 1) * pageSize;
		limit(start, pageSize);
		return this;
	}

	/**
	 * 使用limit来截取数据
	 * @param start 开始索引，以0开始
	 * @param len 长度
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	public LasyList limit(int start, int len) {
		sqlstart = start;
		sqllen = len;
		// sqllimit = " limit "+start +" , "+len;
		return this;
	}

	/**
	 * 
	 * 使用limit来截取数据
	 * @param len 长度
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	public LasyList limit(int len) {
		return limit(0, len);
	}

	/**
	 * 模糊查询，会使用 sql中的 a like '%b%'的方式来实现
	 * @param column
	 * @param q
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	public LasyList like(String column, String q) {
		String w = String.format("  %s like '$%s$' ", column, q).replace("$",
				"%");
		sqlcons.add(w);

		return this;
	}

	public LasyList eq(String column, String val) {
		String w = String.format("%s = '%s'", column, val);
		sqlcons.add(w);

		return this;
	}

	/**
	 * 大于 ,相当于sql中 column > val 
	 * 会获取column制定的列大于val值的所有项 
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	public LasyList gt(String column, int val) {
		String w = String.format("%s > %d", column, val);
		sqlcons.add(w);

		return this;
	}
	/**
	 * 大于等于 ,相当于sql中 column >= val 
	 * 会获取column制定的列大于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	public LasyList gte(String column, int val) {
		String w = String.format("%s >= %d", column, val);
		sqlcons.add(w);

		return this;
	}

	/**
	 * 小于 ,相当于sql中 column <= val 
	 * 会获取column制定的列小于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	public LasyList lt(String column, int val) {
		String w = String.format("%s < %d", column, val);
		sqlcons.add(w);

		return this;
	}
	/**
	 * 小于等于 ,相当于sql中 column <= val 
	 * 会获取column制定的列小于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	public LasyList lte(String column, int val) {
		String w = String.format("%s <= %d", column, val);
		sqlcons.add(w);

		return this;
	}
	/**
	 * 不等于 ,相当于sql中 column <> val 
	 * 会获取column制定的列不等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	public LasyList ne(String column, int val) {
		String w = String.format("%s <> %d", column, val);
		sqlcons.add(w);

		return this;
	}

	/**
	 * 
	 * @param column
	 *            排序依据的列
	 * @param asc
	 *            当asc为true时，是升序，否则为降序
	 */
	public LasyList orderby(String column, boolean asc) {
		sqlorder = "order by " + column;
		if (asc) {
			sqlorder += " asc ";
		} else {
			sqlorder += " desc ";
		}
		return this;
	}

	/**
	 * 获取列表的第一个元素
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	public Model one() {
		Model model = get(0);
		return model;
	}
	
	@Override
	protected void finalize() throws Throwable {
		try{
			Log.d("LasyList finalized");
			rs.close();
			rs =null;
		}catch(Exception e){}
		super.finalize();
	}
}
