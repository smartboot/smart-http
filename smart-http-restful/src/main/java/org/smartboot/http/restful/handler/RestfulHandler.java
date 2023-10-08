package org.smartboot.http.restful.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.utils.AntPathMatcher;
import org.smartboot.http.common.utils.CollectionUtils;
import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.Interceptor;
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
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/7/2
 */
public class RestfulHandler extends HttpServerHandler {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(RestfulHandler.class);
    private final HttpRouteHandler httpRouteHandler;
    private final MethodInterceptor interceptor = MethodInvocation::proceed;

    private Map<Interceptor, InterceptorEntity> interceptors = new HashMap<>();

    public RestfulHandler(HttpServerHandler defaultHandler) {
        this.httpRouteHandler = defaultHandler != null ? new HttpRouteHandler(defaultHandler) : new HttpRouteHandler();
    }

    public void addInterceptor(Object object) {
        Class<?> clazz = object.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            Interceptor interceptor = method.getAnnotation(Interceptor.class);
            if (interceptor == null) {
                continue;
            }
            Parameter[] parameterTypes = method.getParameters();
            if (parameterTypes.length != 2 || parameterTypes[0].getType() != HttpRequest.class || parameterTypes[1].getType() != HttpResponse.class) {
                throw new IllegalArgumentException("Interceptor method must with parameters: (HttpRequest request,HttpResponse response)");
            }
            interceptors.put(interceptor, new InterceptorEntity(method, object));
        }
    }

    public void addController(Object object) {
        Class<?> clazz = object.getClass();
        String rootPath = clazz.getDeclaredAnnotation(Controller.class).value();
        for (Method method : clazz.getDeclaredMethods()) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping == null) {
                continue;
            }
            String mappingUrl = getMappingUrl(rootPath, requestMapping);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("restful mapping: {} -> {}", mappingUrl, clazz.getName() + "." + method.getName());
            }
            List<InterceptorEntity> list = new ArrayList<>();
            for (Map.Entry<Interceptor, InterceptorEntity> entry : interceptors.entrySet()) {
                boolean match = false;
                for (String pattern : entry.getKey().patterns()) {
                    if (PATH_MATCHER.match(pattern, mappingUrl)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    continue;
                }
                for (String pattern : entry.getKey().exclude()) {
                    if (PATH_MATCHER.match(pattern, mappingUrl)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    list.add(entry.getValue());
                }
            }
            MethodInterceptor interceptor0 = interceptor;
            if (CollectionUtils.isNotEmpty(list)) {
                interceptor0 = invocation -> {
                    for (InterceptorEntity entity : list) {
                        Object o = entity.method.invoke(entity.object, invocation.request(), invocation.response());
                        if (o != null) {
                            return o;
                        }
                    }
                    return invocation.proceed();
                };
            }

            httpRouteHandler.route(mappingUrl, new ControllerHandler(method, object, interceptor0));
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

    class InterceptorEntity {
        Method method;
        Object object;

        public InterceptorEntity(Method method, Object object) {
            this.method = method;
            this.object = object;
        }
    }
}
