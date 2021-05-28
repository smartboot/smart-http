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
import org.smartboot.http.common.utils.StringUtils;
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
    private final HandleCache CACHE_DISABLED = new HandleCache(null, null);
    /**
     * 默认404
     */
    private final HttpServerHandle defaultHandle = new HttpServerHandle() {
        @Override
        public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
            response.setHttpStatus(HttpStatus.NOT_FOUND);
        }
    };
    private final HandleCache[] handleCaches = new HandleCache[StringUtils.String_CACHE_URI.length];
    private final Map<String, HttpServerHandle> handleMap = new ConcurrentHashMap<>();

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        String uri = request.getRequestURI();
        int len = uri.length();
        int index = len - 1;
        if (index < handleCaches.length) {
            HandleCache handleCache = handleCaches[index];
            if (handleCache != null && handleCache.getUri() == uri) {
                handleCache.handle.doHandle(request, response);
                return;
            }
        }
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
        //缓存精准路径
        if (!urlPattern.contains("*") && StringUtils.addCache(StringUtils.String_CACHE_URI, urlPattern) && urlPattern.length() < handleCaches.length) {
            handleCaches[urlPattern.length() - 1] = new HandleCache(urlPattern, httpHandle);
        }
        handleMap.put(urlPattern, httpHandle);
        return this;
    }

    private static class HandleCache {
        private final String uri;
        private final HttpServerHandle handle;

        public HandleCache(String uri, HttpServerHandle handle) {
            this.uri = uri;
            this.handle = handle;
        }

        public String getUri() {
            return uri;
        }

        public HttpServerHandle getHandle() {
            return handle;
        }
    }
}
