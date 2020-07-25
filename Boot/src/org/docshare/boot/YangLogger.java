package org.docshare.boot;

import org.docshare.log.Log;
import org.eclipse.jetty.util.log.Logger;

class YangLogger implements Logger{
	static boolean showJettyDebug = false;


	@Override
	public void debug(String arg0, Throwable arg1) {
		if(showJettyDebug){
			Log.d(arg0);
		}
		
	}



	@Override
	public Logger getLogger(String arg0) {
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
		Log.w(arg0);
	}



	@Override
	public void debug(Throwable arg0) {
		if(showJettyDebug){
			Log.d(arg0);
		}
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		if(showJettyDebug){
			Log.d(String.format(arg0, arg1));
		}
	}

	@Override
	public void ignore(Throwable arg0) {
		if(showJettyDebug){
			Log.d(arg0);
		}
	}

	@Override
	public void info(Throwable arg0) {
		Log.i(arg0);
	}

	@Override
	public void info(String arg0, Object... arg1) {
		Log.i(String.format(arg0, arg1));
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		Log.i(arg0, arg1);
		
	}

	@Override
	public void warn(Throwable arg0) {
			Log.d(arg0);
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		Log.d(String.format(arg0, arg1));
	}



	@Override
	public void debug(String arg0, long arg1) {
		debug(arg0+" "+arg1);
	}
	
}