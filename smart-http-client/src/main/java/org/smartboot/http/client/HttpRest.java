/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRest.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpRequestImpl;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/3
 */
public class HttpRest {
    private final static String DEFAULT_USER_AGENT = "smart-http";
    protected final HttpRequestImpl request;
    protected final CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();
    private final Consumer<CompletableFuture<HttpResponse>> responseListener;
    private Map<String, String> queryParams = null;

    public HttpRest(String uri, String host, WriteBuffer writeBuffer, Consumer<CompletableFuture<HttpResponse>> responseListener) {
        this.request = new HttpRequestImpl(writeBuffer);
        this.responseListener = responseListener;
        this.request.setUri(uri);
        this.request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());
        this.keepalive(true)
                .addHeader(HeaderNameEnum.HOST.getName(), host)
                .addHeader(HeaderNameEnum.USER_AGENT.getName(), DEFAULT_USER_AGENT);
    }

    protected final void willSendRequest() {
        resetUri();
        responseListener.accept(completableFuture);
    }

    private void resetUri() {
        if (queryParams == null) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        queryParams.forEach((key, value) -> {
            try {
                stringBuilder.append(key).append('=').append(URLEncoder.encode(value, "utf8")).append('&');
            } catch (UnsupportedEncodingException e) {
                stringBuilder.append(key).append('=').append(value).append('&');
            }
        });
        if (stringBuilder.length() > 0) {
            stringBuilder.setLength(stringBuilder.length() - 1);
        }
        int index1 = request.getUri().indexOf("#");
        int index2 = request.getUri().indexOf("?");
        if (index1 == -1 && index2 == -1) {
            request.setUri(request.getUri() + "?" + stringBuilder);
        } else if (index1 == -1) {
            if (index2 == request.getUri().length() - 1) {
                request.setUri(request.getUri() + stringBuilder);
            } else {
                request.setUri(request.getUri() + "&" + stringBuilder);
            }
        } else {
            if (index2 == -1) {
                request.setUri(request.getUri().substring(0, index1) + '?' + stringBuilder + request.getUri().substring(index1));
            } else {
                request.setUri(request.getUri().substring(0, index1) + '&' + stringBuilder + request.getUri().substring(index1));
            }
        }
    }

    public final Future<HttpResponse> send() {
        try {
            willSendRequest();
            request.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;
    }

    public HttpRest onSuccess(Consumer<HttpResponse> consumer) {
        completableFuture.thenAccept(consumer);
        return this;
    }

    public HttpRest onFailure(Consumer<Throwable> consumer) {
        completableFuture.exceptionally(throwable -> {
            consumer.accept(throwable);
            return null;
        });
        return this;
    }

    public HttpRest addHeader(String headerName, String headerValue) {
        this.request.addHeader(headerName, headerValue);
        return this;
    }

    public HttpRest setMethod(String method) {
        request.setMethod(method);
        return this;
    }

    public HttpRest keepalive(boolean flag) {
        request.setHeader(HeaderNameEnum.CONNECTION.getName(), flag ? HeaderValueEnum.KEEPALIVE.getName() : null);
        return this;
    }

    /**
     * 在 uri 后面添加请求参数
     *
     * @param name  参数名
     * @param value 参数值
     */
    public final HttpRest addQueryParam(String name, String value) {
        if (queryParams == null) {
            queryParams = new HashMap<>();
        }
        queryParams.put(name, value);
        return this;
    }
}
