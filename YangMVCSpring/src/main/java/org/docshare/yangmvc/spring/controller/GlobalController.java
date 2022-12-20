package org.docshare.yangmvc.spring.controller;

import org.docshare.log.Log;
import org.docshare.mvc.MVCFilter;
import org.docshare.yangmvc.spring.util.MyFilterChain;
import org.docshare.yangmvc.spring.util.WrapperFilterConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class GlobalController {
    private static MVCFilter filter =null;
    boolean inited = false;
    GlobalController(){

    }
    private void init(ServletContext app){
        if(inited)return;
        inited = true;
        filter = new  MVCFilter();
        try {
            filter.init(new WrapperFilterConfig(app));
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
    MyFilterChain chain = new MyFilterChain();
    @RequestMapping("/**")
    public void index(HttpServletRequest  request, HttpServletResponse response){
//        try {
//            filter.doFilter(request,response,null);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ServletException e) {
//            throw new RuntimeException(e);
//        }
        init(request.getServletContext());
        try {
            filter.doFilter(request,response,chain);
        } catch (IOException | ServletException e) {
            Log.e(e);
        }
        //String r =  request.getRequestURI();
    }
}
