/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RouteHandle.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handle;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.AntPathMatcher;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2018/3/24
 */
public final class HttpRouteHandle extends HttpServerHandle {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private Map<String, HttpServerHandle> handleMap = new ConcurrentHashMap<>();

    /**
     * 默认404
     */
    private final HttpServerHandle defaultHandle = new HttpServerHandle() {
        @Override
        public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
            response.setHttpStatus(HttpStatus.NOT_FOUND);
        }
    };

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        String uri = request.getRequestURI();
        HttpServerHandle httpHandle = handleMap.get(uri);
        if (httpHandle == null) {
            for (Map.Entry<String, HttpServerHandle> entity : handleMap.entrySet()) {
                if (PATH_MATCHER.match(entity.getKey(), uri)) {
                    httpHandle = entity.getValue();
                    break;
                }
            }
            if (httpHandle == null) {
                httpHandle = defaultHandle;
            }
            handleMap.put(uri, httpHandle);
        }

        httpHandle.doHandle(request, response);
    }

    /**
     * 配置URL路由
     *
     * @param urlPattern url匹配
     * @param httpHandle 处理handle
     * @return
     */
    public HttpRouteHandle route(String urlPattern, HttpServerHandle httpHandle) {
        handleMap.put(urlPattern, httpHandle);
        return this;
    }

}
