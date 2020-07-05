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
import org.docshare.mvc.Config;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;
import org.eclipse.jetty.util.security.Credential.MD5;

public class StaticFilterChain implements FilterChain {
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
	private String getETag(String filename,long tm){
		return MD5.digest("YangHaha"+filename+tm);
	}
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp)
			throws IOException, ServletException {
		HttpServletRequest req2 = (HttpServletRequest) req;
		HttpServletResponse hresp = (HttpServletResponse) resp;
		String uri = req2.getRequestURI();
		//Log.e("StaticFilterChain called "+uri);
//		if(uri.contains("fav.ico")){
//			resp.setContentType("image/x-icon");
//			OutputStream os = resp.getOutputStream();
//			InputStream in = getClass().getResourceAsStream("/fav.ico");
//			FileTool.writeAll(in, os);
//			return;
//		}
		String prefix = TextTool.getPrefix(uri);
		if(forbitMap.contains(prefix)){
			sendForbit(req2,hresp);
			return;
		}
		
		if(! Config.reloadable ){ //如果自动重新加载模式打开的话
			hresp.setHeader("Cache-Control", "max-age="+60*60*24*7); // 7 days
		}
		
		File f = new File("WebRoot"+ uri);
		
		InputStream in = null;
		String nowtag = null;
		if(f.exists()){
			String etag  =req2.getHeader("ETag");
			if(etag != null){
				nowtag = getETag(uri,f.lastModified());
				if(nowtag.equals(etag)){
					hresp.sendError(HttpServletResponse.SC_NOT_MODIFIED, "Not Modified o(*￣︶￣*);o");
					return;
				}
			}
			in = new FileInputStream(f);
			hresp.setHeader("ETag", nowtag);
		}
		if(in == null ){
			in = getClass().getResourceAsStream(uri);
		}

		if(in == null&& uri.contains(".ico") ){ //default ico
			in = getClass().getResourceAsStream("favicon.ico");			
		}
		if(in != null){
			try {
				String type = MIME.getMIMEType(uri);
				Log.v("data type is "+type);
				if(type != null){
					hresp.setContentType(type);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			OutputStream os = hresp.getOutputStream();
			FileTool.writeAll(in, os);
			in.close();
			return;
		}
		
		
		// not found !
			
		hresp.setCharacterEncoding("utf-8");
		hresp.setContentType("text/html;charset=utf-8");
		hresp.sendError(HttpServletResponse.SC_NOT_FOUND,"File not found ! WebRoot"+req2.getRequestURI());
		
	}
}