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
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.transport.AioQuickClient;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Base64;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public final class HttpClient {

    private final HttpClientConfiguration configuration;
    /**
     * Http 解码协议
     */
    private final Protocol<Response> protocol;
    /**
     * 消息处理器
     */
    private final HttpMessageProcessor processor;

    /**
     * Header: Host
     */
    private final String hostHeader;
    /**
     * 客户端Client
     */
    private AioQuickClient client;

    /**
     * 绑定线程池资源组
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    private boolean connected;


    public HttpClient(String host, int port) {
        this(host, port, new HttpResponseProtocol(), new HttpMessageProcessor());
    }

    public HttpClient(String host, int port, Protocol<Response> protocol, HttpMessageProcessor processor) {
        configuration = new HttpClientConfiguration(host, port);
        this.protocol = protocol;
        this.processor = processor;
        hostHeader = host + ":" + port;
    }

    public HttpGet get(String uri) {
        connect();
        HttpGet httpGet = new HttpGet(client.getSession(), processor.getQueue(client.getSession()));
        initRest(httpGet, uri);
        return httpGet;
    }

    public HttpRest rest(String uri) {
        connect();
        HttpRest httpRest = new HttpRest(client.getSession(), processor.getQueue(client.getSession()));
        initRest(httpRest, uri);
        return httpRest;
    }

    public HttpPost post(String uri) {
        connect();
        HttpPost httpRest = new HttpPost(client.getSession(), processor.getQueue(client.getSession()));
        initRest(httpRest, uri);
        return httpRest;
    }

    private void initRest(HttpRest httpRest, String uri) {
        if (configuration.getProxy() != null && StringUtils.isNotBlank(configuration.getProxy().getProxyUserName())) {
            httpRest.request.addHeader(HeaderNameEnum.PROXY_AUTHORIZATION.getName(), "Basic " + Base64.getEncoder().encodeToString((configuration.getProxy().getProxyUserName() + ":" + configuration.getProxy().getProxyPassword()).getBytes()));
        }
        httpRest.request.setUri(uri);
        httpRest.request.addHeader(HeaderNameEnum.HOST.getName(), hostHeader);
        httpRest.request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());

        httpRest.completableFuture.thenAccept(httpResponse -> {
            //request标注为keep-alive，response不包含该header,默认保持连接.
            if (HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(httpRest.request.getHeader(HeaderNameEnum.CONNECTION.getName())) && httpResponse.getHeader(HeaderNameEnum.CONNECTION.getName()) == null) {
                return;
            }
            //非keep-alive,主动断开连接
            if (!HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(httpResponse.getHeader(HeaderNameEnum.CONNECTION.getName()))) {
                close();
            } else if (!HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(httpRest.request.getHeader(HeaderNameEnum.CONNECTION.getName()))) {
                close();
            }
        });
        httpRest.completableFuture.exceptionally(throwable -> {
            close();
            return null;
        });
    }


    public HttpClientConfiguration configuration() {
        return configuration;
    }

    private void connect() {
        if (connected) {
            return;
        }
        connected = true;
        try {
            configuration.getPlugins().forEach(processor::addPlugin);
            client = configuration.getProxy() == null ? new AioQuickClient(configuration.getHost(), configuration.getPort(), protocol, processor) : new AioQuickClient(configuration.getProxy().getProxyHost(), configuration.getProxy().getProxyPort(), protocol, processor);
            BufferPagePool readPool = configuration.getReadBufferPool();
            client.setBufferPagePool(configuration.getWriteBufferPool()).setReadBufferFactory(bufferPage -> readPool == null ? VirtualBuffer.wrap(ByteBuffer.allocate(configuration.readBufferSize())) : readPool.allocateBufferPage().allocate(configuration.readBufferSize()));
            if (configuration.getConnectTimeout() > 0) {
                client.connectTimeout(configuration.getConnectTimeout());
            }
            if (asynchronousChannelGroup == null) {
                client.start();
            } else {
                client.start(asynchronousChannelGroup);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void setAsynchronousChannelGroup(AsynchronousChannelGroup asynchronousChannelGroup) {
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    public void close() {
        client.shutdownNow();
    }

}
