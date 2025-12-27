package org.docshare.yangmvc.boot;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的ServletRegistration.Dynamic实现
 */
public class YangServletRegistration implements ServletRegistration.Dynamic {
	private static final Logger logger = LoggerFactory.getLogger(YangServletRegistration.class);

	
	private String servletName;
	private Servlet servlet;
	private boolean asyncSupported = false;
	private int loadOnStartup = -1;
	private String runAsRole;
	
	public YangServletRegistration(String servletName, Servlet servlet) {
		this.servletName = servletName;
		this.servlet = servlet;
	}

	@Override
	public Set<String> addMapping(String... urlPatterns) {
		logger.info("Servlet mapping added: " + java.util.Arrays.toString(urlPatterns));
		return new java.util.HashSet<String>();
	}

	@Override
	public Collection<String> getMappings() {
		return new java.util.ArrayList<String>();
	}

	@Override
	public String getRunAsRole() {
		return runAsRole;
	}

	@Override
	public String getClassName() {
		return servlet.getClass().getName();
	}

	@Override
	public String getInitParameter(String name) {
		return null;
	}

	@Override
	public Map<String, String> getInitParameters() {
		return new java.util.HashMap<String, String>();
	}

	@Override
	public String getName() {
		return servletName;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		logger.info("Servlet init parameter set: " + name + " = " + value);
		return true;
	}

	@Override
	public Set<String> setInitParameters(Map<String, String> initParameters) {
		for (Map.Entry<String, String> entry : initParameters.entrySet()) {
			setInitParameter(entry.getKey(), entry.getValue());
		}
		return new java.util.HashSet<String>();
	}

	@Override
	public void setAsyncSupported(boolean isAsyncSupported) {
		this.asyncSupported = isAsyncSupported;
		logger.info("Servlet async support set to: " + isAsyncSupported);
	}

	@Override
	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
		logger.info("Servlet load on startup set to: " + loadOnStartup);
	}

	@Override
	public void setMultipartConfig(MultipartConfigElement multipartConfig) {
		logger.info("Servlet multipart config set");
	}

	@Override
	public void setRunAsRole(String roleName) {
		this.runAsRole = roleName;
		logger.info("Servlet run-as role set to: " + roleName);
	}

	@Override
	public Set<String> setServletSecurity(ServletSecurityElement constraint) {
		logger.info("Servlet security constraint set");
		return new java.util.HashSet<String>();
	}

	public boolean isAsyncSupported() {
		return asyncSupported;
	}
	
	public int getLoadOnStartup() {
		return loadOnStartup;
	}
}