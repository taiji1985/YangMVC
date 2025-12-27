package org.docshare.yangmvc.springboot.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * FilterConfig包装器类
 * <p>
 * 将Spring Boot的ServletContext包装为FilterConfig，以便在Spring Boot环境中
 * 正确初始化YangMVC的MVCFilter
 *
 * @author YangMVC
 */
public class WrapperFilterConfig implements FilterConfig {

    private final ServletContext servletContext;
    private final String filterName;
    private final Map<String, String> initParameters;

    /**
     * 构造函数
     *
     * @param servletContext Servlet上下文
     */
    public WrapperFilterConfig(ServletContext servletContext) {
        this(servletContext, "yangMvcFilter", new HashMap<>());
    }

    /**
     * 构造函数
     *
     * @param servletContext Servlet上下文
     * @param filterName     过滤器名称
     */
    public WrapperFilterConfig(ServletContext servletContext, String filterName) {
        this(servletContext, filterName, new HashMap<>());
    }

    /**
     * 构造函数
     *
     * @param servletContext  Servlet上下文
     * @param filterName      过滤器名称
     * @param initParameters  初始化参数
     */
    public WrapperFilterConfig(ServletContext servletContext, String filterName, Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.filterName = filterName;
        this.initParameters = initParameters != null ? initParameters : new HashMap<>();
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    /**
     * 添加初始化参数
     *
     * @param name  参数名
     * @param value 参数值
     */
    public void addInitParameter(String name, String value) {
        initParameters.put(name, value);
    }

    /**
     * 获取所有初始化参数
     *
     * @return 初始化参数Map的副本
     */
    public Map<String, String> getAllInitParameters() {
        return new HashMap<>(initParameters);
    }
}
