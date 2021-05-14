/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRest.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpRequestImpl;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
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

    public HttpRest(String uri, String host, WriteBuffer writeBuffer, Consumer<CompletableFuture<HttpResponse>> responseListener) {
        this.request = new HttpRequestImpl(writeBuffer);
        this.responseListener = responseListener;
        this.request.setUri(uri);
        this.request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());
        this.keepalive(true)
                .addHeader(HttpHeaderConstant.Names.HOST, host)
                .addHeader(HttpHeaderConstant.Names.USER_AGENT, DEFAULT_USER_AGENT);
    }

    protected final void bindResponseListener() {
        responseListener.accept(completableFuture);
    }

    public final Future<HttpResponse> send() {
        try {
            bindResponseListener();
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
        request.setHeader(HttpHeaderConstant.Names.CONNECTION, flag ? HttpHeaderConstant.Values.KEEPALIVE : null);
        return this;
    }
}
