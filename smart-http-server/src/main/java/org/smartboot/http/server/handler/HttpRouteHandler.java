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
import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.impl.Request;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2018/3/24
 */
public final class HttpRouteHandler extends HttpServerHandler {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    /**
     * 默认404
     */
    private final HttpServerHandler defaultHandler = new HttpServerHandler() {
        @Override
        public void handle(HttpRequest request, HttpResponse response) throws IOException {
            response.setHttpStatus(HttpStatus.NOT_FOUND);
        }
    };
    private final HandlerCache[] handlerCaches = new HandlerCache[64];
    private final Map<String, HttpServerHandler> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void onHeaderComplete(Request request) throws IOException {
        matchHandler(request.getRequestURI()).onHeaderComplete(request);
    }

    @Override
    public boolean onBodyStream(ByteBuffer buffer, Request request) {
        return matchHandler(request.getRequestURI()).onBodyStream(buffer, request);
    }

    @Override
    public void onClose(Request request) {
        matchHandler(request.getRequestURI()).onClose(request);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws IOException {
        matchHandler(request.getRequestURI()).handle(request, response, completableFuture);
    }

    /**
     * 配置URL路由
     *
     * @param urlPattern  url匹配
     * @param httpHandler 处理handler
     * @return
     */
    public HttpRouteHandler route(String urlPattern, HttpServerHandler httpHandler) {
        //缓存精准路径
        if (!urlPattern.contains("*") && urlPattern.length() < handlerCaches.length) {
            ByteTree.ROOT.addNode(urlPattern);
            handlerCaches[urlPattern.length() - 1] = new HandlerCache(urlPattern, httpHandler);
        }
        handlerMap.put(urlPattern, httpHandler);
        return this;
    }

    private HttpServerHandler matchHandler(String uri) {
        if (uri == null) {
            return defaultHandler;
        }
        int len = uri.length();
        int index = len - 1;
        if (index < handlerCaches.length) {
            HandlerCache handleCache = handlerCaches[index];
            if (handleCache != null && handleCache.getUri().equals(uri)) {
                return handleCache.handler;
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
        return httpHandler;
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
