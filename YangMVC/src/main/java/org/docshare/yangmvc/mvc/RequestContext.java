package org.docshare.yangmvc.mvc;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 请求上下文类，封装所有与请求相关的状态信息
 * 用于支持Controller的线程安全单例模式
 * @author 杨同峰
 */
public class RequestContext {
    /**
     * 请求对象，同jsp中的request。 但请优先使用param等函数
     */
    private HttpServletRequest request;
    
    /**
     * 响应对象，同jsp中的response。 但请优先使用output、outputJSON等函数
     */
    private HttpServletResponse response;
    
    /**
     * 会话对象。同jsp中的session。但请优先使用sess函数
     */
    private HttpSession session;
    
    /**
     * 输出对象，类似jsp中的out对象，但请优先使用output、outputJSON、downloadFile等函数。
     */
    private PrintWriter writer = null;
    
    /**
     * 参数映射表
     */
    private Map<String, Object> paramMap = new HashMap<String, Object>();
    
    /**
     * 同jsp中的application对象。
     */
    private ServletContext application;
    
    /**
     * Cookie映射表
     */
    private HashMap<String, String> cookieMap = new HashMap<>();
    
    /**
     * 输出控制标志
     */
    private boolean canOut = true;
    
    /**
     * 渲染模板所用数据
     */
    private Map<String, Object> root = new HashMap<String, Object>();
    
    /**
     * JSON数据
     */
    private String json = null;
    
    /**
     * 权限验证对象
     */
    private Controller.Require requireObj = null;
    
    // Getter和Setter方法
    public HttpServletRequest getRequest() {
        return request;
    }
    
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public HttpServletResponse getResponse() {
        return response;
    }
    
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
    
    public HttpSession getSession() {
        return session;
    }
    
    public void setSession(HttpSession session) {
        this.session = session;
    }
    
    public PrintWriter getWriter() {
        return writer;
    }
    
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }
    
    public Map<String, Object> getParamMap() {
        return paramMap;
    }
    
    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }
    public void clearParamMap(){
        if(this.paramMap!=null){
            this.paramMap.clear();
        }
    }
    
    public ServletContext getApplication() {
        return application;
    }

    public void setApplication(ServletContext application) {
        this.application = application;
    }
    
    /**
     * 获取ServletContext对象（与getApplication()相同）
     * @return ServletContext对象
     */
    public ServletContext getServletContext() {
        return application;
    }
    
    /**
     * 设置ServletContext对象（与setApplication()相同）
     * @param servletContext ServletContext对象
     */
    public void setServletContext(ServletContext servletContext) {
        this.application = servletContext;
    }
    
    public HashMap<String, String> getCookieMap() {
        return cookieMap;
    }
    
    public void setCookieMap(HashMap<String, String> cookieMap) {
        this.cookieMap = cookieMap;
    }
    
    public boolean isCanOut() {
        return canOut;
    }
    
    public void setCanOut(boolean canOut) {
        this.canOut = canOut;
    }
    
    public Map<String, Object> getRoot() {
        return root;
    }
    
    public void setRoot(Map<String, Object> root) {
        this.root = root;
    }
    
    public String getJson() {
        return json;
    }
    
    public void setJson(String json) {
        this.json = json;
    }
    
    public Controller.Require getRequireObj() {
        return requireObj;
    }
    
    public void setRequireObj(Controller.Require requireObj) {
        this.requireObj = requireObj;
    }
    
    /**
     * 清理所有数据
     */
    public void clear() {
        this.request = null;
        this.response = null;
        this.session = null;
        this.writer = null;
        this.paramMap.clear();
        this.application = null;
        this.cookieMap.clear();
        this.canOut = true;
        this.root.clear();
        this.json = null;
        this.requireObj = null;
    }
}