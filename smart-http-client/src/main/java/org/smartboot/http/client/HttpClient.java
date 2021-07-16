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
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Base64;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class HttpClient implements Closeable {

    private final Protocol<Response> protocol;
    private final HttpMessageProcessor processor;
    private final String host;
    private final int port;
    private final String hostHeader;
    private AioQuickClient client;
    private AioSession aioSession;
    private BufferPagePool writeBufferPool;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private String proxyHost;
    private int proxyPort;
    private String proxyUserName;
    private String proxyPassword;

    public HttpClient(String host, int port) {
        this(host, port, new HttpResponseProtocol(), new HttpMessageProcessor());
    }

    public HttpClient(String host, int port, Protocol<Response> protocol, HttpMessageProcessor processor) {
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.processor = processor;
        hostHeader = host + ":" + port;
    }

    public HttpGet get(String uri) {
        HttpGet httpGet = new HttpGet(uri, hostHeader, aioSession, processor.getQueue(aioSession));
        setProxyAuthorization(httpGet);
        return httpGet;
    }

    public HttpRest rest(String uri) {
        HttpRest httpRest = new HttpRest(uri, hostHeader, aioSession, processor.getQueue(aioSession));
        setProxyAuthorization(httpRest);
        return httpRest;
    }

    public HttpPost post(String uri) {
        HttpPost httpRest = new HttpPost(uri, hostHeader, aioSession, processor.getQueue(aioSession));
        setProxyAuthorization(httpRest);
        return httpRest;
    }

    private void setProxyAuthorization(HttpRest httpRest) {
        if (StringUtils.isNotBlank(proxyUserName)) {
            httpRest.addHeader(HeaderNameEnum.PROXY_AUTHORIZATION.getName(), "Basic " + Base64.getEncoder().encodeToString((proxyUserName + ":" + proxyPassword).getBytes()));
        }
    }

    public HttpClient proxy(String host, int port, String username, String password) {
        this.proxyHost = host;
        this.proxyPort = port;
        this.proxyUserName = username;
        this.proxyPassword = password;
        return this;
    }

    public HttpClient proxy(String host, int port) {
        return this.proxy(host, port, null, null);
    }

    public void connect() {
        client = proxyHost == null ? new AioQuickClient(host, port, protocol, processor) : new AioQuickClient(proxyHost, proxyPort, protocol, processor);
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
