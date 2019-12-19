package org.smartboot.servlet;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.handler.FilterMatchHandler;
import org.smartboot.servlet.handler.HandlePipeline;
import org.smartboot.servlet.handler.ServletMatchHandler;
import org.smartboot.servlet.handler.ServletRequestListenerHandler;
import org.smartboot.servlet.handler.ServletServiceHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.war.WebContextRuntime;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle implements Lifecycle {
    private volatile boolean started = false;
    private DeploymentRuntime defaultRuntime = new DeploymentRuntime();
    private ContextMatcher contextMatcher = new ContextMatcher(defaultRuntime);
    private HandlePipeline pipeline = new HandlePipeline();

    public ServletHttpHandle() {
        start();
    }

    @Override
    public void start() {
        pipeline.next(new ServletRequestListenerHandler())
                .next(new ServletMatchHandler())
                .next(new FilterMatchHandler())
                .next(new ServletServiceHandler());

        defaultRuntime.getDeploymentInfo().setContextPath("");
        defaultRuntime.start();

        String location = "/Users/zhengjunwei/IdeaProjects/yt_trade/trade-web/target/dev-trade-web";
//        String location = "/Users/zhengjunwei/IdeaProjects/yt-buy/buy-web/target/buy-web";
        WebContextRuntime webContextRuntime = new WebContextRuntime(location);
        try {
            webContextRuntime.start();
            contextMatcher.addRuntime(webContextRuntime.getDeploymentRuntime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            DeploymentRuntime runtime = contextMatcher.matchRuntime(request.getRequestURI());
            Thread.currentThread().setContextClassLoader(runtime.getServletContext().getClassLoader());

            HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request, runtime.getServletContext());
            HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(response);
            HandlerContext exchange = new HandlerContext();
            exchange.setRequest(servletRequest);
            exchange.setResponse(servletResponse);
            exchange.setDeploymentRuntime(runtime);

            pipeline.handleRequest(exchange);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
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
