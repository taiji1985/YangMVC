package org.docshare.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.docshare.log.Log;




/**
 * 语句池
 * @author HP
 *
 */
public class StatementPool {
	
	HashMap<String, PreparedStatement> cache = new HashMap<>();
	public PreparedStatement get(Connection con,String sql) throws SQLException{
		PreparedStatement p ;
		if(cache.containsKey(sql)) {
			Log.v("StatementPool: return cached PreparedStatement" );
			p=cache.get(sql);
			//p.clearParameters();
		}
		else{
			p=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			cache.put(sql, p);
		}
		return p;
	}
	public void clear(){
		cache.clear();
	}

}
