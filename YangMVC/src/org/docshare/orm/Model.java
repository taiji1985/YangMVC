package org.docshare.orm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.docshare.mvc.TextTool;

public class Model implements Map<String,Object> {
	private String tname;
	private Map<String, Object> columns;
	DBTool joined_tool=null;//与之相关的tool类
	protected Model(String tname,Map<String,Object> columns){
		this.tname = tname;
		this.columns = columns;
	}
	public static DBTool tool(String tname){		
		return new DBTool(tname);
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
		StringBuffer sb = new StringBuffer();
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
	
	public String getRemark(String c){
		String r = joined_tool.getColumnRemark(c);
		return r == null?c:r;
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

	@Override
	public Object get(Object key) {
		String ks = (String)key;
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

	@Override
	public Object remove(Object key) {
		return null;
	}

	@Override
	public void clear() {
		
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
			// TODO Auto-generated method stub
			return this.val;
		}

		@Override
		public Object setValue(Object value) {
			// TODO Auto-generated method stub
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
		
		return s;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		for(String k:columns.keySet()){
			stringBuffer.append(","+k+"="+columns.get(k));
		}
		stringBuffer.append("]");
		stringBuffer.setCharAt(0, '[');
		stringBuffer.insert(0, "Model");
		return stringBuffer.toString();
	}
	@Override
	public Object put(String key, Object value) {
		if(columns.containsKey(key)){
			return columns.put((String)key, value);
		}else return null;
	}
	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		
		
	}
	public Object getPrimaryKey() {
		return joined_tool.key;
	}
}
