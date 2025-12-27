package org.docshare.yangmvc.boot;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的FilterRegistration.Dynamic实现
 */
public class YangFilterRegistration implements FilterRegistration.Dynamic {
	private static final Logger logger = LoggerFactory.getLogger(YangFilterRegistration.class);

	
	private String filterName;
	private Filter filter;
	private boolean asyncSupported = false;
	
	public YangFilterRegistration(String filterName, Filter filter) {
		this.filterName = filterName;
		this.filter = filter;
	}

	@Override
	public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes,
			boolean isMatchAfter, String... servletNames) {
		logger.info("Filter mapping added for servlets: " + java.util.Arrays.toString(servletNames));
	}

	@Override
	public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes,
			boolean isMatchAfter, String... urlPatterns) {
		logger.info("Filter mapping added for URL patterns: " + java.util.Arrays.toString(urlPatterns));
	}

	@Override
	public Collection<String> getServletNameMappings() {
		return new java.util.ArrayList<String>();
	}

	@Override
	public Collection<String> getUrlPatternMappings() {
		return new java.util.ArrayList<String>();
	}

	@Override
	public String getClassName() {
		return filter.getClass().getName();
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
		return filterName;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		logger.info("Filter init parameter set: " + name + " = " + value);
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
		logger.info("Filter async support set to: " + isAsyncSupported);
	}

	public boolean isAsyncSupported() {
		return asyncSupported;
	}
}