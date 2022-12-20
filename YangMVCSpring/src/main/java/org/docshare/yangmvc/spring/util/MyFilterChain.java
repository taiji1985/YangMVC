package org.docshare.yangmvc.spring.util;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class MyFilterChain implements FilterChain {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
        //Log.i("filter chain do filter" +servletRequest.get);

    }
}
