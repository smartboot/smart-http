package org.smartboot.servlet;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServerExchange {

    private HttpServletRequestImpl request;
    private HttpServletResponseImpl response;
    private ServletContextImpl servletContext;

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
