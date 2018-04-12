package org.docshare.boot;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.docshare.log.Log;
import org.docshare.mvc.MVCFilter;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler.Context;

class StaticFilterChain implements FilterChain {
	HashSet<String> forbitMap = new HashSet<String>();
	public StaticFilterChain(){
		String[] arr = {"class","dll","exe","java","xml","properties"};
		for(String s:arr){
			forbitMap.add(s);
		}
		MIME.start();
	}
	
	private void sendForbit(HttpServletRequest req,HttpServletResponse resp){
		try {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND,"File not found ! WebRoot"+req.getRequestURI());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp)
			throws IOException, ServletException {
		HttpServletRequest req2 = (HttpServletRequest) req;
		HttpServletResponse resq2 = (HttpServletResponse) resp;
		String uri = req2.getRequestURI();
		Log.e("StaticFilterChain called "+uri);
//		if(uri.contains("fav.ico")){
//			resp.setContentType("image/x-icon");
//			OutputStream os = resp.getOutputStream();
//			InputStream in = getClass().getResourceAsStream("/fav.ico");
//			FileTool.writeAll(in, os);
//			return;
//		}
		String prefix = TextTool.getPrefix(uri);
		if(forbitMap.contains(prefix)){
			sendForbit(req2,resq2);
			return;
		}
		
		File f = new File("WebRoot"+ uri);
		
		InputStream in = null;
		if(f.exists()){
			in = new FileInputStream(f);
		}
		if(in == null ){
			in = getClass().getResourceAsStream(uri);
		}

		if(in == null&& uri.contains(".ico") ){
			in = getClass().getResourceAsStream("favicon.ico");			
		}
		if(in != null){
			try {
				String type = MIME.getMIMEType(uri);
				Log.d("data type is "+type);
				if(type != null){
					resq2.setContentType(type);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			OutputStream os = resq2.getOutputStream();
			FileTool.writeAll(in, os);
			in.close();
			return;
		}
		
		
		//Î´ÕÒµ½
			
		resq2.setCharacterEncoding("utf-8");
		resq2.setContentType("text/html;charset=utf-8");
		resq2.sendError(HttpServletResponse.SC_NOT_FOUND,"File not found ! WebRoot"+req2.getRequestURI());
		
	}
}

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
		System.out.println(string);
	}


}
