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
import org.smartboot.http.utils.Attachment;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.VirtualBufferFactory;
import org.smartboot.socket.buffer.BufferPage;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpClient implements Closeable {
    private static AsynchronousChannelGroup asynchronousChannelGroup;
    private static BufferPagePool bufferPagePool = new BufferPagePool(1024 * 1024 * 10, 8, true);

    static {
        try {
            System.setProperty("java.nio.channels.spi.AsynchronousChannelProvider", "org.smartboot.aio.EnhanceAsynchronousChannelProvider");
            asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(8, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final HttpResponseProtocol protocol = new HttpResponseProtocol();
    private final ArrayBlockingQueue<CompletableFuture<HttpResponse>> queue = new ArrayBlockingQueue<>(1024);
    private final HttpMessageProcessor processor = new HttpMessageProcessor();
    private String host;
    private int port;
    private AioQuickClient<Response> client;
    private AioSession aioSession;

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
        connect();
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        int time = 15 * 1000;
        int threadNum = 4;
        int connectCount = 1024;
        int pipeline = 16;
        AtomicLong success = new AtomicLong(0);
        AtomicLong fail = new AtomicLong(0);
        AtomicBoolean running = new AtomicBoolean(true);
        ArrayBlockingQueue<HttpClient> queue = new ArrayBlockingQueue<>(connectCount);
        for (int i = 0; i < connectCount; i++) {
            HttpClient client = new HttpClient("127.0.0.1", 8080);
            queue.put(client);
        }
        for (int i = 0; i < threadNum; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("start...");
                    while (running.get()) {
                        try {
                            HttpClient httpClient = queue.take();
                            httpClient.get("/plaintext")
                                    .onSuccess(response -> {
                                        success.incrementAndGet();
//                                        System.out.println(response.body());
//                                        if (running.get()) {
//                                            queue.offer(httpClient);
//                                        } else {
//                                            httpClient.close();
//                                        }
                                    })
                                    .onFailure(throwable -> fail.incrementAndGet())
                                    .send();
                            queue.offer(httpClient);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        Thread.sleep(time);
        running.set(false);
        HttpClient httpClient;
        while ((httpClient = queue.poll()) != null) {
            httpClient.close();
        }
        System.out.println("cost:" + (System.currentTimeMillis() - start));

        System.out.println("success:" + success.get());
        System.out.println("fail:" + fail.get());
    }

    public HttpGet get(String uri) {
        return new HttpGet(uri, host, aioSession.writeBuffer(), queue::offer);
    }

    public HttpRest rest(String uri) {
        return new HttpRest(uri, host, aioSession.writeBuffer(), queue::offer);
    }

    private void connect() {
        client = new AioQuickClient<>(host, port, protocol, processor);
        try {
            client.setBufferPagePool(bufferPagePool).setReadBufferFactory(new VirtualBufferFactory() {
                @Override
                public VirtualBuffer newBuffer(BufferPage bufferPage) {
                    return VirtualBuffer.wrap(ByteBuffer.allocate(1024));
                }
            });
            aioSession = client.start(asynchronousChannelGroup);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        client.shutdownNow();
    }

    /**
     * @author 三刀
     * @version V1.0 , 2018/6/10
     */
    class HttpMessageProcessor implements MessageProcessor<Response> {
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
            queue.poll().complete(new HttpResponseImpl(baseHttpResponse));
        }

        @Override
        public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
//            if (throwable != null) {
//                throwable.printStackTrace();
//            }

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
