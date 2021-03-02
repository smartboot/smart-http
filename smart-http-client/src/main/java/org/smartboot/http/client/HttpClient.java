/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpClient.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpMessageProcessor;
import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.Response;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpClient implements Closeable {

    private final Protocol<Response> protocol;
    private final HttpMessageProcessor processor;
    private final String host;
    private final int port;
    private AioQuickClient<Response> client;
    private AioSession aioSession;
    private BufferPagePool writeBufferPool;
    private AsynchronousChannelGroup asynchronousChannelGroup;

    public HttpClient(String host, int port) {
        this(host, port, new HttpResponseProtocol(), new HttpMessageProcessor());
    }

    public HttpClient(String host, int port, Protocol<Response> protocol, HttpMessageProcessor processor) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.processor = processor;
    }

    public HttpGet get(String uri) {
        return new HttpGet(uri, host, aioSession.writeBuffer(), processor.getQueue(aioSession)::offer);
    }

    public HttpRest rest(String uri) {
        return new HttpRest(uri, host, aioSession.writeBuffer(), processor.getQueue(aioSession)::offer);
    }

    public HttpPost post(String uri) {
        return new HttpPost(uri, host, aioSession.writeBuffer(), processor.getQueue(aioSession)::offer);
    }

    public void connect() {
        client = new AioQuickClient<>(host, port, protocol, processor);
        try {
            client.setBufferPagePool(writeBufferPool).setReadBufferFactory(bufferPage -> VirtualBuffer.wrap(ByteBuffer.allocate(1024)));
            aioSession = asynchronousChannelGroup == null ? client.start() : client.start(asynchronousChannelGroup);
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

    @Override
    public void close() {
        client.shutdownNow();
    }

}
