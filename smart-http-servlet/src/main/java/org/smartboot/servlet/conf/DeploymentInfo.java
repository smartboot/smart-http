package org.smartboot.servlet.conf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DeploymentInfo {
    private final Map<String, ServletInfo> servlets = new HashMap<>();
    private final Map<String, FilterInfo> filters = new HashMap<>();
    private final Map<String, String> initParameters = new HashMap<>();
    private final Map<String, EventListenerInfo> eventListeners = new HashMap<>();
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

    public DeploymentInfo addServletContextListener(final EventListenerInfo listenerInfo) {
        eventListeners.put(listenerInfo.getListenerClass(), listenerInfo);
        return this;
    }

    public Map<String, EventListenerInfo> getEventListeners() {
        return eventListeners;
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
