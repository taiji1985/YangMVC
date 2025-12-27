package org.docshare.yangmvc.boot;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.util.FileTool;


public class YangServerContext implements ServletContext {
	private static final Logger logger = LoggerFactory.getLogger(YangServerContext.class);

	
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
	
	// 用于存储动态注册的Filter、Servlet和Listener
	private HashMap<String, Filter> filters = new HashMap<String, Filter>();
	private HashMap<String, Servlet> servlets = new HashMap<String, Servlet>();
	private ArrayList<EventListener> listeners = new ArrayList<EventListener>();
	private HashMap<String, String> initParameters = new HashMap<String, String>();
	
	
	Properties pro = null;
	long last_protime;
	static final String PROP_FILE = "/web.properties";
	@SuppressWarnings("unused")
	private void loadProp() {
		InputStream in = null;// new FileInputStream(new File(purl.getPath()));
		try {
			
			if(pro != null)return;

			URL purl = getClass().getResource(PROP_FILE);
			logger.debug("read prop from "+purl);
			logger.debug("class loader name "+getClass().getClassLoader().toString());
			
			if(purl == null){
				logger.error("Config file NOT found : web.properties ");
//				return;
			}else{
				logger.info("Config file found ! ");
			}
////			File f  = new File(purl.getPath());
////			if(!f.exists()){
////				logger.info("web.properties not found ");
////				return ;
////			}	
			pro = new Properties();	
//			
			in = getClass().getResourceAsStream(PROP_FILE);
			if(in != null){
				pro.load(in);
				logger.info("web.properties loaded ");
				
			}else{

				logger.info("web.properties NOT load ");
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
	public RequestDispatcher getNamedDispatcher(String name) {
		// 根据名称查找已注册的Servlet
		Servlet servlet = servlets.get(name);
		if (servlet != null) {
			// 这里应该返回一个RequestDispatcher实现，但由于项目架构限制，暂时返回null
			logger.warn("getNamedDispatcher not fully implemented for: " + name);
		}
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
		logger.info("{}", str);
	}

	@Override
	public void log(Exception e, String msg) {
		logger.info("{}", msg);
		
		e.printStackTrace(System.out);
	}

	@Override
	public void log(String msg, Throwable e) {
		logger.info("{}", msg);
		
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
	public Dynamic addFilter(String filterName, String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Filter> filterClass = (Class<? extends Filter>) Class.forName(className);
			return addFilter(filterName, filterClass);
		} catch (ClassNotFoundException e) {
			logger.error("Filter class not found: " + className + ", error: " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public Dynamic addFilter(String filterName, Filter filter) {
		if (filters.containsKey(filterName)) {
			return null; // Filter already exists
		}
		filters.put(filterName, filter);
		logger.info("Filter registered: " + filterName);
		// 返回一个简单的FilterRegistration.Dynamic实现
		return new YangFilterRegistration(filterName, filter);
	}
	
	@Override
	public Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		try {
			Filter filter = filterClass.newInstance();
			return addFilter(filterName, filter);
		} catch (Exception e) {
			logger.error("Failed to create filter instance: " + filterClass.getName() + ", error: " + e.getMessage());
			return null;
		}
	}
	@Override
	public void addListener(String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends EventListener> listenerClass = (Class<? extends EventListener>) Class.forName(className);
			addListener(listenerClass);
		} catch (ClassNotFoundException e) {
			logger.error("Listener class not found: " + className + ", error: " + e.getMessage());
		}
	}
	
	@Override
	public <T extends EventListener> void addListener(T listener) {
		listeners.add(listener);
		logger.info("Listener registered: " + listener.getClass().getName());
	}
	
	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		try {
			EventListener listener = listenerClass.newInstance();
			addListener(listener);
		} catch (Exception e) {
			logger.error("Failed to create listener instance: " + listenerClass.getName() + ", error: " + e.getMessage());
		}
	}
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName,
			String className) {
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Servlet> servletClass = (Class<? extends Servlet>) Class.forName(className);
			return addServlet(servletName, servletClass);
		} catch (ClassNotFoundException e) {
			logger.error("Servlet class not found: " + className + ", error: " + e.getMessage());
			return null;
		}
	}
	
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName,
			Servlet servlet) {
		if (servlets.containsKey(servletName)) {
			return null; // Servlet already exists
		}
		servlets.put(servletName, servlet);
		logger.info("Servlet registered: " + servletName);
		// 返回一个简单的ServletRegistration.Dynamic实现
		return new YangServletRegistration(servletName, servlet);
	}
	
