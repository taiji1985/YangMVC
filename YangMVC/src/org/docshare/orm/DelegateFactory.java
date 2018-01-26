package org.docshare.orm;

import org.docshare.orm.mysql.MySQLDelegate;

public class DelegateFactory {
	static IDBDelegate mysqlDelegate=new MySQLDelegate();
	public static IDBDelegate getIns(String dbtype){
		if(dbtype.equals("mysql")){
			return mysqlDelegate;
		}
		return null;
	}
}
