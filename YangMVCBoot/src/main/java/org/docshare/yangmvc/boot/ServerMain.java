package org.docshare.yangmvc.boot;




import java.io.IOException;
import java.util.Scanner;

import org.docshare.yangmvc.log.Log;
import org.docshare.yangmvc.mvc.Config;
import org.docshare.yangmvc.util.FileTool;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.server.session.CachingSessionDataStore;
// Session imports removed for basic Jetty 11 compatibility
import org.eclipse.jetty.server.session.NullSessionCache;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;


public class ServerMain {
	
	private static int port = 1985;
	static void parseParam(String[] args){
		Log.i("Usage: java -jar xxx.jar [port] [nowebsocket] ");
		Log.i("   eg: java -jar aa_boot.jar ");
		Log.i("   eg: java -jar aa_boot.jar 80 nowebsocket ");
		Log.i("   That means listen at port 80 and use no websocket");
		if(args.length>=1 ){
			port = Integer.parseInt(args[0]);
		}
		if(args.length >= 2  && "nowebsocket".equals(args[1])){
			supportWebSocket = false;
		}
	}
	public static void main(String[] args) {
		Log.i("Server start .........");
		parseParam(args);
		if(true){
			try {
				start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    public static ContextHandler contextHandler = null;
    public static boolean supportWebSocket = true;
    public static boolean useSession = true;
	public static boolean useRedis = false;
	private static SessionHandler sessionHandler=null;
	public static SessionHandler getSessionHandler(){
		return sessionHandler;
	}

	public static RedisSessionDataMap sessionDataMap = null;
	private static String _prefix=null;
	public static void setRedisSessionPrefix(String prefix){
		_prefix = prefix;
		if(sessionDataMap!=null){
			sessionDataMap.setKeyPrefix(prefix);
		}
		//sessionDataMap.setKeyPrefix(prefix);
	}
	@SuppressWarnings("unused")
	private static void applyRedis(){
		Log.i("use redis sessionCache");
		NullSessionCache dsc = new NullSessionCache(sessionHandler);
		String redis_host = Config.getProperty("redis_host", "localhost");
		String redis_port = Config.getProperty("redis_port","6379");
		try {
			//Class<?> redisClass = Class.forName("org.docshare.boot.RedisSessionDataMap");
			sessionDataMap = new RedisSessionDataMap();
			sessionDataMap.setKeyPrefix(_prefix);
			dsc.setSessionDataStore(new CachingSessionDataStore(sessionDataMap,
					new NullSessionDataStore()));
			sessionHandler.setSessionCache(dsc);

		} catch (Exception e) {
			Log.e("Unable to load redis class" +e.getMessage());
		}

	}
	public static void start() throws Exception{
        Server server = new Server(port);
        String url = "http://127.0.0.1";
        if(port != 80){
            url = String.format("http://127.0.0.1:%d",port);
        }
        Config.loadProperties("/web.properties");
		try{
			org.eclipse.jetty.util.log.Log.setLog(new YangLogger());
	        
	        HandlerList collection = new HandlerList(); 

	        contextHandler = new ContextHandler();
	        
	        contextHandler.setContextPath("/");
	        contextHandler.setResourceBase("./WebRoot");
	        contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
	        //contextHandler.setHandler(YangHandle.getIns(server));
	        // Session support temporarily disabled due to dependency issues
	        if(useSession){
	        	sessionHandler = new SessionHandler();
				//----------------------------
				if(useRedis){
					applyRedis();
				}

				//----------------------------
	        	collection.addHandler(sessionHandler);
	        }
	        collection.addHandler(YangHandle.getIns(server));
	        collection.addHandler(new ResourceHandler());
	        collection.addHandler(new DefaultHandler());
	        
	        
	        
	        contextHandler.setHandler(collection);
	        if(supportWebSocket){
				//support websocket 
				ServletContextHandler wsHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
				wsHandler.setContextPath("/ws");
				wsHandler.addServlet(YangMVCWebSocketHandler.class, "/*");
			
				ContextHandlerCollection final_handler = new ContextHandlerCollection();
					
				final_handler.addHandler(wsHandler);
				final_handler.addHandler(contextHandler);
					
				server.setHandler(final_handler);
	        }else{
	        	server.setHandler(contextHandler);
	        }
	        server.start();
	        
	        Log.i("Server is Started");
	        Log.i("please visit "+url);
	        IpHelper.showIP();
	        readConsole(url);
	        
	        //server.join(); 
		}catch (IOException e) {
			Log.e("Port binding error, port may be occupied by another application :"+port);
			String pid = IpHelper.showPortUsed(port);
			//Retry!!
			if(System.getProperty("os.name").contains("Win")){
				try {
					IpHelper.killPID(pid);
					Thread.sleep(3000);
					server.start();
			        readConsole(url);
				} catch (Exception e1) {
					Log.e(e1);
				}
			}
		}
	}
	
	private static void readConsole(String url) {
		Scanner scanner  = null;
		try {
			scanner= new Scanner(System.in);
			while (true) {
				Log.i("Press Enter key to open browser");
				String ret = scanner.nextLine();
				if("exit".equals(ret))break;
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
		} catch (Exception e) {
			//Log.e(e);
		}finally {
			FileTool.safelyClose(scanner);
		}
	}

}
