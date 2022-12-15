package org.docshare.boot;

import org.docshare.log.Log;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class DefaultWebSocketListener implements WebSocketListener {
	private Session session;
	@Override
	public void onWebSocketConnect(Session session) {
		Log.d("upgrade req "+ session.getUpgradeRequest().getRequestURI());
		
		Log.d("onWebSocketConnect->"+session.getRemoteAddress());
		this.session = session;
	}
    //发送String
	@Override
	public void onWebSocketText(String message) {
		Log.d("DefaultWebSocketListener","recv "+message);
	}
    //发送byte[]
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		Log.d("onWebSocketBinary");
	}
 
	@Override
	public void onWebSocketError(Throwable cause) {
		Log.d("Error->" + cause.getMessage());
	}
 
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		Log.d("onWebSocketClose");
		this.session = null;
	}


}
