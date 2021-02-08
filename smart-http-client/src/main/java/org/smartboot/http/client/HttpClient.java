/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpClient.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.Response;
import org.smartboot.http.common.utils.Attachment;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
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
import java.util.concurrent.ExecutorService;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpClient implements Closeable {

    private final HttpResponseProtocol protocol = new HttpResponseProtocol();
    private final ArrayBlockingQueue<CompletableFuture<HttpResponse>> queue = new ArrayBlockingQueue<>(1024);
    private final HttpMessageProcessor processor = new HttpMessageProcessor();
    private final String host;
    private final int port;
    private AioQuickClient<Response> client;
    private AioSession aioSession;
    private BufferPagePool writeBufferPool;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private ExecutorService executorService;

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HttpGet get(String uri) {
        return new HttpGet(uri, host, aioSession.writeBuffer(), queue::offer);
    }

    public HttpRest rest(String uri) {
        return new HttpRest(uri, host, aioSession.writeBuffer(), queue::offer);
    }

    public void connect() {
        client = new AioQuickClient<>(host, port, protocol, processor);
        try {
            client.setBufferPagePool(writeBufferPool).setReadBufferFactory(bufferPage -> VirtualBuffer.wrap(ByteBuffer.allocate(1024)));
            aioSession = client.start(asynchronousChannelGroup);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setWriteBufferPool(BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
    }

    public void setAsynchronousChannelGroup(AsynchronousChannelGroup asynchronousChannelGroup) {
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
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

        @Override
        public void process(AioSession session, Response baseHttpResponse) {
            CompletableFuture<HttpResponse> httpRest = queue.poll();
            if (executorService == null) {
                httpRest.complete(baseHttpResponse);
            } else {
                session.awaitRead();
                executorService.execute(() -> {
                    httpRest.complete(baseHttpResponse);
                    session.signalRead();
                });
            }
        }

        @Override
        public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {

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
    }
}
