package org.docshare.mvc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileUploadException;
import org.docshare.log.Log;
import org.docshare.mvc.except.NullParamException;
import org.docshare.orm.DBTool;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;
import org.docshare.util.BeanUtil;
import org.docshare.util.FileTool;
import org.docshare.util.GzipUtil;
import org.docshare.util.IOUtil;
import org.docshare.util.TextTool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import freemarker.template.Template;
/**
 * 所有控制器的父类
 * @author 杨同峰
 *
 */
public class Controller {

	private static final String M_FLAG="multipart/form-data";

	/**
	 * 请求对象，同jsp中的request。 但请优先使用param等函数
	 */
	public HttpServletRequest request;
	/**
	 * 响应对象，同jsp中的response。 但请优先使用output、outputJSON等函数
	 */
	public HttpServletResponse response;
	/**
	 * 会话对象。同jsp中的session。但请优先使用sess函数
	 */
	public HttpSession session; 
	/**
	 * 输出对象，类似jsp中的out对象，但请优先使用output、outputJSON、downloadFile等函数。
	 */
	protected PrintWriter writer = null;
	private Map<String, Object> paramMap=new HashMap<String, Object>();

	/**
	 * 同jsp中的application对象。
	 */
	public ServletContext application;


	private boolean single = false;
	
	private HashMap<String, String> cookieMap = new HashMap<String, String>();
	/**
	 * 追加参数， 同样可以用param获取到最佳的参数
	 * @param key 参数名
	 * @param val 参数值
	 */
	public void putParam(String key,Object val) {
		Log.d("put param: "+key+"= "+val);
		paramMap.put(key, val);
		
	}
	/**
	 * 返回当前请求是否为Get请求，
	 * @return 当为Get请求时返回真，否则返回假。
	 */
	public boolean isGet(){
		return request.getMethod().toLowerCase().equals("get");
	}
	/**
	 * 返回当前请求是否为Post请求，
	 * @return 当为Post请求时返回真，否则返回假。
	 */
	public boolean isPost(){
		return request.getMethod().toLowerCase().equals("post");
	}
	/**
	 * 获取HTTP请求的方法，GET/POST/PUT 等
	 * @return
	 */
	public String method() {
		return request.getMethod();
	}
	
