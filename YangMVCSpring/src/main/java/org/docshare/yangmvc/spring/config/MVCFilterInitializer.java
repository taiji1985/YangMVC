package org.docshare.yangmvc.spring.config;

import org.docshare.yangmvc.mvc.MVCFilter;
import org.docshare.yangmvc.spring.util.WrapperFilterConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * MVCFilter初始化器
 * 使用@PostConstruct确保在YangMvcConfigInitializer之前执行
 * 实现ServletContextAware接口获取ServletContext
 * 
 * @author YangMVC
 */
@Component
public class MVCFilterInitializer implements ServletContextAware {
    
    private static MVCFilter filter = null;
    private static boolean inited = false;
    private ServletContext servletContext;
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * 在Bean初始化完成后立即执行MVCFilter初始化
     */
    @PostConstruct
    public void initMVCFilter() {
        if (inited) return;
        inited = true;
        
        filter = new MVCFilter();
        try {
            filter.init(new WrapperFilterConfig(servletContext));
            System.out.println("MVCFilter初始化成功 - 在YangMvcConfigInitializer之前执行");
        } catch (ServletException e) {
            System.err.println("MVCFilter初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取已初始化的MVCFilter实例
     * @return MVCFilter实例
     */
    public static MVCFilter getFilter() {
        return filter;
    }
    
    /**
     * 检查MVCFilter是否已初始化
     * @return 是否已初始化
     */
    public static boolean isInited() {
        return inited;
    }
}