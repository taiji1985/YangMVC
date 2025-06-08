package org.docshare.yangmvc.spring.controller;

import org.docshare.log.Log;
import org.docshare.mvc.MVCFilter;
import org.docshare.yangmvc.spring.config.MVCFilterInitializer;
import org.docshare.yangmvc.spring.util.MyFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class GlobalController {
    
    private final MyFilterChain chain = new MyFilterChain();
    
    @RequestMapping("/**")
    public void index(HttpServletRequest request, HttpServletResponse response) {
        // 使用MVCFilterInitializer中已经初始化好的filter
        MVCFilter filter = MVCFilterInitializer.getFilter();
        
        if (filter == null) {
            Log.e("MVCFilter未初始化，请检查MVCFilterInitializer配置");
            return;
        }
        
        try {
            filter.doFilter(request, response, chain);
        } catch (IOException | ServletException e) {
            Log.e(e);
        }
    }
}
