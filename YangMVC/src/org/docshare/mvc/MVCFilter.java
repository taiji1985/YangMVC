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
import org.docshare.mvc.except.FreeMarkerHandler;
import org.docshare.mvc.except.MVCException;
import org.docshare.util.RequestHelper;
import org.docshare.util.TextTool;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;



public class MVCFilter implements Filter {
	private static MVCFilter ins;
	static MVCFilter getIns(){
		return ins;
	}
	private Configuration fmCfg;
	private ServletContext application;
	private Rewriter rewriter=null;
	
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
		//uri = rewriter.rewrite(uri); //重写url
		String temp = getPureURI(uri, context);
		Log.v("process "+temp);
		
		if(!temp.contains("/")){
			temp = "index/"+temp;
		}
		String action = Config.controller +"." + temp;
		action = action.replace("/", ".");
		String cname,method;
		if(action.endsWith(".")){ //如果是以斜杠结束（斜杠被替换成了.),则是访问index方法。
			cname = action.substring(0, action.length()-1);
			method = "index";
		}else{
			int t = action.lastIndexOf(".");
			cname = action.substring(0, t);
			method = action.substring(t+1);		
		}
		if(cname == null || method == null){
			Log.i("action fail ="+action);
			return false;
		}
		
		if(! cname.endsWith("Controller") ){
			int p = cname.lastIndexOf(".");
			String lastname = cname.substring(p+1);
			lastname = Character.toUpperCase(lastname.charAt(0)) + lastname.substring(1)+"Controller";
			cname = cname.substring(0, p+1)+lastname;
			cname = TextTool.underLineToUpper(cname);
		}
		boolean r  = Loader.call(uri,cname, method,req,resp);
		if(r){
			return true;
		}
		
		Log.i("action fail ="+action);
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
//		PrintWriter pw;
//		try {
//			pw = resp.getWriter();
//			pw.print("Error:"+msg);
//			pw.flush();
//			//pw.close();
//		} catch (IOException e) {
//			throw new MVCException(e);
//		}
		PrintWriter pw;
		try {
			resp.setCharacterEncoding("utf-8");
			resp.setContentType("text/html; charset=UTF-8");
			pw = resp.getWriter();
			pw.println(TextTool.txt2HTML(msg));
			pw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	//public static final String TEMP_BASE= "/org/docshare/res";
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req2 = (HttpServletRequest) req;
		req2.setCharacterEncoding(Config.pageEncoding);
		
		String uri = req2.getRequestURI();
		String context = req2.getContextPath();
		if(context == null) context = "";
		Log.d("filter > ",uri,",param = [",RequestHelper.params(req2),"]"); 
		if(uri.contains(".")) {
			chain.doFilter(req, resp);
			return;
		}
		//如果开启了reloadable，就不再使用缓存。缓存主要是为了加速实际运行。
		boolean succ = Config.reloadable ?false: CallCacheMap.runCallCache(uri, req2, (HttpServletResponse) resp);
		if(succ){
			return;
		}
		
		
		try {
			process(uri,context,req2,(HttpServletResponse)resp,chain);
		} catch (Exception e) {
			String msg=Log.getErrMsg(e);
			Log.e(msg);

			Controller controller = new Controller();
			controller.request = req2;
			controller.response = (HttpServletResponse) resp;
			controller.response.setStatus(500);
			Loader.runPostProcessing(uri, controller, msg);
			
			//outErr((HttpServletResponse) resp, msg);
		}
		
		
	}
	private String loadConfig(String cfgName,String def){
		String r = application.getInitParameter(cfgName);
		return r == null? def :r;
	}
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		ins = this;
		//先从类目录的web.properties中读取
		boolean loaded = Config.loadProperties("/web.properties");
		//rewriter=new Rewriter(cfg.getServletContext().getRealPath("/"));
		this.application = cfg.getServletContext();
		if(!loaded){
			try {
				String tpl = cfg.getInitParameter("template");
				if(tpl == null){
					Log.i("no configure in web.xml ");
				}else{
					Config.template = tpl;
				}
				
				String ctr = cfg.getInitParameter("controller");
				if(ctr !=null){
					Config.controller = ctr;
				}
				Config.template = Config.template == null? "/view" :Config.template;
				Config.controller = Config.controller == null? "org.demo":Config.controller;
				if(application.getInitParameter("dbusr") != null){
					Config.dbusr  = loadConfig("dbusr" ,Config.dbusr);
					Config.dbhost = loadConfig("dbhost",Config.dbhost);
					Config.dbpwd  = loadConfig("dbpwd" ,Config.dbpwd);
					Config.dbname = loadConfig("dbname",Config.dbname);
					Config.dbport = loadConfig("dbport",Config.dbport);	
					Config.dbtype = loadConfig("dbtype",Config.dbtype);	
					Config.dbschema = loadConfig("dbschema",Config.dbschema);				
					Config.reloadable = Boolean.parseBoolean(loadConfig("reloadable", Config.reloadable+""));
				}
			} catch (Exception e1) {
				Log.e("can't load YangMVC config from  web.xml------------");
				//e1.printStackTrace();
			} 
		}else{
			Log.i("loaded from web.properties, skip load from web.xml");
		}
		
