package org.smartboot.servlet;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.handler.FilterHandler;
import org.smartboot.servlet.handler.ServletHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle {
    private ServletContextImpl context = new ServletContextImpl();
    private DeploymentInfo deploymentInfo = new DeploymentInfo();
    private DeploymentRuntime runtime = new DeploymentRuntime(deploymentInfo);

    public ServletHttpHandle() {
        ServletInfo servletInfo = new ServletInfo(DefaultServlet.class, "default");
        servletInfo.addMapping("/");
        servletInfo.getInitParams().put("abc", "123");
        deploymentInfo.addServlet(servletInfo);
        runtime.deploy();
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request);
        HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(response);

        HttpServerExchange exchange = new HttpServerExchange();
        exchange.setRequest(servletRequest);
        exchange.setResponse(servletResponse);
        exchange.setServletContext(context);
        try {
            ServletHandler servletHandler = new ServletHandler();
            FilterHandler filterHandler = new FilterHandler(servletHandler);

            filterHandler.handleRequest(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
