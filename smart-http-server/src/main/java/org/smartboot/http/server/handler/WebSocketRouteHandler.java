/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RouteHandle.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handler;

import org.smartboot.http.common.enums.BodyStreamStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.AntPathMatcher;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2018/3/24
 */
public final class WebSocketRouteHandler extends WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketRouteHandler.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    /**
     * 默认404
     */
    private final WebSocketHandler defaultHandler = new WebSocketHandler() {
        @Override
        public void handle(WebSocketRequest request, WebSocketResponse response) throws IOException {
            LOGGER.warn("not found");
        }
    };
    private final Map<String, WebSocketHandler> handlerMap = new ConcurrentHashMap<>();

    @Override
    public void onClose(Request request) {
        handlerMap.get(request.getRequestURI()).onClose(request);
    }

    @Override
    public void willHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {
        String uri = request.getRequestURI();
        WebSocketHandler httpHandler = handlerMap.get(uri);
        if (httpHandler == null) {
            for (Map.Entry<String, WebSocketHandler> entity : handlerMap.entrySet()) {
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
        httpHandler.willHeaderComplete(request, response);
    }

    @Override
    public void whenHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {
        WebSocketHandler httpHandler = handlerMap.get(request.getRequestURI());
        httpHandler.whenHeaderComplete(request, response);
    }

    @Override
    public BodyStreamStatus onBodyStream(ByteBuffer byteBuffer, Request request) {
        return handlerMap.get(request.getRequestURI()).onBodyStream(byteBuffer, request);
    }

    @Override
    public void handle(WebSocketRequest request, WebSocketResponse response) throws Throwable {
        WebSocketHandler httpHandler = handlerMap.get(request.getRequestURI());
        httpHandler.handle(request, response);
    }

    /**
     * 配置URL路由
     *
     * @param urlPattern url匹配
     * @param httpHandle 处理handle
     * @return
     */
    public WebSocketRouteHandler route(String urlPattern, WebSocketHandler httpHandle) {
        handlerMap.put(urlPattern, httpHandle);
        return this;
    }
}
