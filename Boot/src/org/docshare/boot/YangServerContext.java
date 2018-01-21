package org.docshare.boot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.docshare.log.Log;


public class YangServerContext implements ServletContext {
	
	private static YangServerContext ins;

	/**
	 * 单例模式
	 * @return
	 */
	public static YangServerContext getIns(){
		if(ins == null){
			ins =  new YangServerContext();
		}
		return ins;
	}
	/**
	 * 必须通过getIns来创建
	 */
	private YangServerContext() {  
		loadProp();
	}

	HashMap<String, Object> attr = new HashMap<String, Object>();
	HashMap<String, ServletContext> servletContext = new HashMap<String, ServletContext>();
	
	
	Properties pro = null;
	long last_protime;
	static final String PROP_FILE = "/web.properties";
	private void loadProp() {
		try {
			
			if(pro != null)return;

			URL purl = getClass().getResource(PROP_FILE);
			Log.d("read prop from "+purl);
			Log.d("class loader name "+getClass().getClassLoader().toString());
			
			if(purl == null){
				Log.e("Config file NOT found : web.properties ");
//				return;
			}else{
				Log.i("Config file found ! ");
			}
////			File f  = new File(purl.getPath());
////			if(!f.exists()){
////				Log.i("web.properties not found ");
////				return ;
////			}	
			pro = new Properties();	
//			
			InputStream in = null;// new FileInputStream(new File(purl.getPath()));
			in = getClass().getResourceAsStream(PROP_FILE);
			if(in != null){
				pro.load(in);
				Log.i("web.properties loaded ");
				in.close();
			}else{

				Log.i("web.properties NOT load ");
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object getAttribute(String key) {
		return attr.get(key);
	}

	@Override
	public Enumeration<?> getAttributeNames() {
		return new Enumeration<Object>() {
			
			Set<String> set = attr.keySet();
			ArrayList<String> list = new ArrayList<String>(set);
			int it = 0;
			@Override
			public boolean hasMoreElements() {
				return it<list.size();
			}

			@Override
			public Object nextElement() {
				return list.get(it++);
			}
			
		};
	}

	@Override
	public ServletContext getContext(String url) { //Deprecated
		return this;
	}

	@Override
	public String getContextPath() {
		return "/";
	}

	@Override
	public String getInitParameter(String key) {
		
		return pro.getProperty(key);
	}

	@Override
	public Enumeration<?> getInitParameterNames() {
		
		return pro.keys() ;
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public String getMimeType(String file) {
		return MIME.getMIMEType(file);
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		//TODO
		return null;
	}

	@Override
	public String getRealPath(String path) {
		return new File("./",path).getAbsolutePath();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		throw new RuntimeException("No Implement Exception");
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		//throw new RuntimeException("No Implement Exception");	
		return this.getClass().getClassLoader().getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return this.getClass().getClassLoader().getResourceAsStream(path);
	}

	@Override
	public Set<?> getResourcePaths(String arg0) {
		throw new RuntimeException("No Implement Exception");
	}

	@Override
	public String getServerInfo() {
		return "YangServer";
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		throw new RuntimeException("No Implement Exception");
	}

	@Override
	public String getServletContextName() {
		return YangServerContext.class.getName();
	}

	@Override
	public Enumeration<?> getServletNames() {
		return new ArrayEnum<String>(new ArrayList<String>()); //没有
	}

	@Override
	public Enumeration<?> getServlets() {
		return new ArrayEnum<HttpServlet>(new ArrayList<HttpServlet>()); //没有
	}

	@Override
	public void log(String str) {
		Log.i(str);
	}

	@Override
	public void log(Exception e, String msg) {
		Log.i(msg);
		
		e.printStackTrace(System.out);
	}

	@Override
	public void log(String msg, Throwable e) {
		Log.i(msg);
		
		e.printStackTrace(System.out);
		
	}

	@Override
	public void removeAttribute(String key) {
		attr.remove(key);
	}

	@Override
	public void setAttribute(String key, Object val) {
		attr.put(key,val);
	}

}
