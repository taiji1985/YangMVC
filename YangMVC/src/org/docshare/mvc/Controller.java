package org.docshare.mvc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileUploadException;
import org.docshare.log.Log;
import org.docshare.mvc.except.NullParamException;
import org.docshare.orm.LasyList;
import org.docshare.orm.Model;

import com.alibaba.fastjson.JSON;

public class Controller {

	public static final String M_FLAG="multipart/form-data";

	
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected HttpSession session; 
	protected PrintWriter writer = null;
	protected Map<String, Object> paramMap=new HashMap<String, Object>();


	private ServletContext application;


	private boolean single = false;
	protected void putParam(String key,Object val) {
		Log.d("put param"+key+"= "+val);
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
	private String getDefaultTemp(){
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
		int total = list.size();
		
		Integer page =  (Integer) paramWithDefault("page", 1);
		Integer pagesz =  (Integer) paramWithDefault("pagesz", 30);
		int pagec = (int) Math.floor(total/pagesz)+1;
		Log.d(total/pagesz);
		
		Integer prev = page - 1 <= 0 ? null: page-1;
		Integer next = page+1>pagec? null:page+1;
		
		int start = page -5 < 1 ? 1: page-5;
		int end = page +5 > pagec? pagec:page+5;
		end  = (start +10)<pagec ? start+10:end;
		start = (end - 10 )>=0 ? end-10:start;
		List<Integer> pagelist = new ArrayList<Integer>();
		
		StringBuffer sb = new StringBuffer();
		sb.append("<ul class='yangmvc_page'>");
		if(prev!=null){
			sb.append("<li><a href='"+getPath()+"?page="+prev+"'>&lt;&lt;</a></li>");
		}else{
			sb.append("<li>&lt;&lt;</li>");
		}
		for(int i=start;i<=end;i++){
			pagelist.add(i);
			if(i== page.intValue()){
				sb.append("<li>"+i+"</li>");
			}else{
				sb.append("<li><a href='"+getPath()+"?page="+i+"'>"+i+"</a></li>");
			}
		}
		if(next != null){
			sb.append("<li><a href='"+getPath()+"?page="+next+"'>&gt;&gt;</a></li>");
		}else{
			sb.append("<li>&gt;&gt;</li>");
		}
		sb.append("</ul>");
		put("page_data", sb);
		put("page_prev",prev);
		put("page_next", next);
		put("page_list",pagelist);
		put("page_now", page);
		
		return list.page(page, pagesz);
		
	}
	/**
	 * 获取参数，如果该参数为null，则返回使用参数def给出的值
	 * @param name 参数名
	 * @param def 默认值
	 * @return 
	 */
	public Object paramWithDefault(String name,Object def){
		Object ret = param(name);
		return ret == null?def:ret;
	}
	/**
	 * 使用模板目录中对应的文件进行渲染
	 */
	public void render() {
		render(getDefaultTemp());
	}
	/**
	 * 向request中放入数据，方便在jsp中使用getAttribute获取，或者
	 * 使用EL表达式读取<br>
	 * Demo: put("n",12); <br>
	 * JSP :　request.getAttribute("n") 会返回12, ${n}也会为12<br>
	 * @param name
	 * @param obj
	 */
	public void put(String name,Object obj){
		request.setAttribute(name, obj);
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
	
	private boolean existFile(String path){
		String p = request.getSession().getServletContext().getRealPath(path);
		return new File(p).exists();
	}
	/**
	 * 渲染一个模板，模板为参数view指定，这个路径是相对于配置中的template目录的。
	 * @param view
	 */
	public void render(String view) {
		String path = Config.tpl_base + view;
		
		if(!existFile(path)){
			output("模板文件不存在:"+path);
			return;
		}
		
		//判断是否有page参数，如果有生成几个变量给页面使用
	
		
		
		
		RequestDispatcher d = request.getRequestDispatcher(path);
		try {
			d.forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 由过滤器调用这个方法来传送 request和response对象
	 * @param req
	 * @param resp
	 */
	void setReq(HttpServletRequest req, HttpServletResponse resp) {
		this.request = req;
		this.response = resp;
		session = request.getSession();
		application = session.getServletContext();
		String contentType = request.getContentType();
		if(contentType!=null && contentType.startsWith(M_FLAG)){
			UploadProcesser processer  = new UploadProcesser(this, request, response);
			try {
				processer.process();
			} catch (FileUploadException e) {
				String s = Log.getErrMsg(e);
				Log.e(s);
				output(s);
			}
		}
	}
	void error(String s){
		
	}
	/**
	 * 输出字符串 并关闭流
	 * @param s
	 */
	public void output(String s) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("utf-8");
			writer = response.getWriter();
			
			writer.write(s);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 输出JSON字符串到网页
	 * @param obj
	 */
	public void outputJSON(Object obj){
		String string = JSON.toJSONString(obj);
		output(string);
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
			if(key.equals(m.getPrimaryKey())){
				continue;
			}
			sb.append("\n<div>\n<label>");
			sb.append(m.getRemark(key));
			Object v = m.get(key);
			if(v == null){
				v = "";
			}
			sb.append(String.format("</label>\n<input type='text' name='%s' value='%s' ",key,v));
			if(key.equals(m.getPrimaryKey())){
				sb.append(" readonly='true' "  );
			}
			sb.append("></input>\n");
			
			sb.append("\n</div>");
		}
		sb.append("\n<div>\n<label>");
		if(m.get(m.getPrimaryKey()) == null){
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
	 * 获取URL参数或者Form提交的参数
	 * @param p
	 * @return
	 */
	public String param(String p){
		if(paramMap.containsKey(p))return paramMap.get(p).toString();
		return request.getParameter(p);
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
	 * 根据名称匹配的原则，将与模型中参数名相同的参数的值放入模型中。并返回该模型<br>
	 * 是收集表单数据到模型中的神器，手机后就可以直接进行数据库操作了。
	 * @param m
	 * @return
	 */
	public Model paramToModel(Model m){
		for(String k:m.keySet()){
			if(k.equals(m.getPrimaryKey())){
				continue; //主键不允许修改
			}
			String v  = request.getParameter(k);
			if(v!=null){
				m.put(k, request.getParameter(k));
			}
		}
		return m;
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
	
	public void jump(String url){
		try {
			response.sendRedirect(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 设置当前控制器为单例模式， 
	 * @param single 设置为true为单例，否则每次请求创建一个该控制器对象
	 */
	public void setSingle(boolean single){
		this.single = single;
	}
	public boolean isSingle(){
		return single;
	}
	
	/**
	 * 获取当前请求所接受的语言种类
	 * @return
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
}
