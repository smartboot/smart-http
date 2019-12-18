package org.smartboot.servlet;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;

import javax.servlet.Servlet;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HandlerContext {

    private HttpServletRequestImpl request;
    private HttpServletResponseImpl response;
    private DeploymentRuntime deploymentRuntime;
    private Servlet servlet;

    public DeploymentRuntime getDeploymentRuntime() {
        return deploymentRuntime;
    }

    public void setDeploymentRuntime(DeploymentRuntime deploymentRuntime) {
        this.deploymentRuntime = deploymentRuntime;
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

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
}
