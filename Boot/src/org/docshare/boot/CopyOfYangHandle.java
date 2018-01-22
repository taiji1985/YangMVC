package org.docshare.boot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.docshare.log.Log;
import org.docshare.mvc.MVCFilter;
import org.docshare.util.FileTool;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;

class StaticFilterChainCopy implements FilterChain {
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp)
			throws IOException, ServletException {
		HttpServletRequest req2 = (HttpServletRequest) req;
		HttpServletResponse resq2 = (HttpServletResponse) resp;
		String uri = req2.getRequestURI();
		Log.e("StaticFilterChain called "+uri);
		if(uri.contains("fav.ico")){
			resp.setContentType("image/x-icon");
			OutputStream os = resp.getOutputStream();
			InputStream in = getClass().getResourceAsStream("/fav.ico");
			FileTool.writeAll(in, os);
			return;
		}
		File f = new File("WebRoot"+req2.getRequestURI());
		if(! f.exists()){
			
			resq2.setCharacterEncoding("utf-8");
			resq2.setContentType("text/html;charset=utf-8");
			resq2.sendError(HttpServletResponse.SC_NOT_FOUND,"File not found ! WebRoot"+req2.getRequestURI());
			//OutputStream os = resp.getOutputStream();
			//FileTool.writeAll(os, "404 File not found ! 没找到这个文件，请确保你有一个 文件: WebRoot/"+req2.getRequestURI(), "utf-8");	
		}
	}
};
public class CopyOfYangHandle extends AbstractHandler {

	StaticFilterChain chain =new StaticFilterChain();
	
	@SuppressWarnings("unused")
	private Server server = null;
	private MVCFilter filter;
	HashSessionManager manager =new HashSessionManager();
	HashSessionIdManager	sim  = new HashSessionIdManager(new Random());

	public CopyOfYangHandle(Server server) {
		this.server =server;
		filter = new  MVCFilter();
		try {
			filter.init(new YangFilterConfig());
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void doStart() throws Exception {
		super.doStart();
		//sim.doStart();
		manager.setIdManager(sim);
		manager.doStart();
	}
	
	@Override
	protected void doStop() throws Exception {
		manager.doStop();
		//sim.doStop();
		super.doStop();
	}

	@Override
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		T("visit "+ target);
//		response.setContentType("text/plain");  
//        response.getWriter().println("hello");  
//        baseRequest.setHandled(true);  
		
		
		if(baseRequest.getSessionManager() == null){
			Log.e("baseRequest set session manager ");
			baseRequest.setSessionManager(manager);
		}
		
		HttpSession ss = request.getSession();
		System.err.println(ss.getId());
		filter.doFilter(request, response, chain);

		baseRequest.setHandled(true);  
	}

	private void T(String string) {
		System.out.println(string);
	}


}
