package org.smartboot.servlet;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.handler.FilterHandler;
import org.smartboot.servlet.handler.ServletHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle {

    private DeploymentRuntime runtime = new DeploymentRuntime();

    public ServletHttpHandle() {
        DeploymentInfo deploymentInfo = runtime.getDeploymentInfo();
        ServletInfo servletInfo = new ServletInfo(DefaultServlet.class, "default");
        servletInfo.addMapping("/");
        servletInfo.addInitParam("abc", "123");
        deploymentInfo.addServlet(servletInfo);

        FilterInfo filterInfo = new FilterInfo(DefaultFilter.class, "default");
        filterInfo.addInitParam("abc", "123");
        deploymentInfo.addFilter(filterInfo);

        runtime.deploy();
    }


    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request);
        HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(response);

        HttpServerExchange exchange = new HttpServerExchange();
        exchange.setRequest(servletRequest);
        exchange.setResponse(servletResponse);
        exchange.setServletContext(runtime.getServletContext());
        try {
            ServletHandler servletHandler = new ServletHandler();
            FilterHandler filterHandler = new FilterHandler(servletHandler);

            filterHandler.handleRequest(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
