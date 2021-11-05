/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpBootstrap.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.server.impl.HttpMessageProcessor;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;
import org.smartboot.socket.extension.processor.AbstractMessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

public class HttpBootstrap {

    private static final String BANNER = "                               _       _      _    _          \n" +
            "                              ( )_    ( )    ( )_ ( )_        \n" +
            "  ___   ___ ___     _ _  _ __ | ,_)   | |__  | ,_)| ,_) _ _   \n" +
            "/',__)/' _ ` _ `\\ /'_` )( '__)| |     |  _ `\\| |  | |  ( '_`\\ \n" +
            "\\__, \\| ( ) ( ) |( (_| || |   | |_    | | | || |_ | |_ | (_) )\n" +
            "(____/(_) (_) (_)`\\__,_)(_)   `\\__)   (_) (_)`\\__)`\\__)| ,__/'\n" +
            "                                                       | |    \n" +
            "                                                       (_)   ";

    private static final String VERSION = "1.1.10";
    /**
     * http消息解码器
     */
    private final HttpMessageProcessor processor;
    private final HttpServerConfiguration configuration = new HttpServerConfiguration();
    private AioQuickServer server;
    /**
     * Http服务端口号
     */
    private int port = 8080;

    public HttpBootstrap() {
        this(new HttpMessageProcessor());
    }

    public HttpBootstrap(HttpMessageProcessor processor) {
        this.processor = processor;
        this.processor.setConfiguration(configuration);
    }

    /**
     * Http服务端口号
     */
    public HttpBootstrap setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * 往 http 处理器管道中注册 Handle
     *
     * @param httpHandler
     * @return
     */
    public HttpBootstrap httpHandler(HttpServerHandler httpHandler) {
        processor.httpServerHandler(httpHandler);
        return this;
    }

    /**
     * 获取websocket的处理器管道
     *
     * @return
     */
    public HttpBootstrap webSocketHandler(WebSocketHandler webSocketHandler) {
        processor.setWebSocketHandler(webSocketHandler);
        return this;
    }

    /**
     * 服务配置
     *
     * @return
     */
    public HttpServerConfiguration configuration() {
        return configuration;
    }

    /**
     * 启动HTTP服务
     */
    public void start() {
        BufferPagePool readBufferPool = new BufferPagePool(configuration.getReadPageSize(), 1, false);
        server = new AioQuickServer(configuration.getHost(), port, new HttpRequestProtocol(configuration), getProcessor());
        server.setThreadNum(configuration.getThreadNum())
                .setBannerEnabled(false)
                .setBufferFactory(() -> new BufferPagePool(configuration.getWritePageSize(), configuration.getWritePageNum(), true))
                .setReadBufferFactory(bufferPage -> readBufferPool.allocateBufferPage().allocate(configuration.getReadBufferSize()))
                .setWriteBuffer(configuration.getWriteBufferSize(), 16);
        try {
            if (configuration.isBannerEnabled()) {
                System.out.println(BANNER + "\r\n :: smart-http :: (" + VERSION + ")");
            }
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MessageProcessor<Request> getProcessor() {
        MessageProcessor<Request> messageProcessor = configuration.getProcessor().apply(processor);
        if (configuration.isDebug()) {
            AbstractMessageProcessor<Request> abstractMessageProcessor = new AbstractMessageProcessor<Request>() {
                @Override
                public void process0(AioSession session, Request msg) {
                    messageProcessor.process(session, msg);
                }

                @Override
                public void stateEvent0(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
                    messageProcessor.stateEvent(session, stateMachineEnum, throwable);
                }
            };
            abstractMessageProcessor.addPlugin(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM, StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));
            return abstractMessageProcessor;
        } else {
            return messageProcessor;
        }
    }

    /**
     * 停止服务
     */
    public void shutdown() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}
