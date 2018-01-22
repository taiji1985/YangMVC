package org.docshare.boot;




import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.util.Scanner;

import org.docshare.log.Log;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.omg.PortableServer.POAPackage.InvalidPolicyHelper;


public class ServerMain {
	
	private static int port = 1985;
	static void parseParam(String[] args){
		if(args.length>=1 ){
			port = Integer.parseInt(args[0]);
		}
	}
	public static void main(String[] args) {
		Log.i("Server start .........");
		parseParam(args);
		while(true){
			try {
				start();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	public static void start() throws Exception{
		try{
			org.eclipse.jetty.util.log.Log.setLog(new YangLogger());
	        Server server = new Server(port);
	        HandlerCollection collection =new HandlerCollection();
	        collection.addHandler(new SessionHandler());
	        collection.addHandler(new YangHandle(server));
	        collection.addHandler(new DefaultHandler());
	       
	        server.setHandler(collection);
	        server.start();
	        String url = "http://127.0.0.1";
	        if(port != 80){
	            url = String.format("http://127.0.0.1:%d",port);
	        }
	        Log.i("服务器已经开启 Server is Started");
	        Log.i("please visit "+url);
	        IpHelper.showIP();
	        Scanner scanner=new Scanner(System.in);
	        while(true){
	        	Log.i("每按一次回车键打开一次浏览器,Press Enter key to open browser");
	        	scanner.nextLine();
	        	try {
		        	 Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler "+url); 
				} catch (Exception e) {
					e.printStackTrace()	;
					break;
				}
	        }
	        
	        //server.join(); 
		}catch (BindException e) {
			Log.e("绑定端口号错误，一般是是因为端口号被其他应用占用了。 binding port error :"+port);
			String pid = IpHelper.showPortUsed(port);
			IpHelper.killPID(pid);
		}
	}
	

}
