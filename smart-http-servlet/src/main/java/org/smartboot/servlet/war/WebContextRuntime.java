package org.smartboot.servlet.war;

import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.DeploymentRuntime;
import org.smartboot.servlet.Lifecycle;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.EventListenerInfo;
import org.smartboot.servlet.conf.WebAppInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class WebContextRuntime implements Lifecycle {
    private volatile boolean started = false;
    private DeploymentRuntime deploymentRuntime;
    private String location;
    private String contextPath;

    public WebContextRuntime(String location, String contextPath) {
        this.location = location;
        this.contextPath = contextPath;
    }

    public WebContextRuntime(String location) {
        this.location = location;
    }

    public DeploymentRuntime getDeploymentRuntime() {
        return deploymentRuntime;
    }

    @Override
    public void start() throws Exception {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        FileInputStream webXmlInputStream = null;
        try {
            //load web.xml file
            WebXmlParse webXmlParse = new WebXmlParse();
            File contextFile = new File(location);

            webXmlInputStream = new FileInputStream(new File(contextFile, "WEB-INF" + File.separatorChar + "web.xml"));
            WebAppInfo webAppInfo = webXmlParse.load(webXmlInputStream);

            //new runtime object
            this.deploymentRuntime = new DeploymentRuntime();
            DeploymentInfo deploymentInfo = deploymentRuntime.getDeploymentInfo();

            //register Servlet into deploymentInfo
            webAppInfo.getServlets().values().forEach(deploymentInfo::addServlet);
            //register Filter
            webAppInfo.getFilters().values().forEach(deploymentInfo::addFilter);
            //register servletContext into deploymentInfo
            webAppInfo.getContextParams().forEach(deploymentInfo::addInitParameter);

            //register ServletContextListener into deploymentInfo
            webAppInfo.getListeners().forEach(value -> deploymentInfo.addServletContextListener(new EventListenerInfo(value)));

            //register filterMapping into deploymentInfo
            webAppInfo.getFilterMappings().forEach(filterMappingInfo -> deploymentInfo.addFilterMapping(filterMappingInfo));

            if (StringUtils.isBlank(contextPath)) {
                deploymentInfo.setContextPath("/" + contextFile.getName());
            } else {
                deploymentInfo.setContextPath(contextPath);
            }
            deploymentInfo.setRealPath(location);

            //自定义ClassLoader
            WebContextClassLoader webContextClassLoader = new WebContextClassLoader(location);
            ClassLoader webClassLoader = webContextClassLoader.getClassLoader();
            Thread.currentThread().setContextClassLoader(webClassLoader);
            deploymentInfo.setClassLoader(webClassLoader);
            deploymentRuntime.start();
        } finally {
            if (webXmlInputStream != null) {
                try {
                    webXmlInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
