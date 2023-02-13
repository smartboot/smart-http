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
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.processor.AbstractMessageProcessor;
import org.smartboot.socket.extension.ssl.factory.ClientSSLContextFactory;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

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
        HttpGet httpGet = new HttpGet(uri, hostHeader, client.getSession(), processor.getQueue(client.getSession()));
        setProxyAuthorization(httpGet);
        return httpGet;
    }

    public HttpRest rest(String uri) {
        connect();
        HttpRest httpRest = new HttpRest(uri, hostHeader, client.getSession(), processor.getQueue(client.getSession()));
        setProxyAuthorization(httpRest);
        return httpRest;
    }

    public HttpPost post(String uri) {
        connect();
        HttpPost httpRest = new HttpPost(uri, hostHeader, client.getSession(), processor.getQueue(client.getSession()));
        setProxyAuthorization(httpRest);
        return httpRest;
    }

    private void setProxyAuthorization(HttpRest httpRest) {
        if (configuration.getProxy() != null && StringUtils.isNotBlank(configuration.getProxy().getProxyUserName())) {
            httpRest.request.addHeader(HeaderNameEnum.PROXY_AUTHORIZATION.getName(), "Basic " + Base64.getEncoder().encodeToString((configuration.getProxy().getProxyUserName() + ":" + configuration.getProxy().getProxyPassword()).getBytes()));
        }
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
            MessageProcessor<Response> processor = this.processor;
            if (configuration.isSsl()) {
                processor = new AbstractMessageProcessor<Response>() {
                    {
//                        addPlugin(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM,StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));
                        addPlugin(new SslPlugin<>(new ClientSSLContextFactory()));
//                        addPlugin(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM,StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));
//                        addPlugin(new StreamMonitorPlugin<>());
                    }

                    @Override
                    public void process0(AioSession aioSession, Response response) {
                        HttpClient.this.processor.process(aioSession, response);
                    }

                    @Override
                    public void stateEvent0(AioSession aioSession, StateMachineEnum stateMachineEnum, Throwable throwable) {
                        HttpClient.this.processor.stateEvent(aioSession, stateMachineEnum, throwable);
                    }
                };
            }

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
