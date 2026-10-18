package org.docshare.yangmvc.orm;

import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.util.BeanUtil;
import org.docshare.yangmvc.util.TextTool;
import org.docshare.yangmvc.util.TimeHelper;
import org.jetbrains.annotations.NotNull;

import com.alibaba.fastjson.JSON;

public class Model implements Map<String,Object> {
	private static final Logger logger = LoggerFactory.getLogger(Model.class);

	private final String tableName;
	public boolean isNull = false;
	private final Map<String, Object> columns;
	private Map<String,Object> extra; // append data of Model
	DBTool joined_tool=null;// relate tool 
	/**
	 * 用以标志是否是由Create创建（且为入库）， 以便于判断是应进行插入还是删除。
	 * 如是创建出的，则应为插入，否则应为更新。
	 * 当该对象保存如数据库后，该值被置为false
	 */
	public boolean isCreated  =false; 
	protected Model(String tableName,Map<String,Object> columns){
		this.tableName = tableName;
		//this.columns = columns;
		this.columns = new HashMap<>();
		if(columns != null){
			this.columns.putAll(columns);
		}
	}
	public static DBTool tool(String tableName){
		return DBTool.getIns(tableName);
	}
	public String getTableName(){
		return tableName;
	}

	
	public String remark(String c){
		String r = joined_tool.getColumnRemark(c);
		return r == null?c:r;
	}

	
	@NotNull
	@Override
	public Set<String> keySet() {
		return columns.keySet();
	}

	@Override
	public int size() {
		if(columns == null)return 0;
		return columns.size();
	}
	
	@Override
	public boolean isEmpty(){
		return size()==0;
	}


