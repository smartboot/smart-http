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
    /**
     * Http 解码协议
     */
    private final Protocol<Response> protocol;
    /**
     * 消息处理器
     */
    private final HttpMessageProcessor processor;
    /**
     * 远程地址
     */
    private final String host;
    /**
     * 远程端口
     */
    private final int port;
    /**
     * Header: Host
     */
    private final String hostHeader;
    /**
     * 客户端Client
     */
    private AioQuickClient client;
    /**
     * 缓冲池
     */
    private BufferPagePool writeBufferPool;

    /**
     * 缓冲池，必须是堆内缓冲区
     */
    private BufferPagePool readBufferPool;

    /**
     * 绑定线程池资源组
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    /**
     * 代理服务器地址
     */
    private String proxyHost;
    /**
     * 代理服务器端口
     */
    private int proxyPort;
    /**
     * 代理服务器授权账户
     */
    private String proxyUserName;
    /**
     * 代理服务器授权密码
     */
    private String proxyPassword;

    /**
     * 连接超时时间
     */
    private int timeout;

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
        HttpGet httpGet = new HttpGet(uri, hostHeader, client.getSession(), processor.getQueue(client.getSession()));
        setProxyAuthorization(httpGet);
        return httpGet;
    }

    public HttpRest rest(String uri) {
        HttpRest httpRest = new HttpRest(uri, hostHeader, client.getSession(), processor.getQueue(client.getSession()));
        setProxyAuthorization(httpRest);
        return httpRest;
    }

    public HttpPost post(String uri) {
        HttpPost httpRest = new HttpPost(uri, hostHeader, client.getSession(), processor.getQueue(client.getSession()));
        setProxyAuthorization(httpRest);
        return httpRest;
    }

    private void setProxyAuthorization(HttpRest httpRest) {
        if (StringUtils.isNotBlank(proxyUserName)) {
            httpRest.addHeader(HeaderNameEnum.PROXY_AUTHORIZATION.getName(), "Basic " + Base64.getEncoder().encodeToString((proxyUserName + ":" + proxyPassword).getBytes()));
        }
    }

    /**
     * 设置 Http 代理服务器
     *
     * @param host     代理服务器地址
     * @param port     代理服务器端口
     * @param username 授权账户
     * @param password 授权密码
     */
    public HttpClient proxy(String host, int port, String username, String password) {
        this.proxyHost = host;
        this.proxyPort = port;
        this.proxyUserName = username;
        this.proxyPassword = password;
        return this;
    }

    /**
     * 连接代理服务器
     *
     * @param host 代理服务器地址
     * @param port 代理服务器端口
     */
    public HttpClient proxy(String host, int port) {
        return this.proxy(host, port, null, null);
    }

    public void connect() {
        client = proxyHost == null ? new AioQuickClient(host, port, protocol, processor) : new AioQuickClient(proxyHost, proxyPort, protocol, processor);
        try {
            client.setBufferPagePool(writeBufferPool).setReadBufferFactory(bufferPage -> readBufferPool == null ? VirtualBuffer.wrap(ByteBuffer.allocate(4096)) : readBufferPool.allocateBufferPage().allocate(1024 * 4));
            if (timeout > 0) {
                client.connectTimeout(timeout);
            }
            if (asynchronousChannelGroup == null) {
                client.start();
            } else {
                client.start(asynchronousChannelGroup);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setReadBufferPool(BufferPagePool readBufferPool) {
        this.readBufferPool = readBufferPool;
    }

    public void setWriteBufferPool(BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
    }

    public void setAsynchronousChannelGroup(AsynchronousChannelGroup asynchronousChannelGroup) {
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    /**
     * 设置建立连接的超时时间
     *
     * @param timeout
     * @return
     */
    public HttpClient timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public void close() {
        client.shutdownNow();
    }

}
