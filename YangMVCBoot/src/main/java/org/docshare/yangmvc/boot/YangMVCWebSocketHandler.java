package org.docshare.yangmvc.boot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @WebServlet(name = "MyWebSocketServlet", urlPatterns = { "/ws" })
public class YangMVCWebSocketHandler extends WebSocketServlet {
	private static final Logger logger = LoggerFactory.getLogger(YangMVCWebSocketHandler.class);

    private static List<Class<?>> registeredClasses = new ArrayList<>();
    private static WebSocketServletFactory factory;
    
    @Override
    public void configure(WebSocketServletFactory factory) {
        // 设置超时
        // factory.getPolicy().setIdleTimeout(10000);
        
        // 注册所有待注册的类
        for (Class<?> clazz : registeredClasses) {
            logger.info("register WebSocket Handler: " + clazz.getName());
            factory.register(clazz);
        }
        registeredClasses.clear();
        
        YangMVCWebSocketHandler.factory = factory;
    }
    
    public static void register(Class<?> clazz) {
        if (factory == null) {
            registeredClasses.add(clazz);
        } else {
            logger.info("register WebSocket Handler: " + clazz.getName());
            factory.register(clazz);
        }
    }
}
