package org.docshare.orm;

/**
 * 每一列的描述
 * @author Administrator
 *
 */
public class ColumnDesc {
	public String name;
	public int type;
	public String typeName;
	public String remark;
	public String pk_table = null; //如果是null,则不是外键
	public String pk_column = null;
	
	public ColumnDesc(String name, int type, String remark) {
		super();
		this.name = name;
		this.type = type;
		this.remark = remark;
	}
	
	public ColumnDesc() {
	}
	
	@Override
	public String toString() {
		
		return "name["+typeName+"]";
	}
}
