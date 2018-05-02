package org.docshare.orm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.docshare.log.Log;
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
		initRS();
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
		Log.d("size() called");
		if(arrList!=null){
			return arrList.size();
		}
		return (int)delegate.size(cons, tool, tbName);
	}
	
	/**
	 * 判断是否有相关记录
	 * @return
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
	Map<String, ?> column_desc = null;
	public void printColumnDesc(){
		if(column_desc == null){
			column_desc = tool.c_to_remarks;
		}
		for(String k : column_desc.keySet()){
			System.out.println(k+", "+column_desc.get(k));
		}
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
						column_desc = new HashMap<String, Object>();
					}
				}else{
					rs =  delegate.runSQL(cons, tool, tbName);
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

	/**
	 * 使用limit来截取数据
	 * @param start 开始索引，以0开始
	 * @param len 长度
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	@NotNull
	public LasyList limit(int start, int len) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LIMIT, "", start,len));
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
	 * @param column
	 * @param q
	 * @return 过滤后的LasyList对象（还是this当前对象，方便级联使用）
	 */
	@NotNull
	public LasyList like(String column, String q) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LIKE, column, q));
		return this;
	}
	/**
	 * 在多个列中进行like查找，多个列之间是或者关系
	 * @param columnList
	 * @param q
	 * @return
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
	 * 大于 ,相当于sql中 column > val 
	 * 会获取column制定的列大于val值的所有项 
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList gt(String column, int val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_GT, column, val));
		
		return this;
	}
	/**
	 * 大于等于 ,相当于sql中 column >= val 
	 * 会获取column制定的列大于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList gte(String column, int val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_GTE, column, val));
		return this;
	}

	/**
	 * 小于 ,相当于sql中 column <= val 
	 * 会获取column制定的列小于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList lt(String column, int val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LT, column, val));
		return this;
	}
	/**
	 * 小于等于 ,相当于sql中 column <= val 
	 * 会获取column制定的列小于或等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList lte(String column, int val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_LTE, column, val));
		return this;
	}
	/**
	 * 不等于 ,相当于sql中 column <> val 
	 * 会获取column制定的列不等于val值的所有项对象
	 * @param column 列名
	 * @param val    值
	 * @return 当前对象。 返回当前对象的好处就是可以使用级联的写法 如  tool.all().gt(id,12)
	 */
	@NotNull
	public LasyList ne(String column, int val) {
		cons.add(new SQLConstains(SQLConstains.TYPE_NE, column, val));
		return this;
	}

	/**
	 * 
	 * @param column
	 *            排序依据的列
	 * @param asc
	 *            当asc为true时，是升序，否则为降序
	 */
	@NotNull
	public LasyList orderby(String column, boolean asc) {
		cons.add(new SQLConstains(SQLConstains.TYPE_ORDER, column, asc));
		return this;
	}
	
	/***
	 * 除了limit和order意外的任意约束条件。 这个约束条件不会判断是否重复
	 * @param any
	 * @return
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
		StringBuffer sb = new StringBuffer();
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
		List<Model> mList = new ArrayList<Model>();
		try {
			arrList  = mList;
			while(rs!= null && rs.next()){
				Model m = tool.db2Table(rs,column_desc);
				
				mList.add(m);
			}
			if(rs!= null ){
				rs.close();
			}
			if(rs == null){
				debugInfo();
			}
		} catch (SQLException e) {
			Log.e("LasyList.toArrayList Exception " + debugInfo());
			Log.e(e);
		}
		return mList;
	}
	public String debugInfo(){
		StringBuffer sb = new StringBuffer();
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
	 * @param sql
	 * @return LasyList对象
	 */
	public static LasyList fromRawSql(String sql){
		LasyList list = new LasyList(sql);
		return list;
		
	}
	
	
	

}
