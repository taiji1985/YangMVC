package org.docshare.orm;

import org.docshare.orm.access.AccessDelegate;
import org.docshare.orm.mysql.MySQLDelegate;
import org.docshare.orm.postgres.PostgresDelegate;

public class DelegateFactory {
	static IDBDelegate mysqlDelegate=new MySQLDelegate();
	static IDBDelegate pgDelegate=new PostgresDelegate();
	static IDBDelegate accessDelegate=new AccessDelegate();
	public static IDBDelegate getIns(String dbtype){
		if(dbtype.equals("mysql")){
			return mysqlDelegate;
		}else if(dbtype.equals("postgres")){
			return pgDelegate;
		}else if(dbtype.equals("access")){
			return accessDelegate;
		}
		return null;
	}
}
