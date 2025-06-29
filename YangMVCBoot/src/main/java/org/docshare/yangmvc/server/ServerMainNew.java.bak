package org.docshare.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerMainNew {
	public static void main(String[] args) {
		Server server = new Server();  
		  
        Connector connector = new SelectChannelConnector();  
        connector.setPort(8080);  
  
        server.setConnectors(new Connector[] { connector });  
  
        WebAppContext webAppContext = new WebAppContext("WebRoot","/");  
  
        webAppContext.setContextPath("/");  
        webAppContext.setDescriptor("web.xml");  
        webAppContext.setResourceBase("WebContent");  
        webAppContext.setDisplayName("myProject");  
        webAppContext.setClassLoader(Thread.currentThread().getContextClassLoader());  
        webAppContext.setConfigurationDiscovered(true);  
        webAppContext.setParentLoaderPriority(true);  
        server.setHandler(webAppContext);  
        System.out.println(webAppContext.getContextPath());  
        System.out.println(webAppContext.getDescriptor());  
        System.out.println(webAppContext.getResourceBase());  
        System.out.println(webAppContext.getBaseResource());  
  
        try {  
        	
            server.start();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        System.out.println("server is start");  
	}
}
