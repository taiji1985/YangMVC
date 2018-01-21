package org.docshare.boot;

import org.docshare.log.Log;
import org.eclipse.jetty.util.log.Logger;

class YangLogger implements Logger{
	static boolean showJettyDebug = false;
	@Override
	public void debug(String arg0) {
		if(showJettyDebug){
			Log.d(arg0);
		}
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		if(showJettyDebug){
			Log.d(arg0);
		}
		
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
		if(showJettyDebug){
			Log.d(join(arg0,arg1,arg2));
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
	public void info(String arg0) {
		Log.i(arg0);
		
	}
	private String join(String a,Object b,Object c){
		String r = a;
		if(b != null && !b.equals("null")){
			r += " "+ b;
		}
		if(c != null && !b.equals("null")){
			r += " "+ c;
		}
		return r;
	}
	@Override
	public void info(String arg0, Object arg1, Object arg2) {
		Log.i(join(arg0,arg1,arg2));
			
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

	@Override
	public void setDebugEnabled(boolean arg0) {
		
	}

	@Override
	public void warn(String arg0) {
		Log.w(arg0);
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		Log.w(arg0);
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
		Log.w(join(arg0,arg1,arg2));
	}
	
}