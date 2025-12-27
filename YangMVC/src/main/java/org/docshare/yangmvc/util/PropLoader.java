package org.docshare.yangmvc.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.Config;

public class PropLoader {
	private static final Logger logger = LoggerFactory.getLogger(PropLoader.class);

	public static void setField(Field f,Object obj,String var){
		try {
			logger.debug("set "+f.getName()+" to "+var);
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
		loadToStatic(PROP_FILE, Config.class);
		logger.info(Config.str());
	}

	private static Properties loadProp(String fileName) {
		try {
			Properties pro;

			URL purl = PropLoader.class.getResource(PROP_FILE);
			logger.debug("read prop from " + purl);
			logger.debug("class loader name "
					+ PropLoader.class.getClassLoader().toString());
			if (purl == null) {
				logger.error("Config file NOT found : "+fileName);
				// return;
			} else {
				logger.info("Config file found ! ");
			}

			pro = new Properties();
			//
			InputStream in = null;// new FileInputStream(new
									// File(purl.getPath()));
			in = PropLoader.class.getResourceAsStream(PROP_FILE);
			if (in != null) {
				pro.load(in);
				logger.info("{}", fileName+" loaded ");
				in.close();
			} else {

				logger.info("{}", fileName+" NOT load ");
			}
			return pro;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}
