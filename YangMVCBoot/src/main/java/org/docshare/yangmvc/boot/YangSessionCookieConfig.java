package org.docshare.yangmvc.boot;

import javax.servlet.SessionCookieConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的SessionCookieConfig实现
 */
public class YangSessionCookieConfig implements SessionCookieConfig {
	private static final Logger logger = LoggerFactory.getLogger(YangSessionCookieConfig.class);

	
	private String name = "JSESSIONID";
	private String domain;
	private String path;
	private String comment;
	private boolean httpOnly = false;
	private boolean secure = false;
	private int maxAge = -1;

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public String getDomain() {
		return domain;
	}

	@Override
	public int getMaxAge() {
		return maxAge;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isHttpOnly() {
		return httpOnly;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
		logger.info("Session cookie comment set to: " + comment);
	}

	@Override
	public void setDomain(String domain) {
		this.domain = domain;
		logger.info("Session cookie domain set to: " + domain);
	}

	@Override
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
		logger.info("Session cookie httpOnly set to: " + httpOnly);
	}

	@Override
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
		logger.info("Session cookie maxAge set to: " + maxAge);
	}

	@Override
	public void setName(String name) {
		this.name = name;
		logger.info("Session cookie name set to: " + name);
	}

	@Override
	public void setPath(String path) {
		this.path = path;
		logger.info("Session cookie path set to: " + path);
	}

	@Override
	public void setSecure(boolean secure) {
		this.secure = secure;
		logger.info("Session cookie secure set to: " + secure);
	}
}