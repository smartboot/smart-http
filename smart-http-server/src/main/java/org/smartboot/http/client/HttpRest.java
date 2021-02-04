/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRest.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpRequestImpl;
import org.smartboot.http.enums.HttpProtocolEnum;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/3
 */
public class HttpRest {
    private final static String DEFAULT_USER_AGENT = "smart-http";
    protected final HttpRequestImpl request;
    private final CompletableFuture<HttpResponse> completableFuture = new CompletableFuture<>();
    private final Consumer<CompletableFuture<HttpResponse>> bindListener;


    public HttpRest(String uri, String host, WriteBuffer writeBuffer, Consumer<CompletableFuture<HttpResponse>> bindListener) {
        this.request = new HttpRequestImpl(writeBuffer);
        this.request.setUri(uri);
        this.request.setHeader(HttpHeaderConstant.Names.HOST, host);
        this.bindListener = bindListener;
        this.request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());
        this.request.setHeader(HttpHeaderConstant.Names.USER_AGENT, DEFAULT_USER_AGENT);
        keepalive(true);
    }

    public HttpRest send() {
        try {
            bindListener.accept(completableFuture);
            request.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
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

    public HttpRest setMethod(String method) {
        request.setMethod(method);
        return this;
    }

    public HttpRest keepalive(boolean flag) {
        request.setHeader(HttpHeaderConstant.Names.CONNECTION, flag ? HttpHeaderConstant.Values.KEEPALIVE : null);
        return this;
    }
}