		try {
			String initCls = Config.controller+".Init";
			Log.d("try load init class " + initCls);
			Class.forName(initCls).newInstance();
		} catch (ClassNotFoundException e) {
			Log.d("init class not found");
		} catch (InstantiationException e) {
			Log.e("init class can not Instantiation : InstantiationException ");
		} catch (IllegalAccessException e) {
			Log.e("init class can not Instantiation : IllegalAccessException");
		}
		initFreeMarker();
		String hidePwd = Config.dbpwd;
		Config.dbpwd = "[hidden]";
		Log.i(Config.str());
		Config.dbpwd = hidePwd;
		
	}
	/**
	 * 初始化freemarker， 加载顺序 WebApp路径、文件路径、类路径
	 */
	private void initFreeMarker(){
		
		fmCfg = new Configuration(Configuration.VERSION_2_3_25);  
		fmCfg.setDefaultEncoding("utf-8");
		fmCfg.setLocalizedLookup(false);
		fmCfg.setTemplateExceptionHandler(new FreeMarkerHandler());
		
		//fmCfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		TemplateLoader ctl = new ClassTemplateLoader(MVCFilter.class,
	            "/view");
		//fmCfg.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
		WebappTemplateLoader wtl = new WebappTemplateLoader(application,Config.template);
		
	    MultiTemplateLoader mtl; 
	    TemplateLoader ftl1;
		try {
			ftl1 = new FileTemplateLoader(new File(Config.template)); // /view
			mtl = new MultiTemplateLoader(new TemplateLoader[] {
			           wtl,ftl1 , ctl  });
			Log.i("[template dir] "+ application.getRealPath(Config.template));
			Log.i("[template dir] "+ new File(Config.template).getAbsolutePath());
			Log.i("[template dir] classpath "+Config.template); // /view
			Log.i("Find template dir ,use it !");
		} catch (IOException e) {
			//e.printStackTrace();
			//Log.i("view/ dir not found !  ,use classpath");
			mtl = new MultiTemplateLoader(new TemplateLoader[] {
			           wtl,ctl  });
			Log.i("[template dir] "+ application.getRealPath(Config.template));
			Log.i("[template dir] classpath /view");
		}
		
		fmCfg.setTemplateLoader(mtl);
        // 指定FreeMarker模板文件的位置  
		//fmCfg.setServletContextForTemplateLoading(application, Config.tpl_base);
	}
	public static Template getTemplate(String name) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException{
		return getIns().getFmCfg().getTemplate(name);
	}
	HashMap<String,Object> map = new HashMap<String,Object>();

}
