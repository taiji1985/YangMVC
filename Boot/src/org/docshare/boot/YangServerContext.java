package org.docshare.boot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import org.docshare.log.Log;
import org.docshare.util.FileTool;


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
		//因为在MVCFilter中已经读取了web.properties，所以这里就不用处理了。
		//loadProp();
	}

	HashMap<String, Object> attr = new HashMap<String, Object>();
	HashMap<String, ServletContext> servletContext = new HashMap<String, ServletContext>();
	
	
	Properties pro = null;
	long last_protime;
	static final String PROP_FILE = "/web.properties";
	@SuppressWarnings("unused")
	private void loadProp() {
		InputStream in = null;// new FileInputStream(new File(purl.getPath()));
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
			in = getClass().getResourceAsStream(PROP_FILE);
			if(in != null){
				pro.load(in);
				Log.i("web.properties loaded ");
				
			}else{

				Log.i("web.properties NOT load ");
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			FileTool.safelyClose(in);
		}
	}
	
	@Override
	public Object getAttribute(String key) {
		return attr.get(key);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new Enumeration<String>() {
			
			Set<String> set = attr.keySet();
			ArrayList<String> list = new ArrayList<String>(set);
			int it = 0;
			@Override
			public boolean hasMoreElements() {
				return it<list.size();
			}

			@Override
			public String nextElement() {
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
	public Enumeration<String> getInitParameterNames() {
		
		ArrayEnum<String> list =  new ArrayEnum<String>() ;
		list.add(pro.keys());
		return list;
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
	public Set<String> getResourcePaths(String arg0) {
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
	public Enumeration<String> getServletNames() {
		return new ArrayEnum<String>(new ArrayList<String>()); //没有
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return new ArrayEnum<Servlet>(new ArrayList<Servlet>()); //没有
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
	@Override
	public Dynamic addFilter(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Dynamic addFilter(String arg0, Filter arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void addListener(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public <T extends EventListener> void addListener(T arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addListener(Class<? extends EventListener> arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			String arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Servlet arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String arg0,
			Class<? extends Servlet> arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T extends Filter> T createFilter(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T extends EventListener> T createListener(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public <T extends Servlet> T createServlet(Class<T> arg0)
			throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void declareRoles(String... arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getEffectiveMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getEffectiveMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public FilterRegistration getFilterRegistration(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ServletRegistration getServletRegistration(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean setInitParameter(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
		// TODO Auto-generated method stub
		
	}
	//---------------------
	@Override
	public String getVirtualServerName() {
		return "YangMVC Server";
	}
	

}
