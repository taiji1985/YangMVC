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
	public String tb;
	
	public ColumnDesc(String name, int type, String remark) {
		this(name,type,remark,null);
	}
	
	public ColumnDesc() {
	}
	
	public ColumnDesc(String name2, int columnType, String columnLabel,
			String tb) {
		super();
		this.name = name2;
		this.type = columnType;
		this.remark = columnLabel;
		this.tb= tb;
	}

	@Override
	public String toString() {
		
		return "name["+typeName+"]";
	}
}
