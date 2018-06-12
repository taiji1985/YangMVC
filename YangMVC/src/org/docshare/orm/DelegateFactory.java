package org.docshare.orm;

import org.docshare.orm.mysql.MySQLDelegate;
import org.docshare.orm.postgres.PostgresDBHelper;
import org.docshare.orm.postgres.PostgresDelegate;

public class DelegateFactory {
	static IDBDelegate mysqlDelegate=new MySQLDelegate();
	static IDBDelegate pgDelegate=new PostgresDelegate();
	public static IDBDelegate getIns(String dbtype){
		if(dbtype.equals("mysql")){
			return mysqlDelegate;
		}else if(dbtype.equals("postgres")){
			return pgDelegate;
		}
		return null;
	}
}
