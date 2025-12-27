package org.docshare.yangmvc.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class DefaultWebSocketListener implements WebSocketListener {
	private static final Logger logger = LoggerFactory.getLogger(DefaultWebSocketListener.class);

    private Session session;
    
    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        logger.info("WebSocket connected: " + session.getRemoteAddress());
    }
    
    @Override
    public void onWebSocketText(String message) {
        logger.info("WebSocket message received: " + message);
        // Echo the message back
        try {
            if (session != null && session.isOpen()) {
                session.getRemote().sendString("Echo: " + message);
            }
        } catch (Exception e) {
            logger.error("Error sending WebSocket message: " + e.getMessage());
        }
    }
    
    @Override
    public void onWebSocketError(Throwable cause) {
        logger.error("WebSocket error: " + cause.getMessage());
    }
    
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        logger.info("WebSocket closed: " + statusCode + " - " + reason);
        this.session = null;
    }
    
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        logger.info("WebSocket binary message received, length: " + len);
        // Handle binary message if needed
    }
}
