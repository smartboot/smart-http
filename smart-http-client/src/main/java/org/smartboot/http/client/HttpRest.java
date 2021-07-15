/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRest.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpRequestImpl;
import org.smartboot.http.client.impl.QueueUnit;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractQueue;
import java.util.Collection;
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
    private final AbstractQueue<QueueUnit> queue;
    private Map<String, String> queryParams = null;
    /**
     * http body 解码器
     */
    private HttpLifecycle bodyDecoder;

    HttpRest(String uri, String host, AioSession session, AbstractQueue<QueueUnit> queue) {
        this.request = new HttpRequestImpl(session);
        this.queue = queue;
        this.request.setUri(uri);
        this.request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());
        this.addHeader(HeaderNameEnum.HOST.getName(), host);
    }

    public HttpLifecycle bodyDecoder() {
        return bodyDecoder;
    }

    public HttpRest bodyDecoder(HttpLifecycle bodyDecoder) {
        this.bodyDecoder = bodyDecoder;
        return this;
    }

    protected final void willSendRequest() {
        resetUri();
        Collection<String> headers = request.getHeaderNames();
        if (!headers.contains(HeaderNameEnum.CONNECTION.getName())) {
            keepalive(true);
        }
        if (!headers.contains(HeaderNameEnum.USER_AGENT.getName())) {
            addHeader(HeaderNameEnum.USER_AGENT.getName(), DEFAULT_USER_AGENT);
        }
        queue.offer(new QueueUnit(this, completableFuture));
    }

    private void resetUri() {
        if (queryParams == null) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(request.getUri());
        int index = request.getUri().indexOf("#");
        if (index > 0) {
            stringBuilder.setLength(index);
        }
        index = request.getUri().indexOf("?");
        if (index == -1) {
            stringBuilder.append('?');
        } else if (index < stringBuilder.length() - 1) {
            stringBuilder.append('&');
        }
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
        request.setUri(stringBuilder.toString());
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

    public HttpRest setHeader(String headerName, String headerValue) {
        this.request.setHeader(headerName, headerValue);
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
