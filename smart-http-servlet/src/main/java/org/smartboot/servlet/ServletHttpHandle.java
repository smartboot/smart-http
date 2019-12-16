package org.smartboot.servlet;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.handler.FilterHandler;
import org.smartboot.servlet.handler.ServletHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.war.WebContextRuntime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle {

    private Map<String, DeploymentRuntime> deploymentRuntimeMap = new HashMap<>();

    public ServletHttpHandle() {


        String location = "/Users/zhengjunwei/IdeaProjects/yt_trade/trade-web/target/dev-trade-web";
//        String location = "/Users/zhengjunwei/IdeaProjects/yt-buy/buy-web/target/buy-web";

        WebContextRuntime webContextRuntime = new WebContextRuntime(location);
        try {
            webContextRuntime.deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request);
        HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(response);

        HandlerContext exchange = new HandlerContext();
        exchange.setRequest(servletRequest);
        exchange.setResponse(servletResponse);
        DeploymentRuntime runtime = deploymentRuntimeMap.get("");
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
