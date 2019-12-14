package org.smartboot.servlet;

import org.dom4j.DocumentException;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.handler.FilterHandler;
import org.smartboot.servlet.handler.ServletHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.war.WebContextClassLoader;
import org.smartboot.servlet.war.WebContextRuntime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle {

    private DeploymentRuntime runtime = new DeploymentRuntime();

    public ServletHttpHandle() throws MalformedURLException {
        DeploymentInfo deploymentInfo = runtime.getDeploymentInfo();
//        ServletInfo servletInfo = new ServletInfo();
//        servletInfo.setServletClass(DefaultServlet.class.getName());
//        servletInfo.setServletName("default");
//        servletInfo.addMapping("/");
//        servletInfo.addInitParam("abc", "123");
//        deploymentInfo.addServlet(servletInfo);
//
//        FilterInfo filterInfo = new FilterInfo();
//        filterInfo.setFilterName(DefaultFilter.class.getName());
//        filterInfo.setFilterName("default");
//        filterInfo.addInitParam("abc", "123");
//        deploymentInfo.addFilter(filterInfo);

//        String location = "/Users/zhengjunwei/IdeaProjects/yt_trade/trade-web/target/dev-trade-web";
        String location="/Users/zhengjunwei/IdeaProjects/yt-buy/buy-web/target/buy-web";
        WebContextClassLoader webContextClassLoader = new WebContextClassLoader(location);
        ClassLoader webClassLoader = webContextClassLoader.getClassLoader();
        deploymentInfo.setClassLoader(webClassLoader);
        Thread.currentThread().setContextClassLoader(webClassLoader);
        WebContextRuntime webContextRuntime = new WebContextRuntime(runtime, location);
        try {
            webContextRuntime.deploy();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        runtime.deploy();
    }


    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request);
        HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(response);

        HandlerContext exchange = new HandlerContext();
        exchange.setRequest(servletRequest);
        exchange.setResponse(servletResponse);
        exchange.setServletContext(runtime.getServletContext());
        exchange.setDeploymentInfo(runtime.getDeploymentInfo());
        try {
            ServletHandler servletHandler = new ServletHandler();
            FilterHandler filterHandler = new FilterHandler(servletHandler);
            filterHandler.handleRequest(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
