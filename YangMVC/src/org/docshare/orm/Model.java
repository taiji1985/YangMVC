package org.docshare.orm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Model implements Map<String,Object> {
	private String tname;
	private Map<String, Object> columns;
	DBTool joined_tool=null;//��֮��ص�tool��
	public Model(String tname,Map<String,Object> columns){
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Set entrySet() {
		return null;
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
