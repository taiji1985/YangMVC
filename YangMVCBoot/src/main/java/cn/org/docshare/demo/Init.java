package cn.org.docshare.demo;

import org.docshare.yangmvc.boot.YangMVCWebSocketHandler;
import org.docshare.yangmvc.log.Log;

public class Init {
	public Init() {
		Log.i("init called");
		YangMVCWebSocketHandler.register(MyWebSocketListener.class);
	}
}
