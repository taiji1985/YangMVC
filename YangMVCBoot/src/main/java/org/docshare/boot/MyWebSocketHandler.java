package org.docshare.boot;

import java.util.ArrayList;

import org.docshare.log.Log;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MyWebSocketHandler extends WebSocketHandler {
	static WebSocketServletFactory fac;
	@Override
	public void configure(WebSocketServletFactory factory) {
		 // 设置超时
        //factory.getPolicy().setIdleTimeout(10000);
        // 注册
        //factory.register(DefaultWebSocketListener.class);
        
       
        for(Class<?> c: arrayList){
			Log.i("register WebSocket Handler2: "+c.getName());
        	factory.register(c);
        	
        }
        arrayList.clear();
        
        fac = factory;
        
        //factory.register(MyWebSocketListener.class);
	}
	static ArrayList<Class<?>> arrayList=new ArrayList<>();
	public static void register(Class<?> clazz){
		
		if(fac == null){
			arrayList.add(clazz);
		}
		else{
			Log.i("register WebSocket Handler "+clazz.getName());
			fac.register(clazz);
		}
	}


}
