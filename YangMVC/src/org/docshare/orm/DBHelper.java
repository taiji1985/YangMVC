package org.docshare.orm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.SignatureAttribute.NestedClassType;

import org.docshare.mvc.Config;

import com.docshare.log.Log;


public class DBHelper {

	static ThreadLocal<DBHelper> locals=new ThreadLocal<DBHelper>();
	/**
	 * ʹ�øú�����ȡDBHelper���� ��������Ϊͬһ���̹߳���һ��DBHelper
	 * @return DBHelper����
	 */
	public static DBHelper getIns(){
		DBHelper ins = locals.get();
		if(ins == null){
			ins = new DBHelper();
			locals.set(ins);
		}
		return ins;
	}
	
	//��ֹ�û��Լ����ù��캯��
	private DBHelper(){
		
	}
	
	Connection con = null;

	public void conn() {
		try {
			if(con !=null)return;
			
			Class.forName("com.mysql.jdbc.Driver");

			String uri = String.format(
					"jdbc:mysql://%s/%s?characterEncoding=utf-8",
					Config.dbhost, Config.dbname);
			String user = Config.dbusr;
			String password = Config.dbpwd;
			con = DriverManager.getConnection(uri, user, password);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}

	}

	public ResultSet getRS(String sql) throws SQLException {
		Statement s;
		conn();
		s = con.createStatement();
		Log.d("DBHelper exec sql : "+sql);
		return s.executeQuery(sql);
	}

	public int update(String sql) {
		Statement s;
		try {
			conn();
			s = con.createStatement();
			return s.executeUpdate(sql);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	public void close() {
		try {
			if(con!=null){
				con.close();
			}
			con = null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	Map<String, HashMap<String,ColumnDesc>> desc_cached = new HashMap<String, HashMap<String,ColumnDesc>>();
	/**
	 * ���ر�������е��У��Լ��е�ע�ͣ����ע�Ϳ���������ʾ��
	 * @param tb �������
	 * @return  ����->ע�� ��ӳ���
	 */
	public HashMap<String,ColumnDesc> listColumn(String tb) {
		if(desc_cached.containsKey(tb)){
			return desc_cached.get(tb);
		}
		
		conn();
		HashMap<String,ColumnDesc> ret = new HashMap<String,ColumnDesc>();
		try {
			ResultSet rs = con.getMetaData().getColumns(null, "%", tb, "%");
			while (rs.next()) {
				ColumnDesc desc=new ColumnDesc();
				
				desc.name = rs.getString("COLUMN_NAME");
				desc.remark = rs.getString("REMARKS");//������  
				desc.type = rs.getInt("DATA_TYPE");//������  
				desc.typeName = rs.getString("TYPE_NAME");
				if(desc.remark!=null&&desc.remark.length()==0){
					desc.remark = null;
				}
				System.out.println("type "+desc.type +","+desc.name);
				ret.put(desc.name, desc);
			}
			desc_cached.put(tb, ret);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public String keyColumn(String tb) {
		conn();
		ResultSet rs;
		String ret = "ss";
		try {
			rs = con.getMetaData().getIndexInfo(Config.dbname, null, tb, true,
					false);
			while (rs.next()) {
				String name = rs.getString("INDEX_NAME");
				if (name.equals("PRIMARY")) {
					ret = rs.getString("COLUMN_NAME");
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public int getVal(String sql, String column) {
		ResultSet rs;
		int ret = 0;
		try {
			rs = getRS(sql);
			if (rs.next()) {
				ret = rs.getInt(column);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;
	}

	public static void main(String[] args) {

		DBHelper dh = new DBHelper();
		dh.conn();
		// dh.listColumn("book");
		System.out.println(dh.keyColumn("book"));
	}
}
