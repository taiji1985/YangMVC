package org.docshare.orm;

import java.util.HashMap;

public class ColumnSave{
	public HashMap<String,ColumnDesc> listColumn;
	public String tb;
	public ColumnSave(String tb,HashMap<String,ColumnDesc> listColumn){
		this.tb = tb;
		this.listColumn = listColumn;
	}
	public ColumnSave() {
		super();
	}
	
}