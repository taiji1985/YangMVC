package org.docshare.mvc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;



public class MVCFilter implements Filter {
	private static MVCFilter ins;
	static MVCFilter getIns(){
		return ins;
	}
	
	@Override
	public void destroy() {
		
	}
	
	private String getPureURI(String Uri,String context){
		return Uri.replaceFirst(context+"/", "");
	}
	
	
	private boolean process(String uri,String context,HttpServletRequest req, HttpServletResponse resp,FilterChain chain) throws Exception{
		String temp = getPureURI(uri, context);
		Log.i("temp = "+temp);
		
		if(!temp.contains("/")){
			temp = "index/"+temp;
		}
		String action = Config.ctr_base +"." + temp;
		action = action.replace("/", ".");
		String cname,method;
		if(action.endsWith(".")){
			cname = action.substring(0, action.length()-1);
			method = "index";
		}else{
			int t = action.lastIndexOf(".");
			cname = action.substring(0, t);
			method = action.substring(t+1);		
		}
		boolean r  = Loader.call(cname, method,req,resp);
		if(r){
			return true;
		}
		
		Log.i("action="+action);
		return false;
	}
	
	public byte[] loadResource(String path){
		URL u=ClassLoader.getSystemResource(path); 
		if(u == null){
			u = this.getClass().getResource(path);
		}
		if(u == null){
			return null;
		}
		String p2= u.getPath();
		Log.d(p2);
		return TextTool.readAllBytes(p2);	
	}
	public void outErr(HttpServletResponse resp,String msg){
		PrintWriter pw;
		try {
			pw = resp.getWriter();
			pw.print("Error:"+msg);
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static final String TEMP_BASE= "/org/docshare/res";
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req2 = (HttpServletRequest) req;
		Log.i("contentType"+req2.getContentType());
		
		String uri = req2.getRequestURI();
		String context = req2.getContextPath();
		Log.i("filter>"+uri);
		
		req2.setCharacterEncoding("utf-8");
		String temp = getPureURI(uri,context);
		/**
		 * 处理静态文件
		 */
		if(temp.startsWith("mvc_static")){
			temp =TEMP_BASE + temp.replace("mvc_static", "");
			Log.i("MVC Static File:"+temp);
			byte[] data = loadResource(temp);
			if(data == null){
				Log.i("resource not found " +temp);
				outErr((HttpServletResponse) resp,"resource not found : "+temp);
				return;
			}
			OutputStream os = resp.getOutputStream();
			os.write(data);
			os.close();
			return ;
		}
		
		if(uri.contains(".")) {
			chain.doFilter(req, resp);
			return;
		}
		
		try {
			boolean ret = process(uri,context,req2,(HttpServletResponse)resp,chain);
			if(ret)return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		ins = this;
		Config.tpl_base = cfg.getInitParameter("template");
		Config.ctr_base = cfg.getInitParameter("controller");
		
		Config.dbusr = cfg.getServletContext().getInitParameter("dbusr");
		Config.dbhost = cfg.getServletContext().getInitParameter("dbhost");
		Config.dbpwd = cfg.getServletContext().getInitParameter("dbpwd");
		Config.dbname = cfg.getServletContext().getInitParameter("dbname");
		Config.dbport = cfg.getServletContext().getInitParameter("dbport");
		
		Config.dbport = Config.dbport==null ? "3306": Config.dbport; 
		
		
		Log.i(Config.str());
		
	}

	HashMap<String,Object> map = new HashMap<String,Object>();

}
