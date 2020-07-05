package org.docshare.boot;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;
import org.docshare.mvc.MVCFilter;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class YangHandle extends AbstractHandler {

	StaticFilterChain chain =new StaticFilterChain();
	
	@SuppressWarnings("unused")
	private Server server = null;
	private MVCFilter filter;
//	HashSessionManager manager =new HashSessionManager();
//	HashSessionIdManager	sim  = new HashSessionIdManager(new Random());
	private static YangHandle ins = null;
	public static YangHandle getIns(Server server){
		if(ins == null){
			ins = new YangHandle(server);
		}
		return ins;
	}
	private YangHandle(Server server) {
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
//		manager.setIdManager(sim);
//		manager.doStart();
	}
	
	@Override
	protected void doStop() throws Exception {
//		manager.doStop();
		//sim.doStop();
		super.doStop();
	}

	@Override
	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) throws IOException, ServletException {
		T("visit "+ target);
//		response.setContentType("text/plain");  
//        response.getWriter().println("hello");  
//        baseRequest.setHandled(true);  
		//Context c = baseRequest.getContext();
		
		filter.doFilter(request, response, chain);

		baseRequest.setHandled(true); 
		
	}

	private void T(String string) {
		Log.i(string);
	}


}
