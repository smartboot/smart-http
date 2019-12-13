package org.smartboot.servlet.war;

import org.dom4j.DocumentException;
import org.smartboot.servlet.DeploymentRuntime;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.ServletContextListenerInfo;
import org.smartboot.servlet.conf.WebAppInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class WebContextRuntime {
    private DeploymentRuntime deploymentRuntime;
    private String location;

    public WebContextRuntime(DeploymentRuntime deploymentRuntime, String location) {
        this.deploymentRuntime = deploymentRuntime;
        this.location = location;
    }

    public void deploy() throws FileNotFoundException, DocumentException {
        WebXmlParse webXmlParse = new WebXmlParse();
        FileInputStream webXmlInputStream = null;
        try {
            webXmlInputStream = new FileInputStream(new File(location, "WEB-INF" + File.separatorChar + "web.xml"));
            WebAppInfo webAppInfo = webXmlParse.load(webXmlInputStream);
            DeploymentInfo deploymentInfo = deploymentRuntime.getDeploymentInfo();
            //注册Servlet
            webAppInfo.getServlets().values().forEach(value -> deploymentInfo.addServlet(value));
            //注册Filter
            webAppInfo.getFilters().values().forEach(value -> deploymentInfo.addFilter(value));
            //注册servletContext参数
            webAppInfo.getContextParams().forEach((key, value) -> deploymentInfo.addInitParameter(key, value));

            //注册ServletContextListener
            webAppInfo.getListeners().forEach(value -> deploymentInfo.addServletContextListener(new ServletContextListenerInfo(value)));

            deploymentInfo.setContextPath("/");
        } finally {
            if (webXmlInputStream != null) {
                try {
                    webXmlInputStream.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
