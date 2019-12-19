package org.smartboot.servlet.conf;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行环境配置
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DeploymentInfo {
    private final Map<String, ServletInfo> servlets = new HashMap<>();
    private final Map<String, FilterInfo> filters = new HashMap<>();
    private final List<FilterMappingInfo> filterMappings = new ArrayList<>();
    private final Map<String, String> initParameters = new HashMap<>();
    private final List<String> eventListeners = new ArrayList<>();
    private final List<ServletContextListener> servletContextListeners = new ArrayList<>();
    private final List<ServletRequestListener> servletRequestListeners = new ArrayList<>();
    private ClassLoader classLoader;
    private String contextPath;
    private String realPath;
    private String displayName;

    public String getRealPath() {
        return realPath;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public DeploymentInfo addServlet(final ServletInfo servlet) {
        servlets.put(servlet.getServletName(), servlet);
        return this;
    }

    public DeploymentInfo addServlets(final ServletInfo... servlets) {
        for (final ServletInfo servlet : servlets) {
            addServlet(servlet);
        }
        return this;
    }

    public DeploymentInfo addServlets(final Collection<ServletInfo> servlets) {
        for (final ServletInfo servlet : servlets) {
            addServlet(servlet);
        }
        return this;
    }

    public Map<String, ServletInfo> getServlets() {
        return servlets;
    }

    public DeploymentInfo addFilter(final FilterInfo filter) {
        filters.put(filter.getFilterName(), filter);
        return this;
    }

    public DeploymentInfo addEventListener(final String listenerInfo) {
        eventListeners.add(listenerInfo);
        return this;
    }

    public List<String> getEventListeners() {
        return eventListeners;
    }

    public DeploymentInfo addServletContextListener(ServletContextListener contextListener) {
        servletContextListeners.add(contextListener);
        return this;
    }

    public DeploymentInfo addServletRequestListener(ServletRequestListener requestListener) {
        servletRequestListeners.add(requestListener);
        return this;
    }

    public List<ServletRequestListener> getServletRequestListeners() {
        return servletRequestListeners;
    }

    public DeploymentInfo addFilters(final FilterInfo... filters) {
        for (final FilterInfo filter : filters) {
            addFilter(filter);
        }
        return this;
    }

    public DeploymentInfo addFilters(final Collection<FilterInfo> filters) {
        for (final FilterInfo filter : filters) {
            addFilter(filter);
        }
        return this;
    }


    public Map<String, FilterInfo> getFilters() {
        return filters;
    }

    public void addFilterMapping(FilterMappingInfo filterMappingInfo) {
        filterMappings.add(filterMappingInfo);
    }

    public List<FilterMappingInfo> getFilterMappings() {
        return filterMappings;
    }

    public String getContextPath() {
        return contextPath;
    }

    public DeploymentInfo setContextPath(final String contextPath) {
        if (contextPath != null && contextPath.isEmpty()) {
            this.contextPath = "/"; //we represent the root context as / instead of "", but both work
        } else {
            this.contextPath = contextPath;
        }
        return this;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }

    public DeploymentInfo addInitParameter(final String name, final String value) {
        initParameters.put(name, value);
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

}
