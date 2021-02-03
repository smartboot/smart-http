/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpClient.java
 * Date: 2021-02-02
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpClient {

    private final ArrayBlockingQueue<CompletableFuture<Response>> callbacks = new ArrayBlockingQueue<>(1024);
    private final String host;
    private final int port;
    private AioQuickClient<Response> client;
    private AioSession session;

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {
        HttpClient client = new HttpClient("localhost", 8080);
        client.connect();
        HttpRequestImpl request = new HttpRequestImpl(client.session.writeBuffer());
        request.setUri("/");
        request.setMethod(HttpMethodEnum.GET.getMethod());
        request.setProtocol(org.smartboot.http.HttpRequest.HTTP_1_1_STRING);
        request.setHeader(HttpHeaderConstant.Names.HOST, "127.0.0.1");
        client.execute(request, new Consumer<Response>() {
            @Override
            public void accept(Response response) {
                System.out.println(response);
            }
        });
    }

    private void connect() throws IOException {
        client = new AioQuickClient<>(host, port, new HttpResponseProtocol(), new HttpMessageProcessor());
        session = client.start();
    }

    public Response get(String url) throws ExecutionException, InterruptedException {
        return get(url, null).get();
    }

    public CompletableFuture<Response> get(String url, Consumer<Response> callback) {
        return null;
    }

    public CompletableFuture<Response> execute(HttpRequest request, Consumer<Response> callback) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        future.thenAccept(callback);
        callbacks.offer(future);
        try {
            request.getOutputStream().flush();
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        //输出请求消息
        return future;
    }
}
