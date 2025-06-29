package org.docshare.yangmvc.boot;

import org.docshare.yangmvc.log.Log;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class DefaultWebSocketListener implements WebSocketListener {
    private Session session;
    
    @Override
    public void onWebSocketConnect(Session session) {
        this.session = session;
        Log.i("WebSocket connected: " + session.getRemoteAddress());
    }
    
    @Override
    public void onWebSocketText(String message) {
        Log.i("WebSocket message received: " + message);
        // Echo the message back
        try {
            if (session != null && session.isOpen()) {
                session.getRemote().sendString("Echo: " + message);
            }
        } catch (Exception e) {
            Log.e("Error sending WebSocket message: " + e.getMessage());
        }
    }
    
    @Override
    public void onWebSocketError(Throwable cause) {
        Log.e("WebSocket error: " + cause.getMessage());
    }
    
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        Log.i("WebSocket closed: " + statusCode + " - " + reason);
        this.session = null;
    }
    
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        Log.i("WebSocket binary message received, length: " + len);
        // Handle binary message if needed
    }
}
