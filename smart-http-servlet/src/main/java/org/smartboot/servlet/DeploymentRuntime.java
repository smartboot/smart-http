package org.smartboot.servlet;

import org.smartboot.http.logging.Logger;
import org.smartboot.http.logging.LoggerFactory;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletContextListenerInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.FilterConfigImpl;
import org.smartboot.servlet.impl.ServletConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestListener;
import java.util.EventListener;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DeploymentRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentRuntime.class);
    private DeploymentInfo deploymentInfo = new DeploymentInfo();
    private ServletContextImpl servletContext = new ServletContextImpl(deploymentInfo);


    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public void deploy() {
        //设置ServletContext参数
        Map<String, String> params = deploymentInfo.getInitParameters();
        params.forEach((key, value) -> {
            servletContext.setInitParameter(key, value);
        });

        //启动Listener
        Map<String, ServletContextListenerInfo> listenerInfoMap = deploymentInfo.getContextListeners();
        for (ServletContextListenerInfo contextListenerInfo : listenerInfoMap.values()) {
            try {
                EventListener listener = (EventListener) Thread.currentThread().getContextClassLoader().loadClass(contextListenerInfo.getListenerClass()).newInstance();
                if (ServletContextListener.class.isAssignableFrom(listener.getClass())) {
                    ServletContextEvent event = new ServletContextEvent(servletContext);
                    ((ServletContextListener) listener).contextInitialized(event);
                    LOGGER.info("contextInitialized listener:" + listener);
                } else if (ServletRequestListener.class.isAssignableFrom(listener.getClass())) {
                    System.err.println(listener);
//                    ServletRequestEvent event = new ServletRequestEvent();
//                    ((ServletRequestListener) listener).requestInitialized(event);
                } else {
                    throw new RuntimeException(listener.toString());
                }

                contextListenerInfo.setListener(listener);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        //启动Servlet
        Map<String, ServletInfo> servletInfoMap = deploymentInfo.getServlets();
        for (ServletInfo servletInfo : servletInfoMap.values()) {
            try {
                ServletConfig servletConfig = new ServletConfigImpl(servletInfo, servletContext);
                Servlet servlet = (Servlet) Thread.currentThread().getContextClassLoader().loadClass(servletInfo.getServletClass()).newInstance();
//                LOGGER.info("init servlet:{}", servlet);
                servlet.init(servletConfig);
                servletInfo.setServlet(servlet);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        //启动Filter
        Map<String, FilterInfo> filterInfoMap = deploymentInfo.getFilters();
        for (FilterInfo filterInfo : filterInfoMap.values()) {
            try {
                FilterConfig filterConfig = new FilterConfigImpl(filterInfo, servletContext);
                Filter filter = (Filter) Thread.currentThread().getContextClassLoader().loadClass(filterInfo.getFilterClass()).newInstance();
//                LOGGER.info("init filter:{}", filter);
                filter.init(filterConfig);
                filterInfo.setFilter(filter);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ServletException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }
}
