package org.docshare.mvc;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.docshare.log.Log;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;



public class MVCFilter implements Filter {
	private static MVCFilter ins;
	static MVCFilter getIns(){
		return ins;
	}
	private Configuration fmCfg;
	private ServletContext application;
	
	@Override
	public void destroy() {
		
	}
	public Configuration getFmCfg(){
		return fmCfg;
	}
	private String getPureURI(String Uri,String context){
		return Uri.replaceFirst(context+"/", "");
	}
	
	
	private boolean process(String uri,String context,HttpServletRequest req, HttpServletResponse resp,FilterChain chain) throws Exception{
		String temp = getPureURI(uri, context);
		Log.d("process "+temp);
		
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
		boolean r  = Loader.call(uri,cname, method,req,resp);
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
			pw.flush();
			//pw.close();
		} catch (IOException e) {

			Log.e(e);
		}
	}
	public static final String TEMP_BASE= "/org/docshare/res";
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req2 = (HttpServletRequest) req;
		req2.setCharacterEncoding("utf-8");
		//Log.i("contentType"+req2.getContentType());
		
		String uri = req2.getRequestURI();
		String context = req2.getContextPath();
		if(context == null) context = "";
		Log.d("filter > "+uri);
		//如果开启了reloadable，就不再使用缓存。缓存主要是为了加速实际运行。
		boolean succ = Config.reloadable ?false: CallCacheMap.runCallCache(uri, req2, (HttpServletResponse) resp);
		if(succ){
			return;
		}
		
		
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
			process(uri,context,req2,(HttpServletResponse)resp,chain);
//			if(ret)return;
//			else{
//				outErr((HttpServletResponse) resp, "The Controller is not found ,see the log for detail");
//			}
		} catch (Exception e) {
			Log.e(e);
		}
		
		
	}
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		ins = this;
		this.application = cfg.getServletContext();
		Config.tpl_base = cfg.getInitParameter("template");
		Config.ctr_base = cfg.getInitParameter("controller");
		if(cfg.getServletContext().getInitParameter("dbusr") != null){
			Config.dbusr = cfg.getServletContext().getInitParameter("dbusr");
			Config.dbhost = cfg.getServletContext().getInitParameter("dbhost");
			Config.dbpwd = cfg.getServletContext().getInitParameter("dbpwd");
			Config.dbname = cfg.getServletContext().getInitParameter("dbname");
			Config.dbport = cfg.getServletContext().getInitParameter("dbport");
			
			
			Config.dbport = Config.dbport==null ? "3306": Config.dbport; 
			String reloadable = cfg.getServletContext().getInitParameter("reloadable");
			Config.reloadable = reloadable == null? true:Boolean.parseBoolean(reloadable);
			
		} 
		
		try {
			String initCls = Config.ctr_base+".Init";
			Log.d("try load init class " + initCls);
			Class.forName(initCls).newInstance();
			
		} catch (ClassNotFoundException e) {
			Log.d("init class not found");
		} catch (InstantiationException e) {
			Log.d("init class can not Instantiation");
		} catch (IllegalAccessException e) {
			Log.d("init class can not Instantiation");
		}
		initFreeMarker();
		String hidePwd = Config.dbpwd;
		Config.dbpwd = "*******";
		Log.i(Config.str());
		Log.i("password is hidden in Config");
		Config.dbpwd = hidePwd;
		
	}
	/**
	 * 初始化freemarker， 加载顺序 WebApp路径、文件路径、类路径
	 */
	private void initFreeMarker(){
		
		fmCfg = new Configuration(Configuration.VERSION_2_3_25);  
		fmCfg.setDefaultEncoding("utf-8");
		
		TemplateLoader ctl = new ClassTemplateLoader(MVCFilter.class,
	            "/view");

		WebappTemplateLoader wtl = new WebappTemplateLoader(application,Config.tpl_base);
		
	    MultiTemplateLoader mtl; 
	    TemplateLoader ftl1;
		try {
			ftl1 = new FileTemplateLoader(new File("view/"));
			mtl = new MultiTemplateLoader(new TemplateLoader[] {
			           wtl,ftl1 , ctl  });
			Log.i("Find view/ dir ,use it !");
		} catch (IOException e) {
			//e.printStackTrace();
			Log.i("view/ dir not found !  ,use classpath");
			mtl = new MultiTemplateLoader(new TemplateLoader[] {
			           wtl,ctl  });
		}
		
		fmCfg.setTemplateLoader(mtl);
        // 指定FreeMarker模板文件的位置  
		//fmCfg.setServletContextForTemplateLoading(application, Config.tpl_base);
	}
	HashMap<String,Object> map = new HashMap<String,Object>();

}
