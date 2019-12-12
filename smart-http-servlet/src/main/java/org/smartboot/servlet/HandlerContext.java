package org.smartboot.servlet;

import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HandlerContext {

    private HttpServletRequestImpl request;
    private HttpServletResponseImpl response;
    private ServletContextImpl servletContext;
    private DeploymentInfo deploymentInfo;

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public void setDeploymentInfo(DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
    }

    public HttpServletRequestImpl getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequestImpl request) {
        this.request = request;
    }

    public HttpServletResponseImpl getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponseImpl response) {
        this.response = response;
    }
}
