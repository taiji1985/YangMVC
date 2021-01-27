package org.docshare.orm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.util.BeanUtil;
import org.docshare.util.TextTool;
import org.jetbrains.annotations.NotNull;

import com.alibaba.fastjson.JSON;

public class Model implements Map<String,Object> {
	private String tname;
	private Map<String, Object> columns;
	private Map<String,Object> extra; // append data of Model
	DBTool joined_tool=null;// relate tool 
	/**
	 * 用以标志是否是由Create创建（且为入库）， 以便于判断是应进行插入还是删除。
	 * 如是创建出的，则应为插入，否则应为更新。
	 * 当该对象保存如数据库后，该值被置为false
	 */
	public boolean isCreated  =false; 
	protected Model(String tname,Map<String,Object> columns){
		this.tname = tname;
		//this.columns = columns;
		this.columns = new HashMap<String, Object>();
		if(columns != null){
			this.columns.putAll(columns);
		}
	}
	public static DBTool tool(String tname){		
		return DBTool.getIns(tname);
	}
	public String getTableName(){
		return tname;
	}
	
	
/*	protected String getRelateTable(){
		String name = this.getClass().getSimpleName();
		//name = name.toLowerCase();
		return cls2db(name);
	}
	private static String cls2db(String name){
		char[] chs = name.toCharArray();
		StringBuilder sb = new StringBuilder();
		sb.append((""+chs[0]).toLowerCase());
		
		for(int i=1;i<chs.length;i++){
			if(Character.isUpperCase(chs[i])){
				sb.append("_");
				sb.append(Character.toLowerCase(chs[i]));
			}else{
				sb.append(chs[i]);
			}
		}
	
		return sb.toString();
		
	}*/
	
	public String remark(String c){
		String r = joined_tool.getColumnRemark(c);
		return r == null?c:r;
	}
	
	@Deprecated
	public String getRemark(String c){
		return remark(c);
	}
	
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
	/**
	 * 以整数形式返回字段值，如果原本为非整数，则尝试转换（如果无法转换则报错）
	 * @param key 字段的名称
	 * @return 字段你的值（整数）
	 */
	public Integer getInt(Object key){
		Object rObject  = get(key);
		if(rObject instanceof Integer){
			return (Integer)rObject;
		}
		String str = rObject +"";
		return Integer.parseInt(str);
	}
	/**
	 * 以长整数形式返回字段值，如果原本为非整数，则尝试转换（如果无法转换则报错）
	 * @param key 字段的名称
	 * @return 字段你的值（整数）
	 */
	public Long getLong(Object key){
		Object rObject  = get(key);
		if(rObject instanceof Long){
			return (Long)rObject;
		}
		String str = rObject +"";
		return Long.parseLong(str);
	}
	
	/**
	 * 以Double形式返回字段值，如果原本为非整数，则尝试转换（如果无法转换则报错）
	 * @param key 字段的名称
	 * @return 字段你的值（整数）
	 */
	public Double getDouble(Object key){ 
		Object rObject  = get(key);
		if(rObject instanceof Double){
			return (Double)rObject;
		}
		String str = rObject +""; 
		return Double.parseDouble(str);
	}
	
	@Override
	public Object get(Object key) {
		String ks = (String)key;
		
		//检查append,如果append中有这个key，则返回
		if(extra!=null && extra.containsKey(ks)){
			return extra.get(ks);
		}
		
		
		String k =null;
		if(!columns.containsKey(key) && columns.containsKey(key+"_id")){
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
			ColumnDesc desc = joined_tool.getColumnDesc(k);
			if(desc.pk_table == null){ //没有这个主键
				return null;
			}
			
			DBTool tool = Model.tool(desc.pk_table);
			return tool.get(desc.pk_column, columns.get(k));
			
		}
		
		return columns.get(key);
	}

	/**
	 * 删除某些字段 - 该函数不确保用户操作后数据存储操作还有效，但如果仅仅是为了输出，则可放心使用。
	 */
	@Override
	public Object remove(Object key) {
		Object ret = null;
		if(extra!=null) extra.remove(key);
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
		
		columns.clear();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection values() {
		ArrayList<Object> list = new ArrayList<Object>();
		for(String s: keySet()){
			list.add(this.get(s));
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	class ModelEntry implements Entry{

		private Object key;
		private Object val;
		public ModelEntry(Object key,Object val){
			this.key = key;
			this.val = val;
		}
		@Override
		public Object getKey() {
			// TODO Auto-generated method stub
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
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set entrySet() {
		Set s = new HashSet();
		for(String key: this.keySet()){
			Entry en = new ModelEntry(key,this.get(key));
			s.add(en);
		}
		if(extra != null)
		{
			for(String key: this.extra.keySet()){
				Entry en = new ModelEntry(key,extra.get(key));
				s.add(en);			
			}
		}
		
		return s;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(String k:columns.keySet()){
			sb.append(","+k+"="+columns.get(k));
		}
		if(extra !=null){
			sb.append(",extra="+JSON.toJSONString(extra));
		}
		sb.append("]");
		sb.setCharAt(0, '[');
		sb.insert(0, "Model");
		return sb.toString();
	}
	/**
	 * 记录所有被改变的记录，以便于生成更新用的sql语句。
	 */
	HashSet<String> changeList = null;
	public Set<String> changeColumns(){
		return changeList != null?changeList : new HashSet<String>();
	}
	@Override
	@NotNull
	public Model put(String key, Object value) {
		if(columns.containsKey(key)){
			columns.put((String)key, value);
			if(changeList == null){
				changeList  = new HashSet<String>();
			}
			changeList.add(key);
		}else{ //如果没有这个key，则添加入extra之中。
			if(extra == null){
				extra = new HashMap<String,Object>();
			}
			extra.put(key, value);
		}
		return this;
	}
	/**
	 * 内部使用的put
	 * @param key
	 * @param value
	 * @return
	 */
	Model innerPut(String key,Object value){
		if(columns.containsKey(key)){
			columns.put((String)key, value);
			
		}else{ //如果没有这个key，则添加入extra之中。
			if(extra == null){
				extra = new HashMap<String,Object>();
			}
			extra.put(key, value);
		}
		return this;
	}
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		
		
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
				e.printStackTrace();
			}
		}
		return obj;
	}
	public static Model fromObject(String tname,Object obj){
		Model ret = Model.tool(tname).create();
		
		for(String key: ret.keySet()){
			try {
				Object val = BeanUtil.get(obj, key);
				ret.put(key, val);
			} catch (Exception e) {
				Log.d("can't set model of property: " + key );
				Log.d(e);
			}
		}
		ret.isCreated = false;
		return ret;
	}
	/**
	 * 将此Model插入到数据库中
	 * @return 影响的行数
	 */
	public int insert(){
		if(joined_tool == null){
			Log.e("Model didn't connect to a tool ,can't save"+this);
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
			Log.e("Model didn't connect to a tool ,can't save"+this);
			return 0;
		}
		return joined_tool.save(this,false);
	}
	
	public void remove(){

		if(joined_tool == null){
			Log.e("Model didn't connect to a tool ,can't save"+this);
			return;
		}
		joined_tool.del(this);
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
			Log.e("Model ,join_tool is null "+toString());
			return 0;
		}
	}
	
	public void dump(){
		Log.i(this.toString());
	}
	
	public Model fromParam(Controller c){
		c.paramToModel(this);
		return this;
	}
}
