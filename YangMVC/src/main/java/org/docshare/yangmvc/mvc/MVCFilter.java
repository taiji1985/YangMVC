package org.docshare.yangmvc.mvc;

import java.io.File;
import java.io.IOException;
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

import org.docshare.yangmvc.mvc.except.FreeMarkerHandler;
import org.docshare.yangmvc.util.RequestHelper;
import org.docshare.yangmvc.util.TextTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(MVCFilter.class);
	private static MVCFilter ins;
	static MVCFilter getIns(){
		return ins;
	}
	private Configuration fmCfg;
	private ServletContext application;
	// private Rewriter rewriter=null;
	
	@Override
	public void destroy() {
		
	}
	public Configuration getFmCfg(){
		return fmCfg;
	}
	private String getPureURI(String Uri,String context){
		return Uri.replaceFirst(context+"/", "");
	}
	static Loader loader;
	
	

	
	
	private boolean process(String uri,String context,HttpServletRequest req, HttpServletResponse resp,FilterChain chain) throws Exception{
		//uri = rewriter.rewrite(uri); //重写url
		String temp = getPureURI(uri, context);
		// 处理url中的连字符，转换为驼峰式。注意：getRequestURI获取的uri不包含查询参数(?a=b)，所以参数中的连字符不会被处理。
		if(temp.contains("-")){
			temp = TextTool.hyphenToUpper(temp);
		}
		logger.trace("process {}", temp);
		
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
			logger.info("action fail ={}", action);
			return false;
		}
		
		if(! cname.endsWith("Controller") ){
			int p = cname.lastIndexOf(".");
			String lastname = cname.substring(p+1);
			lastname = Character.toUpperCase(lastname.charAt(0)) + lastname.substring(1)+"Controller";
			cname = cname.substring(0, p+1)+lastname;
			cname = TextTool.underLineToUpper(cname);
		}
		boolean r  = loader.call(uri,cname, method,req,resp);
		if(r){
			return true;
		}

		logger.info("action fail ={}", action);
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
		logger.debug("{}", p2);
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
		try {
			HttpServletRequest req2 = (HttpServletRequest) req;
			req2.setCharacterEncoding(Config.pageEncoding);

			String uri = req2.getRequestURI();
			String context = req2.getContextPath();

			for(URLFilter urlFilter: Config.urlFilters){
				if(urlFilter!= null){
					uri = urlFilter.doFilter(uri, req2, resp);;
				}
				if(uri == null){ //如果返回null，代表不处理
					chain.doFilter(req, resp);
					return;
				}
				if(uri.equals(URLFilter.DONE)){
					return ;
				}
			}


			if(context == null) context = "";
			// 注意：调用 request.getParameter*() 会消耗 InputStream，导致 JSON 请求无法读取
			// 所以这里只记录基本信息，不记录参数
			logger.debug("filter > {} {}", ((HttpServletRequest) req).getMethod(), uri);
			if(uri.contains(".")) {
				chain.doFilter(req, resp);
				return;
			}


			//如果开启reloadable，就不使用缓存。缓存主要是为了加速实际运行。
			boolean succ = !Config.reloadable && CallCacheMap.runCallCache(uri, req2, (HttpServletResponse) resp);
			if(succ){
				return;
			}


			try {
				process(uri,context,req2,(HttpServletResponse)resp,chain);
			} catch (Exception e) {
				String msg = getErrMsg(e);
				logger.error("", msg);

				Controller controller = new Controller();
				controller.setReq(req2, (HttpServletResponse) resp);
				controller.response().reset();
				controller.response().setStatus(500);
				controller.response().setContentType("text/plain");
				loader.runPostProcessing(uri, controller, msg);

				//outErr((HttpServletResponse) resp, msg);
			}
		} finally {
			// 清理 ThreadLocal，避免内存泄漏和数据污染
			Controller.clearCurrentContext();
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
					logger.info("no configure in web.xml ");
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
				logger.error("can't load YangMVC config from  web.xml------------");
				//e1.printStackTrace();
			}
		}else{
			logger.info("loaded from web.properties, skip load from web.xml");
		}
		
		
		if(Config.getProperty("groovy", null) == null){
			loader = new Loader();
		}else{
			try {
				loader = (Loader) Class.forName("org.docshare.yangmvc.mvc.GroovyLoader").newInstance();
			} catch (Exception e) {
				logger.error("", e);
				loader = new Loader();
			}
		}
		
		
		try {
			String initCls = Config.controller+".Init";
			logger.debug("try load init class {}", initCls);
			Class.forName(initCls).newInstance();
		} catch (ClassNotFoundException e) {
			logger.debug("init class not found");
		} catch (InstantiationException e) {
			logger.error("init class can not Instantiation : InstantiationException ");
		} catch (IllegalAccessException e) {
			logger.error("init class can not Instantiation : IllegalAccessException");
		}
		initFreeMarker();
		String hidePwd = Config.dbpwd;
		Config.dbpwd = "[hidden]";
		logger.info(Config.str());
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
		fmCfg.setClassicCompatible(true);
		
		
		
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
			logger.info("[template dir] {}", application.getRealPath(Config.template));
			logger.info("[template dir] {}", new File(Config.template).getAbsolutePath());
			logger.info("[template dir] classpath {}", Config.template); // /view
			logger.info("Find template dir ,use it !");
		} catch (IOException e) {
			//e.printStackTrace();
			//logger.info("view/ dir not found !  ,use classpath");
			mtl = new MultiTemplateLoader(new TemplateLoader[] {
			           wtl,ctl  });
			logger.info("[template dir] {}", application.getRealPath(Config.template));
			logger.info("[template dir] classpath /view");
		}
		
		fmCfg.setTemplateLoader(mtl);
        // 指定FreeMarker模板文件的位置  
		//fmCfg.setServletContextForTemplateLoading(application, Config.tpl_base);
	}
	public static Template getTemplate(String name) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException{
		return getIns().getFmCfg().getTemplate(name);
	}

	private static String getErrMsg(Throwable e){
		java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
		java.io.PrintWriter pw = new java.io.PrintWriter(buf, true);
		e.printStackTrace(pw);
		pw.flush();
		pw.close();
		return buf.toString();
	}

	HashMap<String,Object> map = new HashMap<String,Object>();

}
