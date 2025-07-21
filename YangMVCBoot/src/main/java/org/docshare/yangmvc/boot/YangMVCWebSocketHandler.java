package org.docshare.yangmvc.boot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.docshare.yangmvc.log.Log;

// @WebServlet(name = "MyWebSocketServlet", urlPatterns = { "/ws" })
public class YangMVCWebSocketHandler extends WebSocketServlet {
    private static List<Class<?>> registeredClasses = new ArrayList<>();
    private static WebSocketServletFactory factory;
    
    @Override
    public void configure(WebSocketServletFactory factory) {
        // 设置超时
        // factory.getPolicy().setIdleTimeout(10000);
        
        // 注册所有待注册的类
        for (Class<?> clazz : registeredClasses) {
            Log.i("register WebSocket Handler: " + clazz.getName());
            factory.register(clazz);
        }
        registeredClasses.clear();
        
        YangMVCWebSocketHandler.factory = factory;
    }
    
    public static void register(Class<?> clazz) {
        if (factory == null) {
            registeredClasses.add(clazz);
        } else {
            Log.i("register WebSocket Handler: " + clazz.getName());
            factory.register(clazz);
        }
    }
}
