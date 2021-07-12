/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpProxyMessageProcessor.java
 * Date: 2021-07-12
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/12
 */
public class HttpProxyMessageProcessor extends HttpMessageProcessor {

    private final ConcurrentHashMap<AioSession, ProxyUnit> proxyClients = new ConcurrentHashMap<>();
    private final AsynchronousChannelGroup proxyChannelGroup;

    public HttpProxyMessageProcessor(AsynchronousChannelGroup proxyChannelGroup) {
        this.proxyChannelGroup = proxyChannelGroup;
    }

    @Override
    public void process(AioSession session, Request request) {
        //消息处理
        try {
            switch (request.getType()) {
                case PROXY_HTTPS:
                    proxyHttps(session, request);
                    break;
                case WEBSOCKET:
                    super.process(session, request);
                    break;
                default: {
                    HttpRequestImpl httpRequest = new HttpRequestImpl(request);
                    HttpResponseImpl response = httpRequest.getResponse();
                    //消息处理
                    httpPipeline.handle(httpRequest, response);

                    //response被closed,则断开TCP连接
                    if (response.isClosed()) {
                        session.close(false);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proxyHttps(AioSession session, Request request) throws IOException {
        ProxyUnit proxyUnit = proxyClients.get(session);
        //已经建立代理连接
        if (proxyUnit != null) {
            InputStream in = proxyUnit.inputStream;
            AioSession proxySession = proxyUnit.proxyClient.getSession();
            byte[] bytes = new byte[in.available()];
            int len = 0;
            while ((len = in.read(bytes)) > 0) {
                proxySession.writeBuffer().write(bytes, 0, len);
            }
            proxySession.writeBuffer().flush();
            return;
        }
        //代理认证
        HttpRequestImpl httpRequest = new HttpRequestImpl(request);
        HttpResponseImpl response = httpRequest.getResponse();
        httpPipeline.handle(httpRequest, httpRequest.getResponse());

        //连接成功,建立传输通道
        if (response.getHttpStatus() == HttpStatus.OK.value()) {
            createProxyConnect(session, httpRequest);
        } else {
            session.close(false);
        }
    }

    /**
     * 建立代理连接
     *
     * @param session
     * @param request
     * @throws IOException
     */
    private ProxyUnit createProxyConnect(AioSession session, HttpRequestImpl request) throws IOException {
        RequestAttachment attachment = session.getAttachment();
        ProxyUnit unit = new ProxyUnit();
        //生成代理客户端
        unit.proxyClient = createProxyClient(request);
        unit.inputStream = new InputStream() {
            @Override
            public int read() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(byte[] b, int off, int len) {
                int min = Math.min(len, attachment.getReadBuffer().remaining());
                attachment.getReadBuffer().get(b, off, min);
                return min;
            }

            @Override
            public int available() {
                return attachment.getReadBuffer().remaining();
            }
        };
        proxyClients.put(session, unit);
        return unit;
    }

    /**
     * 生成代理客户端
     *
     * @param request
     * @throws IOException
     */
    private AioQuickClient createProxyClient(HttpRequestImpl request) throws IOException {
        String host = request.getRequestURI();
        String[] array = host.split(":");
        AioQuickClient client = new AioQuickClient(array[0], array.length == 2 ? NumberUtils.toInt(array[1], 443) : 443, (readBuffer, session) -> {
            byte[] bytes = new byte[readBuffer.remaining()];
            System.out.println(request.getRequestURI() + " get data: " + bytes.length);
            readBuffer.get(bytes);
            return bytes;
        }, new MessageProcessor<byte[]>() {
            @Override
            public void process(AioSession session, byte[] msg) {
                try {
                    request.getResponse().getOutputStream().write(msg);
                    request.getResponse().getOutputStream().flush();
                } catch (IOException e) {
                    System.out.println(request.getRequestURI());
                    e.printStackTrace();
                }
            }

            @Override
            public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }
        });
        client.setReadBufferSize(1024 * 1024).connectTimeout(3000);
        client.start(proxyChannelGroup);
        return client;
    }


    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        super.stateEvent(session, stateMachineEnum, throwable);
        switch (stateMachineEnum) {
            case SESSION_CLOSED:
                ProxyUnit proxyUnit = proxyClients.remove(session);
                if (proxyUnit != null) {
                    proxyUnit.proxyClient.shutdownNow();
                }
                break;
        }
    }

    private class ProxyUnit {
        InputStream inputStream;
        private AioQuickClient proxyClient;
    }
}
