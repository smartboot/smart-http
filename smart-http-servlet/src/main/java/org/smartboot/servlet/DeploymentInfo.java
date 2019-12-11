package org.smartboot.servlet;

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


    public DeploymentInfo addServlet(final ServletInfo servlet) {
        servlets.put(servlet.getName(), servlet);
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
        filters.put(filter.getName(), filter);
        return this;
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
}
