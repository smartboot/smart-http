/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RouteHandle.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handler;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.AntPathMatcher;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2018/3/24
 */
public final class HttpRouteHandler extends HttpServerHandler {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private final HandlerCache CACHE_DISABLED = new HandlerCache(null, null);
    /**
     * 默认404
     */
    private final HttpServerHandler defaultHandler = new HttpServerHandler() {
        @Override
        public void handle(HttpRequest request, HttpResponse response) throws IOException {
            response.setHttpStatus(HttpStatus.NOT_FOUND);
        }
    };
    private final HandlerCache[] handlerCaches = new HandlerCache[StringUtils.String_CACHE_URI.length];
    private final Map<String, HttpServerHandler> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        String uri = request.getRequestURI();
        int len = uri.length();
        int index = len - 1;
        if (index < handlerCaches.length) {
            HandlerCache handleCache = handlerCaches[index];
            if (handleCache != null && handleCache.getUri() == uri) {
                handleCache.handler.handle(request, response);
                return;
            }
        }
        HttpServerHandler httpHandler = handlerMap.get(uri);
        if (httpHandler == null) {
            for (Map.Entry<String, HttpServerHandler> entity : handlerMap.entrySet()) {
                if (PATH_MATCHER.match(entity.getKey(), uri)) {
                    httpHandler = entity.getValue();
                    break;
                }
            }
            if (httpHandler == null) {
                httpHandler = defaultHandler;
            }
            handlerMap.put(uri, httpHandler);
        }
        httpHandler.handle(request, response);
    }

    /**
     * 配置URL路由
     *
     * @param urlPattern url匹配
     * @param httpHandler 处理handler
     * @return
     */
    public HttpRouteHandler route(String urlPattern, HttpServerHandler httpHandler) {
        //缓存精准路径
        if (!urlPattern.contains("*") && StringUtils.addCache(StringUtils.String_CACHE_URI, urlPattern) && urlPattern.length() < handlerCaches.length) {
            handlerCaches[urlPattern.length() - 1] = new HandlerCache(urlPattern, httpHandler);
        }
        handlerMap.put(urlPattern, httpHandler);
        return this;
    }

    private static class HandlerCache {
        private final String uri;
        private final HttpServerHandler handler;

        public HandlerCache(String uri, HttpServerHandler handler) {
            this.uri = uri;
            this.handler = handler;
        }

        public String getUri() {
            return uri;
        }

        public HttpServerHandler getHandler() {
            return handler;
        }
    }
}
