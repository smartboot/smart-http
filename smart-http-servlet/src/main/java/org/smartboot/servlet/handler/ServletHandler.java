package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.ServletInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Map;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHandler implements Handler {
    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        //匹配Servlet
        ServletInfo defaultServlet = null;
        HttpServlet httpServlet = null;
        for (Map.Entry<String, ServletInfo> entry : handlerContext.getDeploymentInfo().getServlets().entrySet()) {
            final ServletInfo servletInfo = entry.getValue();
            for (String path : servletInfo.getMappings()) {

            }
        }
        httpServlet.service(handlerContext.getRequest(), handlerContext.getResponse());
    }
}
