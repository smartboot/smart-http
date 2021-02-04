/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpClient.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.WebSocketRequest;
import org.smartboot.http.WebSocketResponse;
import org.smartboot.http.client.impl.HttpResponseImpl;
import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.Response;
import org.smartboot.http.common.HandlePipeline;
import org.smartboot.http.common.HttpClientHandle;
import org.smartboot.http.common.Pipeline;
import org.smartboot.http.common.WebSocketHandle;
import org.smartboot.http.utils.AttachKey;
import org.smartboot.http.utils.Attachment;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpClient implements Closeable {
    private final Map<String, ConnectCache> connectCacheMap = new HashMap<>();
    private final HttpResponseProtocol protocol = new HttpResponseProtocol();
    private final Map<AioSession, ArrayBlockingQueue<CompletableFuture<HttpResponse>>> map = new HashMap<>();
    private final HttpMessageProcessor processor = new HttpMessageProcessor();


    public static void main(String[] args) {
        HttpClient client = new HttpClient();
        for (int i = 0; i < 10; i++) {
            HttpGet httpGet = client.get("www.baidu.com", 80, "/")
                    .onSuccess(response -> System.out.println(response.body()))
                    .onFailure(Throwable::printStackTrace);
            httpGet.send();
            System.out.println("---华丽的分隔符---");
        }

    }

    public HttpGet get(String host, int port, String uri) {
        ConnectCache connectCache = connect(host, port);
        return new HttpGet(uri, connectCache.host, connectCache.session.writeBuffer(), httpResponseCompletableFuture -> {
            map.get(connectCache.session).offer(httpResponseCompletableFuture);
        });
    }

    public HttpRest rest(String host, int port, String uri) {
        ConnectCache connectCache = connect(host, port);
        return new HttpRest(uri, connectCache.host, connectCache.session.writeBuffer(), httpResponseCompletableFuture -> map.get(connectCache.session).offer(httpResponseCompletableFuture));
    }

    private ConnectCache connect(String host, int port) {
        String key = host + ":" + port;
        ConnectCache connectCache = connectCacheMap.get(key);
        if (connectCache != null && connectCache.session.isInvalid()) {
            connectCache.client.shutdownNow();
            connectCacheMap.remove(key);
            connectCache = null;
        }
        if (connectCache == null) {
            AioQuickClient<Response> client = new AioQuickClient<>(host, port, protocol, processor);
            try {
                client.setReadBufferSize(1024 * 4);
                AioSession session = client.start();
                connectCache = new ConnectCache(client, session, "http://" + key);
                connectCacheMap.put(key, connectCache);
                map.put(session, new ArrayBlockingQueue<>(1024));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return connectCache;
    }

    @Override
    public void close() throws IOException {
    }

    static class ConnectCache {
        private AioQuickClient<Response> client;
        private AioSession session;
        private String host;

        public ConnectCache(AioQuickClient<Response> client, AioSession session, String host) {
            this.client = client;
            this.session = session;
            this.host = host;
        }
    }

    /**
     * @author 三刀
     * @version V1.0 , 2018/6/10
     */
    class HttpMessageProcessor implements MessageProcessor<Response> {
        /**
         * HttpRequest附件Key
         */
        private final AttachKey<HttpResponseImpl> ATTACH_KEY_HTTP_RESPONSE = AttachKey.valueOf("httpRequest");
        /**
         * Http消息处理管道
         */
        private final HandlePipeline<HttpRequest, HttpResponse> httpPipeline = new HandlePipeline<>();
        /**
         * Websocket处理管道
         */
        private final HandlePipeline<org.smartboot.http.WebSocketRequest, org.smartboot.http.WebSocketResponse> wsPipeline = new HandlePipeline<>();

        @Override
        public void process(AioSession session, Response baseHttpResponse) {
            HttpClient.this.map.get(session).poll().complete(new HttpResponseImpl(baseHttpResponse));
        }

        @Override
        public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
            if (throwable != null) {
                throwable.printStackTrace();
            }

            switch (stateMachineEnum) {
                case NEW_SESSION:
                    Attachment attachment = new Attachment();
                    attachment.put(HttpResponseProtocol.ATTACH_KEY_RESPONSE, new Response());
                    session.setAttachment(attachment);
                    break;
                case PROCESS_EXCEPTION:
                    session.close();
                    break;
                case DECODE_EXCEPTION:
                    throwable.printStackTrace();
                    break;
                case SESSION_CLOSED:
                    map.remove(session);
                    break;
            }
        }

        public Pipeline<HttpRequest, HttpResponse> pipeline(HttpClientHandle httpHandle) {
            return httpPipeline.next(httpHandle);
        }

        public Pipeline<HttpRequest, HttpResponse> pipeline() {
            return httpPipeline;
        }

        public Pipeline<org.smartboot.http.WebSocketRequest, org.smartboot.http.WebSocketResponse> wsPipeline(WebSocketHandle httpHandle) {
            return wsPipeline.next(httpHandle);
        }

        public Pipeline<WebSocketRequest, WebSocketResponse> wsPipeline() {
            return wsPipeline;
        }

    }
}