	@Override
	public boolean containsKey(Object key) {
		return columns.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		for (Object v : columns.values()) {
			if (java.util.Objects.equals(v, value)) {
				return true;
			}
		}
		if (extra != null) {
			for (Object v : extra.values()) {
				if (java.util.Objects.equals(v, value)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * 以字符串形式获取字段值
	 * @param key 字段的名称
	 * @return 字段的值
	 */
	public String getStr(Object key){
		Object rObject  = get(key);
		return rObject == null?null:rObject.toString();
	}

	public String getString(Object key){
		return getStr(key);
	}
	/**
	 * 以整数形式返回字段值，如果原本为非整数，则尝试转换（如果无法转换则报错）
	 * @param key 字段的名称
	 * @return 字段你的值（整数）
	 */
	public Integer getInt(Object key){
		Object rObject  = get(key);
		if(rObject == null){
			return null;
		}
		if(rObject instanceof Integer){
			return (Integer)rObject;
		}
		if(rObject instanceof Number){
			// DOUBLE/DECIMAL/BIGINT 都以 Number 返回：小数截断，但超出 int 范围要报错
			long l = ((Number)rObject).longValue();
			if(l > Integer.MAX_VALUE || l < Integer.MIN_VALUE){
				throw new ArithmeticException("getInt(" + key + "): value out of int range: " + rObject);
			}
			return (int)l;
		}
		BigDecimal bd = toBigDecimal(key, rObject);
		if(bd.compareTo(INT_MAX) > 0 || bd.compareTo(INT_MIN) < 0){
			throw new ArithmeticException("getInt(" + key + "): value out of int range: " + rObject);
		}
		return bd.intValue(); // 95.9 -> 95（截断，与 Java 一致）
	}
	/**
	 * 以长整数形式返回字段值，如果原本为非整数，则尝试转换（如果无法转换则报错）
	 * @param key 字段的名称
	 * @return 字段你的值（整数）
	 */
	public Long getLong(Object key){
		Object rObject  = get(key);
		if(rObject == null){
			return null;
		}
		if(rObject instanceof Long){
			return (Long)rObject;
		}
		if(rObject instanceof Number){
			return ((Number)rObject).longValue();
		}
		return toBigDecimal(key, rObject).longValue();
	}
	
	/**
	 * 以Double形式返回字段值，如果原本为非整数，则尝试转换（如果无法转换则报错）
	 * @param key 字段的名称
	 * @return 字段你的值（整数）
	 */
	public Double getDouble(Object key){
		Object rObject  = get(key);
		if(rObject == null){
			return null;
		}
		if(rObject instanceof Double){
			return (Double) rObject;
		}
		if(rObject instanceof Number){
			return ((Number)rObject).doubleValue();
		}
		return Double.parseDouble(rObject.toString().trim());
	}

	private static final BigDecimal INT_MAX = BigDecimal.valueOf(Integer.MAX_VALUE);
	private static final BigDecimal INT_MIN = BigDecimal.valueOf(Integer.MIN_VALUE);

	private static BigDecimal toBigDecimal(Object key, Object value) {
		try {
			return new BigDecimal(value.toString().trim());
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Model value of '" + key + "' is not a number: " + value);
		}
	}
	
	@Override
	public Object get(Object key) {
		String ks = (String)key;
		
		//检查append,如果append中有这个key，则返回
		if(extra!=null && extra.containsKey(ks)){
			return extra.get(ks);
		}

		String k =null;
		if(columns.containsKey(key)){
			return columns.get(key);
		}else if(!columns.containsKey(key) && columns.containsKey(key+"_id")){
			k=key+"_id";
		}
		//如果原本的字段就以__obj开头，则不认为其为主键
		if(ks.endsWith("__obj") && ! columns.containsKey(ks)){
			k = TextTool.getBefore(ks, "__obj");
			if(! columns.containsKey(k)){
				return null; //想把这个作为外键但它不存在
			}
		}
		if(k !=null){ //try to visit foreign key
			if(!Config.model_fk_lookup){
				return null;
			}
			if(joined_tool == null){
				logger.debug("Model has no joined tool, skip foreign key lookup: {}", k);
				return null;
			}
			ColumnDesc desc = joined_tool.getColumnDesc(k);
			if(desc == null){ //元数据里没有这列（rawSql/过滤列）
				logger.debug("no column desc for foreign key: {}", k);
				return null;
			}
			if(desc.pk_table == null){ //没有这个主键
				return null;
			}
			
			DBTool tool = Model.tool(desc.pk_table);
			return tool.get(desc.pk_column, columns.get(k));
			
		}

		return null;
	}

	public Date getDate(Object key){
		return TimeHelper.toDate(get(key));
	}

	/**
	 * 删除某些字段 - 该函数不确保用户操作后数据存储操作还有效，但如果仅仅是为了输出，则可放心使用。
	 */
	@Override
	public Object remove(Object key) {
		Object ret = null;
		if(extra!=null) ret = extra.remove(key);
		if(ret == null){
			ret = columns.remove(key);
		}
		return ret;
	}

	@Override
	public void clear() {
		if(extra!= null){
			extra.clear();
		}
		if(nullList != null){
			nullList.clear();
		}
		
		columns.clear();
	}
	
	@NotNull
	@Override
	public Collection<Object> values() {
		ArrayList<Object> list = new ArrayList<>();
		for (String s : keySet()) {
			list.add(this.get(s));
		}
		return list;
	}

    static class ModelEntry implements Entry<String, Object> {

		private final String key;
		private Object val;

		public ModelEntry(String key, Object val) {
			this.key = key;
			this.val = val;
		}
		@Override
		public String getKey() {
			return this.key;
		}

		@Override
		public Object getValue() {
			return this.val;
		}

		@Override
		public Object setValue(Object value) {
			this.val = value;
			return value;
		}

		@Override
		public String toString() {
			return "ModelEntry [key=" + key + ", val=" + val + "]";
		}
	}
	
	@NotNull
    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return entrySetView;
    }

	/**
	 * entrySet 的"活视图"：不再每次序列化都 new 一个 HashSet + N 个 Entry。
	 * fastjson 序列化 Map 时正是走 entrySet()，列表接口能少掉大量小对象。
	 *
	 * <p>迭代器在创建时对 key 做一次快照（只复制 String 引用，不复制 Entry），
	 * 所以<b>边迭代边改 Model 的值是安全的</b>（不会 ConcurrentModificationException），
	 * 取到的值是最新的；迭代过程中新增的 extra 字段不会出现在本次迭代里。
	 */
	private final Set<Map.Entry<String, Object>> entrySetView = new EntrySetView();

	private final class EntrySetView extends AbstractSet<Map.Entry<String, Object>> {
		@Override
		public Iterator<Map.Entry<String, Object>> iterator() {
			int n = columns.size() + (extra == null ? 0 : extra.size());
			ArrayList<String> keys = new ArrayList<String>(n);
			keys.addAll(columns.keySet());
			if (extra != null) {
				keys.addAll(extra.keySet());
			}
			return new EntryIterator(keys.iterator());
		}

		@Override
		public int size() {
			return columns.size() + (extra == null ? 0 : extra.size());
		}
	}

	private final class EntryIterator implements Iterator<Map.Entry<String, Object>> {
		private final Iterator<String> keys;

		EntryIterator(Iterator<String> keys) {
			this.keys = keys;
		}

		@Override
		public boolean hasNext() {
			return keys.hasNext();
		}

		@Override
		public Map.Entry<String, Object> next() {
			String k = keys.next();
			Object v;
			if (columns.containsKey(k)) {
				v = columns.get(k);
			} else {
				v = extra == null ? null : extra.get(k);
			}
			return new ModelEntry(k, v);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Model[");
		for(String k : columns.keySet()){
			sb.append(",").append(k).append("=").append(columns.get(k));
		}
		if(extra != null){
			sb.append(",extra=").append(JSON.toJSONString(extra));
		}
		sb.append("]");
		return sb.toString();
	}
	/**
	 * 记录所有被改变的记录，以便于生成更新用的sql语句。
	 */
	HashSet<String> changeList = null;
	/** 被 putNull() 显式标记为"写 NULL"的列。 */
	HashSet<String> nullList = null;
	public Set<String> changeColumns(){
		return changeList != null?changeList : new HashSet<>();
	}
	public void setChangeList(Set<String> changeList){
		this.changeList = new HashSet<>();
		this.changeList.addAll(changeList);
	}

	/**
	 * 判断某个名字是不是这张表的真实字段（而不是 put() 放进 extra 的附加数据）。
	 */
	public boolean hasColumn(String key){
		return key != null && columns.containsKey(key);
	}

	/**
	 * 显式把某一列更新为 NULL（默认行为下 save() 会跳过 null 值，无法清空列）。
	 * 需要同时开启 Config.db_update_null=true 才会真正写 NULL。
	 * @param key 列名
	 * @return 当前对象，便于级联
	 */
	public Model putNull(String key){
		if(key == null) return this;
		if(!columns.containsKey(key)){
			logger.warn("putNull: '{}' is not a column of table {}, it will be ignored by save()", key, tableName);
			if(extra == null){
				extra = new HashMap<>();
			}
			extra.put(key, null);
			return this;
		}
		if(changeList == null){
			changeList = new HashSet<>();
		}
		changeList.add(key);
		if(nullList == null){
			nullList = new HashSet<>();
		}
		nullList.add(key);
		columns.put(key, null);
		return this;
	}

	/**
	 * 该列是否被 putNull() 标记为"写 NULL"。
	 */
	public boolean isNullColumn(String key){
		return key != null && nullList != null && nullList.contains(key);
	}

	@Override
	@NotNull
	public Model put(String key, Object value) {
		if(columns.containsKey(key)){
			columns.put(key, value);
			if(changeList == null){
				changeList  = new HashSet<>();
			}
			changeList.add(key);
			if(value != null && nullList != null){
				// 又给了真实值，取消之前的"置空"意图
				nullList.remove(key);
			}
		}else{ //如果没有这个key，则添加入extra之中。
			if(extra == null){
				extra = new HashMap<>();
			}
			extra.put(key, value);
		}
		return this;
	}
	/**
	 * 内部使用的put
	 * @param key 字段名
	 * @param value 值
	 */
	void innerPut(String key,Object value){
		if(columns.containsKey(key)){
			columns.put(key, value);
			
		}else{ //如果没有这个key，则添加入extra之中。
			if(extra == null){
				extra = new HashMap<>();
			}
			extra.put(key, value);
		}
	}
	@Override
	public void putAll(@NotNull Map<? extends String, ?> m) {
		for (Entry<? extends String, ?> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	public String key() {
		return joined_tool.key;
	}
	/**
	 * 将数据拷贝到对象中
	 * @param obj 要保存数据的对象
	 * @return 传入的obj这个对象。
	 * @param <T> 任意类型
	 */
	public <T> T toObject(T obj){
		for(String key: keySet()){
			try {
				BeanUtil.set(obj, key, get(key));
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		return obj;
	}
	public static Model fromObject(String tableName,Object obj){
		Model ret = Model.tool(tableName).create();
		
		for(String key: ret.keySet()){
			try {
				Object val = BeanUtil.get(obj, key);
				ret.put(key, val);
			} catch (Exception e) {
				logger.debug("can't set model of property: " + key );
				logger.debug("{}", e);
			}
		}
		ret.isCreated = false;
		return ret;
	}
	
	public <T> T toObject(Class<T> clz){
		try {
			T obj = clz.newInstance();
			for(String key: keySet()){
				BeanUtil.set(obj, key,  this.get(key));
			}
			if(extra!= null){
				for(String key : extra.keySet()){
					BeanUtil.set(obj, key,  get(key));					
				}
			}
			
			return obj;	
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("", e);
		}
		return null;
	}
	
	/**
	 * 将此Model插入到数据库中
	 * @return 影响的行数
	 */
	public int insert(){
		if(joined_tool == null){
			logger.error("Model didn't connect to a tool ,can't save"+this);
			return 0;
		}
		return joined_tool.insert(this);
	}
	/**
	 * 将此Model更新到数据库中
	 * @return 影响的行数
	 */
	public int update(){
		if(joined_tool == null){
			logger.error("Model didn't connect to a tool ,can't save"+this);
			return 0;
		}
		return joined_tool.save(this,false);
	}
	
	public int remove(){

		if(joined_tool == null){
			logger.error("Model didn't connect to a tool ,can't save"+this);
			return 0;
		}
		return joined_tool.del(this);
	}
	/**
	 * 获取我的DBTool
	 * @return DBTool对象
	 */
	public DBTool myTool(){
		return joined_tool;
	}
	/**
	 * 保存当前对象,v2.3 以上加入的功能
	 * @return 影响的数据库行数
	 */
	public int save(){
		if(joined_tool!=null){
			return joined_tool.save(this);
		}else{
			logger.error("Model ,join_tool is null "+ this);
			return 0;
		}
	}
	public Model saveAndRet(){
		save();
		return this;
	}

	public void dump(){
		logger.info(this.toString());
	}

	public Model fromParam(Controller c){
		c.paramToModel(this);
		return this;
	}
}
