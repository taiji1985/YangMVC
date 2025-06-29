package cn.org.docshare.demo;

import org.docshare.yangmvc.boot.MyWebSocketHandler;
import org.docshare.yangmvc.log.Log;

public class Init {
	public Init() {
		Log.i("init called");
		MyWebSocketHandler.register(MyWebSocketListener.class);
	}
}
