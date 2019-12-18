package org.smartboot.servlet.handler;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.utils.AntPathMatcher;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.ServletInfo;

import javax.servlet.Servlet;
import java.util.Map;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletMatchtHandler extends Handler {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        //匹配Servlet
        Servlet servlet = null;
        Map<String, ServletInfo> servletInfoMap = handlerContext.getDeploymentRuntime().getDeploymentInfo().getServlets();

        for (Map.Entry<String, ServletInfo> entry : servletInfoMap.entrySet()) {
            final ServletInfo servletInfo = entry.getValue();
            for (String path : servletInfo.getMappings()) {
                if (PATH_MATCHER.match(path, handlerContext.getRequest().getRequestURI())) {
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
