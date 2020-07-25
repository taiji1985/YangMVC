package cn.org.docshare.demo;

import org.docshare.boot.MyWebSocketHandler;
import org.docshare.log.Log;

public class Init {
	public Init() {
		Log.i("init called");
		MyWebSocketHandler.register(MyWebSocketListener.class);
	}
}
