package org.docshare.test;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.docshare.mvc.Config;
import org.docshare.orm.DBTool;
import org.docshare.orm.DelegateFactory;
import org.docshare.orm.IDBDelegate;
import org.docshare.orm.SQLConstains;
import org.docshare.util.FileTool;

import junit.framework.TestCase;

public class TestMySQLDelegate extends TestCase{
	private IDBDelegate delegate;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Config.dbname="mvc_demo";
		this.delegate = DelegateFactory.getIns("mysql");
	}
	public void testBuildSQL(){
		List<SQLConstains> cons=new ArrayList<SQLConstains>();
		cons.add(new SQLConstains(SQLConstains.TYPE_EQ,	 "name", "本草纲目"));
		cons.add(new SQLConstains(SQLConstains.TYPE_LT,	 "id", 12));
		cons.add(new SQLConstains(SQLConstains.TYPE_GT,	 "id", 5));
		cons.add(new SQLConstains(SQLConstains.TYPE_LTE, "catalog_id", 12));
		cons.add(new SQLConstains(SQLConstains.TYPE_GTE, "catalog_id", 1));
		
		DBTool tool = DBTool.getIns("book");
		//String s = delegate.buildSQL(cons, tool,null);
		//System.out.println(s);
		ResultSet rs = delegate.runSQL(cons, null, null, tool, "book", "*");
		FileTool.safelyClose(rs);
	}
}
