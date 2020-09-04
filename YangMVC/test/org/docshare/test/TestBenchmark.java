package org.docshare.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.docshare.log.Log;
import org.docshare.mvc.Config;
import org.docshare.orm.DBHelper;
import org.docshare.orm.DBTool;
import org.docshare.orm.Model;

import com.mysql.jdbc.CommunicationsException;

import junit.framework.TestCase;

public class TestBenchmark extends TestCase{
	
	@Override
	protected void setUp() throws Exception {
		Config.dbhost="localhost";
		Config.dbport = "3308";
		Config.dbpwd="123456";
		Config.dbname = "mvc_demo";
	}

	public void testDBHelper() throws SQLException, InterruptedException {
		
		DBHelper dbHelper = DBHelper.getIns();
		dbHelper.conn();
		Connection c = dbHelper.getConnection();
		//Thread.sleep(5000);
		long start = System.currentTimeMillis();			
		try {
			//PreparedStatement ps = c.prepareStatement("select * from c_course where id = ?" );
			
			for(int i=0;i<40000;i++){  
				//ps.setInt(1, 1);
//				if(i%4000 == 0)System.out.println();
//				if(i%400 == 0)System.out.print(".");
				ResultSet rs = dbHelper.getPrepareRS("select * from book where id = ?",1);
			}
		} catch (CommunicationsException e) {
			Log.e("\nDB Communications Fail ");
			e.printStackTrace();
		}	
		long end = System.currentTimeMillis();
		
		Log.i("finish testDBHelper"+(end-start));
		dbHelper.close();

	}
	
	public void testModel() throws SQLException, InterruptedException {
		
		DBHelper dbHelper = DBHelper.getIns();
		dbHelper.conn();
		Connection c = dbHelper.getConnection();
		//Thread.sleep(5000);
		long start = System.currentTimeMillis();			
		//PreparedStatement ps = c.prepareStatement("select * from c_course where id = ?" );
		DBTool tool = Model.tool("book");
		for(int i=0;i<40000;i++){  
			//ps.setInt(1, 1);
//			if(i%8000 == 0)System.out.println();
//			if(i%400 == 0)System.out.print(".");
			tool.all().eq("id", 1).toArrayList();
		}	
		long end = System.currentTimeMillis();
		
		Log.i("finish testModel"+(end-start));
		dbHelper.close();

	}
	public void testModel2() throws SQLException, InterruptedException {
		
		DBHelper dbHelper = DBHelper.getIns();
		dbHelper.conn();
		Connection c = dbHelper.getConnection();
		//Thread.sleep(5000);
		long start = System.currentTimeMillis();			
		//PreparedStatement ps = c.prepareStatement("select * from c_course where id = ?" );
		DBTool tool = Model.tool("book");
		for(int i=0;i<40000;i++){  
			//ps.setInt(1, 1);
//			if(i%8000 == 0)System.out.println();
//			if(i%400 == 0)System.out.print(".");
			tool.get(1);
		}	
		long end = System.currentTimeMillis();
		
		Log.i("finish testModel2"+(end-start));
		dbHelper.close();

	}
	public void testPS() throws SQLException, InterruptedException {
		
		DBHelper dbHelper = DBHelper.getIns();
		dbHelper.conn();
		Connection c = dbHelper.getConnection();
		long start = System.currentTimeMillis();			
		try {
			PreparedStatement ps = c.prepareStatement("select * from book where id = ?" );
			
			for(int i=0;i<40000;i++){  
				ps.setInt(1, 1);
//				if(i%4000 == 0)System.out.println();
//				if(i%400 == 0)System.out.print(".");
				ResultSet resultSet= ps.executeQuery();
			}
		} catch (CommunicationsException e) {
			Log.e("DB Communications Fail ");
			e.printStackTrace();
		}	
		long end = System.currentTimeMillis();
		
		Log.i("finish testPS"+(end-start));
		dbHelper.close();
	}
	
	public void testS() throws SQLException, InterruptedException {
		
		DBHelper dbHelper = DBHelper.getIns();
		dbHelper.conn();
		Connection c = dbHelper.getConnection();
		long start = System.currentTimeMillis();			
		try {
			Statement ps = c.createStatement();
			
			for(int i=0;i<40000;i++){  
//				if(i%4000 == 0)System.out.println();
//				if(i%400 == 0)System.out.print(".");
				ResultSet resultSet= ps.executeQuery("select * from book where id = "+1);
			}
		} catch (CommunicationsException e) {
			Log.e("DB Communications Fail ");
			e.printStackTrace();
		}	
		long end = System.currentTimeMillis();
		
		Log.i("finish testS "+(end-start));
		dbHelper.close();
	}
}
