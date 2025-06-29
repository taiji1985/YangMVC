package org.docshare.yangmvc.boot;

import javax.servlet.SessionCookieConfig;

import org.docshare.yangmvc.log.Log;

/**
 * 简单的SessionCookieConfig实现
 */
public class YangSessionCookieConfig implements SessionCookieConfig {
	
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
		Log.i("Session cookie comment set to: " + comment);
	}

	@Override
	public void setDomain(String domain) {
		this.domain = domain;
		Log.i("Session cookie domain set to: " + domain);
	}

	@Override
	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
		Log.i("Session cookie httpOnly set to: " + httpOnly);
	}

	@Override
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
		Log.i("Session cookie maxAge set to: " + maxAge);
	}

	@Override
	public void setName(String name) {
		this.name = name;
		Log.i("Session cookie name set to: " + name);
	}

	@Override
	public void setPath(String path) {
		this.path = path;
		Log.i("Session cookie path set to: " + path);
	}

	@Override
	public void setSecure(boolean secure) {
		this.secure = secure;
		Log.i("Session cookie secure set to: " + secure);
	}
}