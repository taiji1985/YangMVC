package org.docshare.orm;

public class SQLConstains {
	public static final int TYPE_EQ = 1;
	public static final int TYPE_LIKE = 2;
	public static final int TYPE_GT = 3;
	public static final int TYPE_LT = 4;
	public static final int TYPE_LTE = 5;
	public static final int TYPE_NE = 6;
	public static final int TYPE_GTE = 7;
	public static final int TYPE_ORDER = 50;
	public static final int TYPE_LIMIT = 100;
	public int type ; 	  // 类型 
	public String column; // 列名
	public Object value;  // 值
	public Object value2; //第二个值
	public SQLConstains(int type, String column, Object value,Object value2) {
		super();
		this.type = type;
		this.column = column;
		this.value = value;
		this.value2 = value2;
	}
	public SQLConstains(int type, String column, Object value) {
		super();
		this.type = type;
		this.column = column;
		this.value = value;
	}
}
