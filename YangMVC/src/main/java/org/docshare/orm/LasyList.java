package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.docshare.log.Log;
import org.docshare.util.FileTool;
import org.jetbrains.annotations.NotNull;

import com.alibaba.fastjson.JSON;

/**
 * 使用了延迟加载技术的List。其中的all()方法并非读取所有数据。
 * 数据只有在真正读取时（调用get函数或者被枚举）才会从数据库中读取出来。
 * 
 * @author Administrator
 * 
 */
public class LasyList extends ListAdapter {

	static interface Each{
		boolean one(Model m);
	}

	private ArrayList<SQLConstains> cons = new ArrayList<SQLConstains>(); //约束列表
	private DBTool tool;
	private ResultSet rs = null;
	private IDBDelegate delegate;
	private String tbName;   //表格名称
	public DBTool getTool(){
		return tool;
	}
	private String rawSql = null;
	private LasyList(String rawSql){
		this.rawSql = rawSql;
		tool = Model.tool("rawsql");
		delegate = tool.getDelegate();
		//initRS();
		toArrayList();
	}

	public LasyList(DBTool tool,String tbName){
		this.tbName =tbName;

		this.tool = tool;
		delegate = tool.getDelegate();
	}
	

	/**
	 * 返回列表的大小，使用select count(*)的方式进行查询获取。并根据limit进行修正
	 */
	@Override
	public int size() {
		Log.v("size() called");
		if(arrList!=null){
			return arrList.size();
		}
		return (int)delegate.size(cons, tool, tbName);
	}
	
