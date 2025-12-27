package org.docshare.yangmvc.springboot.filter;

import org.docshare.yangmvc.mvc.MVCFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 延迟初始化的Filter包装器
 * <p>
 * 这个Filter包装实际的MVCFilter，延迟其初始化直到第一次请求时
 * 这样可以确保YangMVC配置在Filter使用前已经加载完成
 *
 * @author YangMVC
 */
public class DelayedInitMVCFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DelayedInitMVCFilter.class);

    private MVCFilter delegate;
    private FilterConfig filterConfig;
    private volatile boolean initialized = false;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        logger.info("DelayedInitMVCFilter created, will initialize on first request");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 延迟初始化
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initializeDelegate();
                }
            }
        }

        // 委托给实际的MVCFilter
        if (delegate != null) {
            delegate.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * 初始化实际的MVCFilter
     */
    private void initializeDelegate() {
        try {
            logger.info("Initializing MVCFilter delegate...");
            delegate = new MVCFilter();
            delegate.init(filterConfig);
            initialized = true;
            logger.info("MVCFilter delegate initialized successfully");
        } catch (ServletException e) {
            logger.error("Failed to initialize MVCFilter delegate", e);
            throw new RuntimeException("Failed to initialize MVCFilter", e);
        }
    }

    @Override
    public void destroy() {
        if (delegate != null) {
            delegate.destroy();
        }
    }
}