	/**
	 * 获取当前控制器对应的URL路径
	 * @return 控制器对应的URL路径
	 */
	public String getPath(){
		String uri = request.getRequestURI();
		//uri = uri.replace(context, "");
		return uri;
	}
	/**
	 * 获取默认的模板
	 * @return 模板路径
	 */
	public String getDefaultTemp(){
		String uri = request.getRequestURI() +".jsp";
		String context = request.getContextPath();
		uri = uri.replace(context, "").replace("/.jsp", "/index.jsp");
		return uri;
	}
	/**
	 * 自动处理分页问题，对应URL参数 page 为页码， pagesz为页面大小（默认为30)
	 * @return 返回所需的对象列表
	 */
	public LasyList page(LasyList list){
		return page(list,30);
	}
	public String getClearQuery(String q){
		if(q == null)return "";
		String[] sa=q.split("&");
		if(sa==null ) return "";
		
		int c = 0;
		String r = "";
		for(String s: sa){
			if(s.length() == 0 || s.startsWith("page") || s.startsWith("pagesz")){
				continue;
			}
			if(c == 0){
				r = "&"+s;
			}else{
				r = r + "&"+s;
			}
			c++;
		}
		return r;
	}
	/**
	 * 自动处理分页问题，对应URL参数 page 为页码， pagesz为页面大小（默认为30)
	 * @param list 原列表
	 * @param pagesize 每页的记录数
	 * @return 返回所需的对象列表
	 */
	public LasyList page(LasyList list,int pagesize){
		int total = list.size();
		
		Integer page =  (Integer) paramInt("page", 1);
		Integer pagesz =  (Integer) paramInt("pagesz", pagesize);
		int pagec = (int) Math.floor(total/pagesz)+1;
		Log.d(total/pagesz);
		
		Integer prev = page - 1 <= 0 ? null: page-1;
		Integer next = page+1>pagec? null:page+1;
		
		int start = page -5 < 1 ? 1: page-5;
		int end = page +5 > pagec? pagec:page+5;
		end  = (start +10)<pagec ? start+10:end;
		start = (end - 10 )>=0 ? end-10:start;
		List<Integer> pagelist = new ArrayList<Integer>();
		String query = request.getQueryString();
		query  =getClearQuery(query);
		
		StringBuffer sb = new StringBuffer();
		sb.append("<ul class='yangmvc_page'>");
		if(prev!=null){
			sb.append(String.format("<li><a href='%s?page=%d&pagesz=%d%s'>&lt;&lt;</a></li>", getPath(),prev,pagesz,query));
			//sb.append("<li><a href='"+getPath()+"?page="+prev+query+"'>&lt;&lt;</a></li>");
		}else{
			sb.append("<li>&lt;&lt;</li>");
		}
		for(int i=start;i<=end;i++){
			pagelist.add(i);
			if(i== page.intValue()){
				sb.append("<li>"+i+"</li>");
			}else{
				sb.append(String.format("<li><a href='%s?page=%d&pagesz=%d%s'>%d</a></li>", getPath(),i,pagesz,query,i));
				//sb.append("<li><a href='"+getPath()+"?page="+i+"'>"+i+"</a></li>");
			}
		}
		if(next != null){
			sb.append(String.format("<li><a href='%s?page=%d&pagesz=%d%s'>&gt;&gt</a></li>",getPath(),next,pagesz,query));
			//sb.append("<li><a href='"+getPath()+"?page="+next+"'>&gt;&gt;</a></li>");
		}else{
			sb.append("<li>&gt;&gt;</li>");
		}
		sb.append("</ul>");
		put("page_data", sb);
		put("page_prev",prev);
		put("page_next", next);
		put("page_list",pagelist);
		put("page_now", page);
		Log.i("page_data = "+sb);
		return list.page(page, pagesz);
		
	}
//	/**
//	 * @deprecated
//	 * 获取参数，如果该参数为null，则返回使用参数def给出的值
//	 * @param name 参数名
//	 * @param def 默认值
//	 * @return  参数值
//	 */
//	@Deprecated
//	protected Object paramWithDefault(String name,Object def){
//		String ret = param(name);
//		if(def instanceof Integer && ret != null){
//			return Integer.parseInt(ret);
//		}
//		return ret == null?def:ret;
//	}
	/**
	 * 内部使用的属性，为了防止用户反复输出，设置一个flag，在开始一次新的请求时清空flag。
	 */
	void clearOutFlag(){
		can_out = true;
	}
	private boolean can_out =true;
	/**
	 * 获取整形参数 ,已过时，请使用paramInt(String pname,int def)
	 * @deprecated
	 * @param pname
	 * @param def
	 * @return 参数值
	 */
	@Deprecated
	protected int paramWithDefaultInt(String pname ,int def){
		String ret = param(pname);
		if(ret == null){
			return def;
		}else{
			return Integer.parseInt(ret);
		}
	}
	
	/**
	 * 使用模板目录中对应的文件进行渲染
	 */
	public void render() {
		render(getDefaultTemp());
	}
	Map<String,Object> root = new HashMap<String, Object>(); //渲染模板所用数据
	/**
	 * 向request中放入数据，方便在jsp中使用getAttribute获取，或者
	 * 使用EL表达式读取<br>
	 * 这个方法是MVC框架中使用频率非常高的一个方法。
	 * 用于将控制器中的数据传递给View。
	 * Demo: put("n",12); <br>
	 * JSP :　request.getAttribute("n") 会返回12, ${n}也会为12<br>
	 * @param name 参数名
	 * @param obj  参数值
	 */
	public void put(String name,Object obj){
		if(obj instanceof IBean){
			Log.d(obj.getClass().getName()+" translate to map :");
			obj = BeanUtil.obj2Map(obj);
			Log.d(JSON.toJSONString(obj));
		}
		request.setAttribute(name, obj);
		root.put(name, obj);
	}
	
