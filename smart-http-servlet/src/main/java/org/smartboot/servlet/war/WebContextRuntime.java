package org.smartboot.servlet.war;

import org.smartboot.servlet.DeploymentRuntime;
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
public class WebContextRuntime {
    private DeploymentRuntime deploymentRuntime;
    private String location;

    public WebContextRuntime(String location) {
        this.location = location;
    }

    public void deploy() throws Exception {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        FileInputStream webXmlInputStream = null;
        try {
            //load web.xml file
            WebXmlParse webXmlParse = new WebXmlParse();
            webXmlInputStream = new FileInputStream(new File(location, "WEB-INF" + File.separatorChar + "web.xml"));
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

            deploymentInfo.setContextPath("/");
            deploymentInfo.setRealPath(location);

            //自定义ClassLoader
            WebContextClassLoader webContextClassLoader = new WebContextClassLoader(location);
            ClassLoader webClassLoader = webContextClassLoader.getClassLoader();
            Thread.currentThread().setContextClassLoader(webClassLoader);
            deploymentInfo.setClassLoader(webClassLoader);
            deploymentRuntime.deploy();
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
}
