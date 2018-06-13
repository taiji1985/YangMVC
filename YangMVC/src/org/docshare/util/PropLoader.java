package org.docshare.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;

import org.docshare.log.Log;
import org.docshare.mvc.Config;

public class PropLoader {
	public static void setField(Field f,Object obj,String var){
		try {
			Log.d("set "+f.getName()+" to "+var);
			Class<?> type = f.getType();
			if(type == int.class){
				f.set(null, Integer.parseInt(var));
			}else if(type == float.class){
				f.set(null, Float.parseFloat(var));
			}else if(type == double.class){
				f.set(null, Double.parseDouble(var));
			}else if(type == boolean.class){
				f.set(null, Boolean.parseBoolean(var));
			}else if(type == String.class){
				f.set(null, var);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	public static void loadToStatic(String fileName, Class<?> clazz) {
		Properties prop = loadProp(fileName);
		Field[] fields = clazz.getFields();
		for(Field f:fields){
			String name = f.getName();
			Object obj = prop.get(name);
			if(obj == null)continue;
			String var= obj.toString();
			setField(f, null, var);
		}
	}

	static final String PROP_FILE = "/last.properties";

	public static void main(String[] args) {
		loadToStatic(PROP_FILE,Config.class);
		Log.i(Config.str());
	}

	private static Properties loadProp(String fileName) {
		try {
			Properties pro;

			URL purl = PropLoader.class.getResource(PROP_FILE);
			Log.d("read prop from " + purl);
			Log.d("class loader name "
					+ PropLoader.class.getClassLoader().toString());
			if (purl == null) {
				Log.e("Config file NOT found : "+fileName);
				// return;
			} else {
				Log.i("Config file found ! ");
			}

			pro = new Properties();
			//
			InputStream in = null;// new FileInputStream(new
									// File(purl.getPath()));
			in = PropLoader.class.getResourceAsStream(PROP_FILE);
			if (in != null) {
				pro.load(in);
				Log.i(fileName+" loaded ");
				in.close();
			} else {

				Log.i(fileName+" NOT load ");
			}
			return pro;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}