	/**
	 * 将model中的每个字段以单独的变量形式加入request中
	 * @param m
	 */
	public void putModelItem(Model m){
		for(String k : m.keySet()){
			put(k,m.get(k));
		}
	}
	/**
	 * 是否存在某个文件
	 * @param path
	 * @return
	 */
	public boolean existFile(String path){
		@SuppressWarnings("deprecation")
		String p = request.getRealPath(path);
		if(p==null){
			//p = request.getServletContext().getRealPath(path);
			try {
				return  null !=  application.getResource(path);
			} catch (MalformedURLException e) {
				return false;
			}
		}else{
			return new File(p).exists();
		}
	}
	/**
	 * 渲染一个模板，模板为参数view指定，这个路径是相对于配置中的template目录的。
	 * @param view
	 */
	public void render(String view) {
		if(!can_out){
			try {
				outMutiOutErr("view "+view);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		can_out = false;
		
		String path = Config.tpl_base + view;
		
		if(!existFile(path)){
			Log.e("model file not found "+ path);
			output("模板文件不存在:"+path);
			return;
		}
		
		//判断是否有page参数，如果有生成几个变量给页面使用
	
		
		
		
		RequestDispatcher d = request.getRequestDispatcher(path);
		try {
			Log.d("dispatcher to " + path);
			d.forward(request, response);
		} catch (ServletException e) {
			Log.e(e);
		} catch (IOException e) {
			Log.e(e);
		}
	}
	private String json = null;
	/**
	 * 当content-type 为application/json ,该函数获取传来的json字符串
	 * @return 所有json字符串
	 */
	public String paramJSON(){
		return json ; 
	}
	/**
	 * 由过滤器调用这个方法来传送 request和response对象
	 * @param req
	 * @param resp
	 */
	public void setReq(HttpServletRequest req, HttpServletResponse resp) {
		this.request = req;
		this.response = resp;
		session = request.getSession();
		application = session.getServletContext();
		String contentType = request.getContentType();
		//Log.d("Controller contentType = "+contentType +", uri = "+req.getRequestURI());
		if(contentType!=null && contentType.startsWith(M_FLAG)){
			UploadProcesser processer  = new UploadProcesser(this, request, response,application);
			try {
				processer.process();
			} catch (FileUploadException e) {
				String s = Log.getErrMsg(e);
				Log.e(s);
				output(s);
			}
		}
		if(contentType!=null && contentType.contains("application/json")){
			InputStream in;
			try {
				Log.d("start read json ....");
				in = request.getInputStream();
				json = FileTool.readAll(in, "utf-8");
				if(json!=null){
					json = json.trim();
				}
				if(json.startsWith("{")){ //如果是一个对象，给予解包
					jsonToParam();
				}
				Log.d("json readed "+json);
			} catch (IOException e) {
				Log.e(e);
				e.printStackTrace();
				
				
			}
			
		}
		
		Cookie[] cookies = request.getCookies();
		
		if(cookies!=null){
			for(Cookie c  : cookies){
				cookieMap.put(c.getName(), c.getValue());
			}
		}
	}
	/**
	 * 将json中的数据转入param，使得用户察觉不到什么异常。
	 */
	public void jsonToParam(){
		if(json == null)return;
		JSONObject obj = JSON.parseObject(json);
		
		paramMap.putAll(obj);
	}
	/**
	 * 获取Session中key制定变量的值
	 * @param key
	 * @return sesion中存储的该键对应的内容，如果没有返回null
	 */
	public Object sess(String key){
		return session.getAttribute(key);
	}
	/**
	 * 设置Session中key变量的值为val
	 * @param key
	 * @param val
	 */
	public void sess(String key,Object val){
		session.setAttribute(key, val);
	}
	/**
	 * 获取int类型session变量
	 * @param key session变量的名字
	 * @return 如果有，则返回，否则返回null
	 */
	public Integer sessInt(String key){
		return (Integer)sess(key);
	}
	/**
	 * 获取double类型session变量
	 * @param key session变量的名字
	 * @return 如果有，则返回，否则返回null
	 */
	public Double sessDouble(String key) {
		return (Double)sess(key);
	}
	/**
	 * 获取String类型session变量
	 * @param key session变量的名字
	 * @return 如果有，则返回，否则返回null
	 */
	public String sessStr(String key) {
		Object o = sess(key);
		return o == null?null:o.toString();
	}
	
	
	public void removeSession(String key){
		session.removeAttribute(key);
	}
	/**
	 * 获取application对象中存储 的变量
	 * @param key 变量名
	 * @return 变量值， 如果没有则返回null
	 */
	public Object app(String key){
		return application.getAttribute("key");
	}
	/**
	 * 获取application对象中存储 的变量
	 * @param key 变量名
	 * @return 变量值， 如果没有则返回null
	 */
	public Integer appInt(String key) {
		return (Integer)app(key);
	}
	/**
	 * 获取application对象中存储 的变量
	 * @param key 变量名
	 * @return 变量值， 如果没有则返回null
	 */
	public Double appDouble(String key){
		return (Double)app(key);
	}
	/**
	 * 获取application对象中存储 的变量
	 * @param key 变量名
	 * @return 变量值， 如果没有则返回null
	 */
	public String appStr(String key) {
		Object o = app(key);
		return o == null?null:o.toString();
	}
	public void app(String key,Object val){
		application.setAttribute(key, val);
	}
	public void removeApp(String key){
		application.removeAttribute(key);
	}
	/**
	 * 获取cookie
	 * @param key
	 * @return value
	 */
	public String cookie(String key){
		return cookieMap.get(key);
	}
	
	/**
	 * 设置cookie
	 * @param key  键
	 * @param value 值
	 * @param period_ms 超时时间（单位毫秒）
	 * @return
	 */
	public String cookie(String key ,Object value,int period_ms){
		Cookie cookie = new Cookie(key,""+value);
		cookie.setMaxAge(period_ms);
		response.addCookie(cookie);
		cookieMap.put(key, ""+value);
		return value+"";
	}
	/**
	 * 删除cookie
	 * @param key
	 */
	public void removeCookie(String key){
		cookie(key,null,0); 
		cookieMap.remove(key);
	}
	
	void error(String s){
		
	}
	private void outMutiOutErr(String s) throws IOException{
		throw new IOException("童鞋，请不要在一个控制器方法中，写入多个输出语句。每个控制器每次执行只能输出一次。你是不是在if中忘了写return ? \n 您试图输出："+s);
	}
	
	private static  boolean enable_gzip = true;
	/**
	 * 设置开启gzip功能，如果发现客户端可以支持gzip（监测Accept-Encoding)，则压缩发送。
	 * 默认为打开状态
	 */
	public static void enableGzip(){
		enable_gzip =true;
	}
	/**
	 * 设置关闭gzip功能，全部以text/plain形式发送内容
	 * 默认为打开状态
	 */
	public static void disableGzip(){
		enable_gzip =false;
	}
	
	/**
	 * 判断是不是支持gzip，如果是gzip，就创建gzip输出流，反之创建一般输出流
	 * @return 写文件用的writer
	 */
	private PrintWriter getMyPrintWriter(){

		PrintWriter out = null;  
        try {
			if (enable_gzip && GzipUtil.isGzipSupported(request) ) {  
			    response.setHeader("Content-Encoding", "gzip");
			    
			    out = GzipUtil.getGzipWriter(response);  
			} else {  
			    out = response.getWriter();  
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
        return out;
	}
	/**
	 * 输出字符串 并关闭流
	 * @param s
	 */
	public void output(String s) {
		try {
			if(!can_out){
				outMutiOutErr(s);
				return;
			}
			can_out = false;
			response.setContentType("text/html; charset=utf-8");
			response.setCharacterEncoding("utf-8");
			writer = getMyPrintWriter();
			if(s == null)writer.write("null");
			else writer.write(s);
			writer.close();
		} catch (IOException e) {
			Log.e(e);
		}
	}
	/**
	 * 输出一个字节流
	 * @param ba
	 * @return
	 */
	public boolean outputBytes(byte[] ba){
		OutputStream oo;
		try {
			oo = response.getOutputStream();
			oo.write(ba);
			oo.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 输出JSON字符串到网页
	 * @param obj
	 */
	public void outputJSON(Object obj){		
		try {
			writer = getMyPrintWriter();
			
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/json");
			
			String debug = param("debug"); //如果是debug模式，则格式化输出json
			
			if(obj == null)writer.write("{}");
			else{String string = JSON.toJSONString(obj,debug!=null);
				writer.write(string);
			}
			writer.close();
		} catch (Exception e) {
			Log.e(e);
		}
		
	}
	
	/**
	 * 将列表以表格方式输出到name指定request变量中
	 * @param name 最后的表格将会存入这个字段中
	 * @param list 需要输出的列表
	 */
	public void putListTable(String name,LasyList list){
		StringBuffer sb = new StringBuffer();
		sb.append("<table class='table table-bordered yangmvc_table '>");
		for(Model m : list){
			sb.append("<tr>");
			for(String key: m.keySet()){
				Object v = m.get(key);
				if(v instanceof String){
					String sv = (String)v;
					if(sv!=null&& sv.length() > 20){
						v = sv.substring(0, 20)+"...";
					}
				}
				if(v == null){
					v = "";
				}
				sb.append("\t<td>"+v+"</td>\r\n");
			}
			sb.append("</tr>\r\n");
		}
		sb.append("</table>");
		put(name,sb.toString());
	}
	
	
	public void renderForm(Model m){
		if(m == null){
			output("未找到该对象");
			return;
		}
		renderForm(m,getDefaultTemp(),"");
	}
	public void renderForm(Model m,String template,String postTo){
		StringBuffer sb  = new StringBuffer();
		sb.append("<form class='yangmvc_form' method='post' action='"+postTo+"'>");
		for(String key : m.keySet()){
			if(key.equals(m.key())){
				continue;
			}
			sb.append("\n<div>\n<label>");
			sb.append(m.remark(key));
			Object v = m.get(key);
			if(v == null){
				v = "";
			}
			sb.append(String.format("</label>\n<input type='text' name='%s' value='%s' ",key,v));
			if(key.equals(m.key())){
				sb.append(" readonly='true' "  );
			}
			sb.append("></input>\n");
			
			sb.append("\n</div>");
		}
		sb.append("\n<div>\n<label>");
		if(m.get(m.key()) == null){
			sb.append("</label>\n<input type='submit'  value='添加'></input>\n");
		}else{
			sb.append("</label>\n<input type='submit'  value='修改'></input>\n");
		}
		sb.append("\n</div>");
	
		sb.append("\n</form>\n");
		put(m.getTableName()+"_form",sb.toString());
		render(template);
	}
	/**
	 * 应对一些url乱码
	 * @param p 参数名称
	 * @return 参数值，如果没有返回null
	 */
	public String urlParam(String p){
		//如果paramMap中有。 这个paramMap是在UploadProcesser中调用putParam修改的。 
		if(paramMap.containsKey(p))return paramMap.get(p).toString();
		String s =  request.getParameter(p);
		if(s == null)return null;
		try {
			s = new String(s.getBytes("ISO-8859-1"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return s;
	}
	/**
	 * 获取URL参数或者Form提交的参数
	 * @param p
	 * @return 参数内容
	 */
	public String param(String p){
		if(paramMap.containsKey(p))return paramMap.get(p).toString();
		return request.getParameter(p);
	}
	/**
	 * 制定默认值并获取URL参数，如果参数不存在，则返回默认值。参数存在返回参数值。
	 * @param name 参数名称
	 * @param def 默认值
	 * @return 如果参数不存在，则返回默认值。参数存在返回参数值。
	 */
	public String param(String name,String def){
		String ret = param(name);
		return ret == null?def:ret;
	}
	
	/**
	 * 获取URL参数或者Form提交的参数,并自动转换为int，如果不是整数则会报错。
	 * @param p 参数名称
	 * @return int类型的参数值
	 */
	
	public Integer paramInt(String p){
		String s = param(p);
		if(s!=null){
			return Integer.parseInt(s);
		}else{
			return null;
		}
	}
	/**
	 * 获取URL参数或者Form提交的参数,并自动转换为int，如果不是整数则会报错。
	 * 如果该参数不存在，则返回默认值
	 * @param p 参数名称
	 * @return int类型的参数值（参数不存在返回def)
	 */
	public Integer paramInt(String p ,int def){
		String ret = param(p);
		if(ret == null){
			return def;
		}else{
			return Integer.parseInt(ret);
		}
	}
	/**
	 * 根据名称匹配的原则，将与模型中参数名相同的参数的值放入模型中。并返回该模型<br>
	 * 是收集表单数据到模型中的神器，手机后就可以直接进行数据库操作了。
	 * @param m
	 * @return 得到的m，和参数m是同一个对象
	 */
	public Model paramToModel(Model m){
		for(String k:m.keySet()){
			if(k.equals(m.key()) && param(k)==null){
				continue; //如果主键为空，则主键不允许修改为null（与以前版本认为主键绝对不允许修改不同）
			}
			String v  = param(k);
			if(v!=null){
				m.put(k, v);
			}
		}
		return m;
	}
	
	/**
	 * 将参数中的值拷贝到对象的对应属性中
	 * 如 height参数拷贝到obj的height属性中
	 * 当prefix 为haha_的时候， haha_height会拷贝到obj的height属性中
	 * @param obj    要赋值的对象
	 * @param prefix 参数的前缀
	 * @return 赋值后的对象，将obj引用返回
	 */
	public <T> T paramToObj(T obj,String prefix){
		List<String> props  = BeanUtil.propList(obj);
		for(String p:props){
			String v = param(prefix+p);
			if(v !=null){
				BeanUtil.set(obj, p, v);
			}
		}
		return obj;
	}
	public void dumpParam() {
		Map<String, String[]> map = request.getParameterMap();
		Log.d("----------DumpParam---------");
		Log.d("request param="+JSON.toJSONString(map));
		Log.d("paramMap="+JSON.toJSONString(paramMap));
	}
	/**
	 * 将参数中的值拷贝到对象的对应属性中 
	 * 如 height参数拷贝到obj的height属性中
	 * @param obj    要赋值的对象
	 * @return 赋值后的对象，将obj引用返回
	 */
	public <T> T paramToObj(T obj){
		return paramToObj(obj,"");
	}
	
	/**
	 * 检查obj是否为null，如果为null，则抛出NullParamException ，这个错误会最终在网页上显示。
	 * 如果希望改变显示内容，可以提前捕获此异常并进行处理。
	 * @param name
	 * @param obj
	 * @throws NullParamException
	 */
	public void checkNull(String name,Object obj) throws NullParamException{
		if(obj == null){
			String msg="Param should not be NULL: "+name;
			if(getLang().contains("zh")){
				msg ="参数不能为空.这个问题一般是缺少url参数或form表单参数所致"+name;
			}
			throw new NullParamException(msg);
		}
	}
	/**
	 * 跳转到新页面/重定向。功能等同resposne.sendRedirect
	 * @param url
	 */
	public void jump(String url){
		try {
			if(!can_out){
				outMutiOutErr("jump to url");
				return;
			}
			can_out = false;
			
			response.sendRedirect(url);
		} catch (IOException e) {
			Log.e(e);
		}
	}
	/**
	 * 设置当前控制器为单例模式， 需要注意的是，只有不使用除了request、response、session之外的类变量的才可以使用单例。
	 * 单例模式可以优化性能，但也请慎用，做好测试。
	 * 如果是文件上传，则不应使用单例模式，因为会用到类变量。
	 * @param single 设置为true为单例，否则每次请求创建一个该控制器对象
	 */
	public void setSingle(boolean single){
		this.single = single;
	}
	/**
	 * 返回当前控制器是否是单例模式
	 * @return
	 */
	public boolean isSingle(){
		return single;
	}
	
	/**
	 * 获取当前请求所接受的语言种类
	 * @return 获取当前支持的语言
	 */
	public String getLang(){
		String lang = request.getHeader("Accept-Language");
		if(lang == null){
			return "en";
		}else{
			
			if(lang.contains(";")){
				lang=  TextTool.getBefore(lang, ";");
			}
			return lang;
		}
	}
	
	/**
	 * 限制当前Controller类中的所有方法的访问权限。这个方法常用于权限判断
	 * 如： session中存在uid字段，则说明已登录，如果没有，说明未登录，则跳转到登录页。
	 * 如果value为空，要求session中存在某个key，如果value不为空，要求session中的值等于value
	 * @param session_key
	 * @param value
	 * @param jump_url 如果不符合条件跳转到的地址
	 * @param err 如果jump_url 为空，则不跳转，显示一个错误信息
	 */
	public void require(String session_key,Object value,String jump_url,String err){
		require_obj =new Require();
		require_obj.key = session_key;
		require_obj.value = value;
		require_obj.jump_url = jump_url;
		require_obj.err= err;
	}
	/**
	 * 限制当前类所有方法的访问权限，一般在构造函数中调用。
	 * @param session_key session中需要存在的key，用来实现判断是否登录等功能
	 * @param jump_url 如果session中不存在这样的key，就会直接跳转到该网址
	 */
	public void require(String session_key,String jump_url){
		require(session_key, null,jump_url,"");
	}
	
	public class Require{
		public String key;
		public Object value;
		public String jump_url;
		public String err;
	}
	
	Require require_obj= null;
	
	/**
	 * 检查是否满足条件
	 * @return 满足为真，不满足为假
	 */
	boolean checkRequire(){
		if(require_obj==null || require_obj.key == null){
			return true;
		}
		Object obj = session.getAttribute(require_obj.key);
		if(require_obj.value ==null){
			return obj != null;
		}
		
		return  require_obj.value.equals(obj);
	}
	
	/**
	 * 根据succ的值决定是否执行跳转或显示错误的操作。此方法提供给MVCFilter使用
	 * @param succ
	 */
	void actionRequire(boolean succ){
		if(succ){
			return;
		}else{
			if(require_obj.jump_url == null){
				output(require_obj.err);
			}else{
				jump(require_obj.jump_url);
			}
		}
	}
	/**
	 * 输出下载内容。浏览器端会出现下载提示框
	 * @param path 需要下载的文件的路径。
	 */
	public void download(String path){
		//String true_path = request.getServletContext().getRealPath(path);
		response.setContentType("application/x-download");
		String filedisplay = new File(path).getName();		
		response.addHeader("Content-Disposition","attachment;filename=" + filedisplay); 
		try {
			request.getRequestDispatcher(path).forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try { 
//			ServletOutputStream o = response.getOutputStream();
//			InputStream in = new FileInputStream(true_path);
//			byte[] buf=new byte[1024];
//			int r = 0;
//			while(true){
//				r = in.read(buf);
//				if(r <= 0) break;
//				o.write(buf, 0, r);
//			}
//			o.flush();
//			o.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		

		
	}
	public void header(String name,String val){
		response.addHeader(name,val);
	}
	
	/**
	 * 读取请求中的正文部分
	 * @return 成功返回正文内容，失败返回空字符串
	 */
	public String reqBody(){
		try {
			return IOUtil.readStream(request.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 将session中的内容
	 */
	private void putSession(){
		Map<String, Object> pMap =new HashMap<String, Object>();
		Enumeration<String> names = session.getAttributeNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			pMap.put(name, session.getAttribute(name));
		}
		put("session",pMap);
	}
	/**
	 * 使用FreeMarker进行数据显示
	 * @param path 基于web.xml中配置的tpl_base值的相对路径
	 */
	public void renderFreeMarker(String path){
		Writer out=null;
		try {		
			//先吧param压如
			Map<String, Object> pMap =new HashMap<String, Object>();
			Enumeration<String> names = request.getParameterNames();
			while(names.hasMoreElements()){
				String name = names.nextElement();
				pMap.put(name, request.getParameter(name));
			}
			pMap.putAll(paramMap);
			put("param",pMap);
			
			//压如session
			putSession();
			
			response.setContentType("text/html; charset=utf-8");
			response.setCharacterEncoding("utf-8");
			Template tpl = MVCFilter.getIns().getFmCfg().getTemplate(path);
			out = getMyPrintWriter();
			tpl.process(root, out);
			out.close();
		} catch (Exception e) {
			if(out!=null){
				e.printStackTrace((PrintWriter)out);
			}
			//Log.e(e);
			//throw new MVCException(e);
		}
		
	}
	/**
	 * 获取tool对象
	 * @param tableName 数据库表格名称
	 * @return 相应的DBTool对象（用以进行查询等操作）
	 */
	public DBTool T(String tableName){
		return Model.tool(tableName);
	}
	/**
	 * 获取LasyList对象
	 * @param tableName 数据库表格名称
	 * @return 返回所有行的LasyList对象（并没有真正进行查询），可以用 .eq .gt 等函数去约束。
	 */
	public LasyList L(String tableName){
		return Model.tool(tableName).all();
	}
	/**
	 * 执行某个方法
	 * @param method
	 */
	public void runMethod(Method method){
		try {
			method.invoke(this);
		} catch (Exception e) {
			Log.e(e);
		}
	}
}