	/**
	 * 判断是否有相关记录
	 * @return 存在返回true，否则为false
	 */
	public boolean exist(){
		if(arrList!=null){
			return arrList.size() > 0;
		}
		return delegate.size(cons, tool, tbName) > 0;
		
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
	 * 判断列表中是否存在某个对象。
	 */
	@Override
	public boolean contains(Object o) {
		toArrayList();
		if(arrList==null)return false;
		return arrList.contains(o);
	}

	HashMap<Integer, Model> row_maps = new HashMap<Integer, Model>();

	/**
	 * 根据索引获取相应的对象
	 * @param index 索引值，以0开头
	 * @return 返回列表的第index个元素，如果index越界 ，返回null
	 */
	@Override
	public Model get(int index) {
		toArrayList();
		try{
			if(arrList == null || index >= arrList.size() || index < 0){
				return null;
			}
			return arrList.get(index);
		}catch(IndexOutOfBoundsException e){
			Log.d(e);
			return null;
		}
	}
	Map<String, ColumnDesc> column_desc = null;
	public void printColumnDesc(){
		if(column_desc == null){
			column_desc = tool.c_to_remarks;
		}
		for(String k : column_desc.keySet()){
			System.out.println(k+", "+column_desc.get(k));
		}
	}
	private String column_filter="*"; // 控制输出哪些项
	/**
	 * 控制输出哪些项目，举例：  id,name,age    每个项目用逗号分隔。
	 * @param filter 返回的列
	 * @return 查询结果
	 */
	public LasyList columnFilter(String filter){
		this.column_filter = filter;
		return this;
	}
	private void initRS() {
		if (rs == null) {
			try {
				if(tbName == null && rawSql!= null){
					rs  = delegate.runSQL(rawSql);
					if(rs !=null && (column_desc==null || column_desc.size() == 0) ){
						column_desc = delegate.columnOfRs(rawSql,rs);
					}
					if(rs == null &&column_desc == null){ //如果查询失败，报个错。
						column_desc = new HashMap<String, ColumnDesc>(); 
					}
				}else{
					rs =  delegate.runSQL(cons,order_constrain,limit_constrain, tool, tbName,column_filter);
				}
			} catch (SQLException e) {
				Log.e("LasyList.initRS ERROR: "+debugInfo());
				Log.e(e);
			}
			
		}
	}
	List<Model> arrList=null; //枚举时直接放入此List中
	int iterIndex = 0;//枚举所用的索引
	/**
	 * 枚举器。程序可以使用for-each写法来访问这个list。
	 * JSTL可以使用&lt;c:forEach&gt;来访问
	 */
	@Override
	public Iterator<Model> iterator() {
		
		arrList = toArrayList();
		iterIndex = 0;
		return new Iterator<Model>() {
			@Override
			public boolean hasNext() {
				if(arrList == null){
					return false;
				}
				return iterIndex < arrList.size();
			}

			@Override
			public Model next() {
				if(arrList == null || iterIndex>= arrList.size() ){
					return null;
				}else{
					return arrList.get(iterIndex++);
				}
			}

			@Override
			public void remove() {
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
	private int _start = 0;
	private SQLConstains limit_constrain=null;
	/**
	 * 使用limit来截取数据
	 * @param start 开始索引，以0开始
	 * @param len 长度
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	@NotNull
	public LasyList limit(int start, int len) {
		_start = start;
		//cons.add(new SQLConstains(SQLConstains.TYPE_LIMIT, "", start,len));
		limit_constrain = new SQLConstains(SQLConstains.TYPE_LIMIT, "", start,len);
		return this;
	}

	/**
	 * 
	 * 使用limit来截取数据
	 * @param len 长度
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	@NotNull
	public LasyList limit(int len) {
		return limit(0, len);
	}

	/**
	 * 模糊查询，会使用 sql中的 a like '%b%'的方式来实现
	 * @param column 列名
	 * @param q 关键字
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	@NotNull
	public LasyList like(String column, String q) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LIKE, column, q));
		return this;
	}
	/**
	 * 在多个列中进行like查找，多个列之间是或者关系
	 * @param columnList 多个列的列名
	 * @param q 关键词
	 * @return 查询结果
	 */
	@NotNull
	public LasyList mlike(String columnList,String q){
		cons.add(new SQLConstains(SQLConstains.TYPE_MLIKE, columnList, q));
		return this;
	}

	@NotNull
	public LasyList eq(String column, Object val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_EQ, column, val));
		
		return this;
	}

	/**
	 * 大于 ,相当于sql中 column &gt; val 
	 * 会获取column制定的列大于val值的所有项 
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList gt(String column, Object val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_GT, column, val));
		
		return this;
	}
	/**
	 * 大于等于 ,相当于sql中 column &gt;= val 
	 * 会获取column制定的列大于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList gte(String column, Object val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_GTE, column, val));
		return this;
	}

	/**
	 * 小于 ,相当于sql中 column &lt;= val 
	 * 会获取column制定的列小于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList lt(String column, Object val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LT, column, val));
		return this;
	}
	/**
	 * 小于等于 ,相当于sql中 column &lt;= val 
	 * 会获取column制定的列小于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList lte(String column, Object val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LTE, column, val));
		return this;
	}
	/**
	 * 不等于 ,相当于sql中 column &lt;&gt; val 
	 * 会获取column制定的列不等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList ne(String column, Object val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_NE, column, val));
		return this;
	}
	
	/**
	 * 添加空约束。。 select * from book where author is null;/
	 * @param column 为空的列
	 * @return 查询结果
	 */
	public LasyList isNull(String column){
		cons.add(new SQLConstains(SQLConstains.TYPE_ISNULL, column, null));
		return this;
	}

	private SQLConstains order_constrain=null;
	/**
	 * 设置排序规则
	 * @param column
	 *            排序依据的列
	 * @param asc
	 *            当asc为true时，是升序，否则为降序
	 * @return 查询结果
	 */
	@NotNull
	public LasyList orderby(String column, boolean asc) {
		//cons.add(new SQLConstains(SQLConstains.TYPE_ORDER, column, asc));
		order_constrain = new SQLConstains(SQLConstains.TYPE_ORDER, column, asc);
		return this;
	}
	
	/***
	 * 除了limit和order意外的任意约束条件。 这个约束条件不会判断是否重复
	 * @param any 任何sql条件
	 * @return 查询结果
	 */
	@NotNull
	public LasyList custom(String any){
		cons.add(new SQLConstains(SQLConstains.TYPE_CUSTOM, any,null));
		return this;
	}

	/**
	 * 获取列表的第一个元素
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	public Model one() {
		limit(_start, 1);//通过加limit减少数据库的查询的量。
		Model model = get(0);
		closeRS();
		return model;
	}
	private void closeRS(){
		try{
			Log.v("LasyList finalized");
			if(rs !=null){
				rs.close();
				rs =null;
			}
		}catch(Exception e){}
	}
	
	@Override
	protected void finalize() throws Throwable {
		closeRS();
		super.finalize();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Model m : this){
			sb.append(m);
		}
		sb.append("]");
		return sb.toString();
	}
	
	public LasyList byExample(Model m){
		for(String k : m.keySet()){
			Object v = m.get(k);
			if(v == null) continue;
			
			//String cc = ArrayTool.valueWrapper(k, v, tool.getColumnTypeName(k));
			//sqlcons.add(cc);
			cons.add(new SQLConstains(SQLConstains.TYPE_EQ, k, v));
		}
		return this;
	}
	@Override
	public List<Model> toArrayList(){
		if(arrList != null){
			return arrList;
		}
		initRS();
		List<Model> mList = new MyArrayList();
		try {
			arrList  = mList;
			Set<String> cs = null; // 只显示这个集合中的列，其他列不显示。
			if(column_filter!=null && ! "*".equals(column_filter)){
				String[] ca = column_filter.split(",");
				cs = new HashSet<>();
				for(String cc : ca){
					cs.add(cc);
				}
			}
			
			while(rs!= null && rs.next()){
				Model m = tool.db2Table(rs,column_desc,cs);
				
				mList.add(m);
			}
			if(rs == null){
				debugInfo();
			}
		} catch (SQLException e) {
			Log.e("LasyList.toArrayList Exception " + debugInfo());
			Log.e(e);
		}finally{
			FileTool.safelyClose(rs);
		}
		return mList;
	}
	public String debugInfo(){
		StringBuilder sb = new StringBuilder();
		sb.append("LasyList[");
		sb.append("\n   SQLConstains="+JSON.toJSONString(cons));
		sb.append("\n	table name = "+tbName);
		sb.append("\n   rawSQL="+rawSql);
		sb.append("]");
		
		return sb.toString();
	}
	/**
	 * 将结果转换为对象数组
	 * 
	 * @param clazz 要转化对象的类
	 * @param <T> 类的类型
	 * @return 对象数组
	 */
	public <T> List<T> toArrayList(Class<T> clazz){
		List<Model> models = toArrayList();
		List<T> ret = new ArrayList<T>();
		try {
			for(Model m : models){
				ret.add(m.toObject(clazz.newInstance()));
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return ret;
	}


	/**
	 * 直接根据sql语句获取列表
	 * @param sql sql语句
	 * @return LasyList对象
	 */
	public static LasyList fromRawSql(String sql){
		LasyList list = new LasyList(sql);
		return list;
		
	}
	
	
	public LasyList each(Each fun){
		for(Model m : this){
			fun.one(m);
		}
		return this;
	}
	public List<Model> filter(Each fun){
		ArrayList<Model> ret= new ArrayList<Model>();
		for(Model m : this){
			if(fun.one(m)) ret.add(m);
		}
		return ret;
	}
	static class MyArrayList extends ArrayList<Model>{
		private static final long serialVersionUID = 7008979830534539859L;
		public MyArrayList each(Each fun){
			for(Model m : this){
				fun.one(m);
			}
			return this;
		}
		public MyArrayList filter(Each fun){
			MyArrayList ret= new MyArrayList();
			for(Model m : this){
				if(fun.one(m)) ret.add(m);
			}
			return ret;
		}
	}

	

}
