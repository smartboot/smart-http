package org.smartboot.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.FilterConfigImpl;
import org.smartboot.servlet.impl.ServletConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DeploymentRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentRuntime.class);
    private DeploymentInfo deploymentInfo = new DeploymentInfo();
    private ServletContextImpl servletContext = new ServletContextImpl();

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public void deploy() {
        //启动Servlet
        Map<String, ServletInfo> servletInfoMap = deploymentInfo.getServlets();
        for (ServletInfo servletInfo : servletInfoMap.values()) {
            try {
                ServletConfig servletConfig = new ServletConfigImpl(servletInfo, servletContext);
                Servlet servlet = servletInfo.getServletClass().newInstance();
                LOGGER.info("init servlet:{}", servlet);
                servlet.init(servletConfig);
                servletInfo.setServlet(servlet);
                servletContext.addServletInfo(servletInfo);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }

        //启动Filter
        Map<String, FilterInfo> filterInfoMap = deploymentInfo.getFilters();
        for (FilterInfo filterInfo : filterInfoMap.values()) {
            try {
                FilterConfig filterConfig = new FilterConfigImpl(filterInfo, servletContext);
                Filter filter = filterInfo.getFilterClass().newInstance();
                LOGGER.info("init filter:{}", filter);
                filter.init(filterConfig);
                filterInfo.setFilter(filter);
                servletContext.addFilterInfo(filterInfo);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }
}
