package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        handlerContext.getServlet().service(handlerContext.getRequest(), handlerContext.getResponse());
        doNext(handlerContext);
    }
}
