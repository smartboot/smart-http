package org.smartboot.servlet.handler;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletMatchHandler extends Handler {
    private static final ServletPathMatcher PATH_MATCHER = new ServletPathMatcher();

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        //匹配Servlet
        Servlet servlet = null;
        ServletContext servletContext = handlerContext.getDeploymentRuntime().getServletContext();
        String contextPath = servletContext.getContextPath();
        Map<String, ServletInfo> servletInfoMap = handlerContext.getDeploymentRuntime().getDeploymentInfo().getServlets();
        HttpServletRequest request = handlerContext.getRequest();

        for (Map.Entry<String, ServletInfo> entry : servletInfoMap.entrySet()) {
            final ServletInfo servletInfo = entry.getValue();
            for (String path : servletInfo.getMappings()) {
                if (PATH_MATCHER.matches(contextPath + path, request.getRequestURI())) {
                    servlet = servletInfo.getServlet();
                    break;
                }
            }
            if (servlet != null) {
                break;
            }
        }
        if (servlet == null) {
            throw new HttpException(HttpStatus.NOT_FOUND);
        }
        handlerContext.setServlet(servlet);
        doNext(handlerContext);
    }
}
