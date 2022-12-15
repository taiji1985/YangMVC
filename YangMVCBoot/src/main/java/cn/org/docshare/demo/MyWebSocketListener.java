package cn.org.docshare.demo;

import java.util.Date;

import org.docshare.log.Log;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MyWebSocketListener implements WebSocketListener {
	private Session session;
	@Override
	public void onWebSocketConnect(Session session) {
		Log.i("upgrade req "+ session.getUpgradeRequest().getRequestURI());
		
		Log.i("MyWebSocketListener onWebSocketConnect->"+session.getRemoteAddress());
		this.session = session;
	}
    //发送String
	@SuppressWarnings("deprecation")
	@Override
	public void onWebSocketText(String message) {
		Log.i("MyWebSocketListener onWebSocketText");
		if (session.isOpen()) {
			// echo the message back
			session.getRemote().sendString(new Date().toLocaleString() , null);
		}
	}
    //发送byte[]
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		Log.i("MyWebSocketListener onWebSocketBinary");
	}
 
	@Override
	public void onWebSocketError(Throwable cause) {
		Log.i("MyWebSocketListener Error->" + cause.getMessage());
	}
 
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		Log.i("MyWebSocketListener onWebSocketClose");
		this.session = null;
	}


}
