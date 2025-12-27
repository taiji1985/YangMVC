package org.docshare.yangmvc.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class YangLogger implements org.eclipse.jetty.util.log.Logger{
	private static final Logger logger = LoggerFactory.getLogger(YangLogger.class);

	static boolean showJettyDebug = false;


	@Override
	public void debug(String arg0, Throwable arg1) {
		if(showJettyDebug){
			logger.debug("{}", arg0);
		}
		
	}



	@Override
	public org.eclipse.jetty.util.log.Logger getLogger(String arg0) {
		return this;
	}

	@Override
	public String getName() {
		return "YangLog";
	}


	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void setDebugEnabled(boolean arg0) {
		
	}



	@Override
	public void warn(String arg0, Throwable arg1) {
		logger.warn("{}", arg0);
	}



	@Override
	public void debug(Throwable arg0) {
		if(showJettyDebug){
			logger.debug("{}", arg0);
		}
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		if(showJettyDebug){
			logger.debug(String.format(arg0, arg1));
		}
	}

	@Override
	public void ignore(Throwable arg0) {
		if(showJettyDebug){
			logger.debug("{}", arg0);
		}
	}

	@Override
	public void info(Throwable arg0) {
		logger.info("{}", arg0);
	}

	@Override
	public void info(String arg0, Object... arg1) {
		logger.info(String.format(arg0, arg1));
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		logger.info(arg0, arg1);
		
	}

	@Override
	public void warn(Throwable arg0) {
			logger.debug("{}", arg0);
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		logger.debug(String.format(arg0, arg1));
	}



	@Override
	public void debug(String arg0, long arg1) {
		debug(arg0+" "+arg1);
	}
	
}