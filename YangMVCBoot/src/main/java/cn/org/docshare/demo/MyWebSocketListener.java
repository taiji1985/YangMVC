package cn.org.docshare.demo;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MyWebSocketListener implements WebSocketListener {
	private static final Logger logger = LoggerFactory.getLogger(MyWebSocketListener.class);

	private Session session;
	@Override
	public void onWebSocketConnect(Session session) {
		logger.info("upgrade req "+ session.getUpgradeRequest().getRequestURI());
		
		logger.info("MyWebSocketListener onWebSocketConnect->"+session.getRemoteAddress());
		this.session = session;
	}
    //发送String
	@SuppressWarnings("deprecation")
	@Override
	public void onWebSocketText(String message) {
		logger.info("MyWebSocketListener onWebSocketText");
		if (session.isOpen()) {
			// echo the message back
			session.getRemote().sendString(new Date().toLocaleString() , null);
		}
	}
    //发送byte[]
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		logger.info("MyWebSocketListener onWebSocketBinary");
	}
 
	@Override
	public void onWebSocketError(Throwable cause) {
		logger.info("MyWebSocketListener Error->" + cause.getMessage());
	}
 
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		logger.info("MyWebSocketListener onWebSocketClose");
		this.session = null;
	}


}
