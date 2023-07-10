package org.smartboot.http.restful.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;
import org.smartboot.http.restful.intercept.MethodInterceptor;
import org.smartboot.http.restful.intercept.MethodInvocation;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.handler.HttpRouteHandler;
import org.smartboot.http.server.impl.Request;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
public class RestfulHandler extends HttpServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulHandler.class);
    private final HttpRouteHandler httpRouteHandler;
    private BiConsumer<HttpRequest, HttpResponse> inspect = (httpRequest, response) -> {
    };
    private final MethodInterceptor interceptor = MethodInvocation::proceed;


    public RestfulHandler(HttpServerHandler defaultHandler) {
        this.httpRouteHandler = defaultHandler != null ? new HttpRouteHandler(defaultHandler) : new HttpRouteHandler();
    }

    public void addController(Object object) {
        Class<?> clazz = object.getClass();
        Controller controller = clazz.getDeclaredAnnotation(Controller.class);
        String rootPath = controller.value();
        for (Method method : clazz.getDeclaredMethods()) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping == null) {
                continue;
            }
            String mappingUrl = getMappingUrl(rootPath, requestMapping);
            LOGGER.info("restful mapping: {} -> {}", mappingUrl, clazz.getName() + "." + method.getName());
            httpRouteHandler.route(mappingUrl, new ControllerHandler(method, controller, inspect, interceptor));
        }
    }


    private String getMappingUrl(String rootPath, RequestMapping requestMapping) {
        StringBuilder sb = new StringBuilder("/");
        if (rootPath.length() > 0) {
            if (rootPath.charAt(0) == '/') {
                sb.append(rootPath, 1, rootPath.length());
            } else {
                sb.append(rootPath);
            }
        }
        if (requestMapping.value().length() > 0) {
            char sbChar = sb.charAt(sb.length() - 1);
            if (requestMapping.value().charAt(0) == '/') {
                if (sbChar == '/') {
                    sb.append(requestMapping.value(), 1, requestMapping.value().length());
                } else {
                    sb.append(requestMapping.value());
                }
            } else {
                if (sbChar != '/') {
                    sb.append('/');
                }
                sb.append(requestMapping.value());
            }
        }
        return sb.toString();
    }

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        httpRouteHandler.onHeaderComplete(request);
    }

    public void setInspect(BiConsumer<HttpRequest, HttpResponse> inspect) {
        this.inspect = inspect;
    }


}
