package org.docshare.orm;

/**
 * ÿһ�е�����
 * @author Administrator
 *
 */
public class ColumnDesc {
	public String name;
	public int type;
	public String typeName;
	public String remark;
	public ColumnDesc(String name, int type, String remark) {
		super();
		this.name = name;
		this.type = type;
		this.remark = remark;
	}
	public ColumnDesc() {
	}
	
}
