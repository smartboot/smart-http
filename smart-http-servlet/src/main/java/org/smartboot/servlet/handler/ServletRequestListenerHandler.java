package org.smartboot.servlet.handler;

import org.smartboot.http.logging.Logger;
import org.smartboot.http.logging.LoggerFactory;
import org.smartboot.servlet.HandlerContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/19
 */
public class ServletRequestListenerHandler extends Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletRequestListenerHandler.class.getName());

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        ServletContext servletContext = handlerContext.getDeploymentRuntime().getServletContext();
        ServletRequestEvent servletRequestEvent = new ServletRequestEvent(servletContext, handlerContext.getRequest());
        List<ServletRequestListener> servletRequestListeners = handlerContext.getDeploymentRuntime().getDeploymentInfo().getServletRequestListeners();
        servletRequestListeners.forEach(requestListener -> {
            requestListener.requestInitialized(servletRequestEvent);
            LOGGER.info("requestInitialized " + requestListener);
        });
        try {
            doNext(handlerContext);
        } finally {
            servletRequestListeners.forEach(requestListener -> {
                requestListener.requestDestroyed(servletRequestEvent);
                LOGGER.info("requestDestroyed " + requestListener);
            });
        }
    }
}