	@Override
	public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName,
			Class<? extends Servlet> servletClass) {
		try {
			Servlet servlet = servletClass.newInstance();
			return addServlet(servletName, servlet);
		} catch (Exception e) {
			logger.error("Failed to create servlet instance: " + servletClass.getName() + ", error: " + e.getMessage());
			return null;
		}
	}
	@Override
	public <T extends Filter> T createFilter(Class<T> filterClass)
			throws ServletException {
		try {
			return filterClass.newInstance();
		} catch (Exception e) {
			throw new ServletException("Failed to create filter: " + filterClass.getName(), e);
		}
	}
	
	@Override
	public <T extends EventListener> T createListener(Class<T> listenerClass)
			throws ServletException {
		try {
			return listenerClass.newInstance();
		} catch (Exception e) {
			throw new ServletException("Failed to create listener: " + listenerClass.getName(), e);
		}
	}
	
	@Override
	public <T extends Servlet> T createServlet(Class<T> servletClass)
			throws ServletException {
		try {
			return servletClass.newInstance();
		} catch (Exception e) {
			throw new ServletException("Failed to create servlet: " + servletClass.getName(), e);
		}
	}
	@Override
	public void declareRoles(String... roleNames) {
		// 声明安全角色，在这个简单实现中只记录日志
		for (String role : roleNames) {
			logger.info("Security role declared: " + role);
		}
	}
	
	@Override
	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}
	
	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		// 返回默认的会话跟踪模式
		Set<SessionTrackingMode> modes = new java.util.HashSet<SessionTrackingMode>();
		modes.add(SessionTrackingMode.COOKIE);
		return modes;
	}
	
	@Override
	public int getEffectiveMajorVersion() {
		return 3; // Servlet 3.0
	}
	
	@Override
	public int getEffectiveMinorVersion() {
		return 0; // Servlet 3.0
	}
	
	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return new java.util.HashSet<SessionTrackingMode>(sessionTrackingModes);
	}
	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		Filter filter = filters.get(filterName);
		if (filter != null) {
			return new YangFilterRegistration(filterName, filter);
		}
		return null;
	}
	
	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		Map<String, FilterRegistration> registrations = new HashMap<String, FilterRegistration>();
		for (Map.Entry<String, Filter> entry : filters.entrySet()) {
			registrations.put(entry.getKey(), new YangFilterRegistration(entry.getKey(), entry.getValue()));
		}
		return registrations;
	}
	
	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// JSP配置描述符，在这个简单实现中返回null
		return null;
	}
	
	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		Servlet servlet = servlets.get(servletName);
		if (servlet != null) {
			return new YangServletRegistration(servletName, servlet);
		}
		return null;
	}
	
	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		Map<String, ServletRegistration> registrations = new HashMap<String, ServletRegistration>();
		for (Map.Entry<String, Servlet> entry : servlets.entrySet()) {
			registrations.put(entry.getKey(), new YangServletRegistration(entry.getKey(), entry.getValue()));
		}
		return registrations;
	}
	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		// 返回一个简单的SessionCookieConfig实现
		return new YangSessionCookieConfig();
	}
	
	@Override
	public boolean setInitParameter(String name, String value) {
		if (initParameters.containsKey(name)) {
			return false; // 参数已存在
		}
		initParameters.put(name, value);
		logger.info("Init parameter set: " + name + " = " + value);
		return true;
	}
	
	private Set<SessionTrackingMode> sessionTrackingModes = getDefaultSessionTrackingModes();
	
	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		this.sessionTrackingModes = new java.util.HashSet<SessionTrackingMode>(sessionTrackingModes);
		logger.info("Session tracking modes set: " + sessionTrackingModes);
	}
	//---------------------
	@Override
	public String getVirtualServerName() {
		return "YangMVC Server";
	}
	

}
