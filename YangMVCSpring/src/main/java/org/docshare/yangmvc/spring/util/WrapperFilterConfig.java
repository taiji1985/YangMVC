package org.docshare.yangmvc.spring.util;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

public class WrapperFilterConfig implements FilterConfig {
    private final ServletContext app;

    public WrapperFilterConfig(ServletContext app){
        this.app = app;
    }
    @Override
    public String getFilterName() {
        return "wrappered_filter";
    }

    @Override
    public ServletContext getServletContext() {
        return app;
    }

    @Override
    public String getInitParameter(String s) {
        return app.getInitParameter(s);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return app.getInitParameterNames();
    }
}
